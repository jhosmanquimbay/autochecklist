package com.example.demostracion.controller;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.FiltroReporteDTO;
import com.example.demostracion.dto.ReporteInventarioDTO;
import com.example.demostracion.dto.ResultadoCargaInventarioDTO;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.service.CargaMasivaInventarioService;
import com.example.demostracion.service.InventarioVentaService;
import com.example.demostracion.service.PDFGeneratorService;
import com.example.demostracion.service.ReporteInventarioService;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
public class InventarioController {

    private final InventarioRepository inventarioRepository;
    private final CargaMasivaInventarioService cargaMasivaInventarioService;
    private final ReporteInventarioService reporteInventarioService;
    private final PDFGeneratorService pdfGeneratorService;
    private final InventarioVentaService inventarioVentaService;

    public InventarioController(InventarioRepository inventarioRepository,
                               CargaMasivaInventarioService cargaMasivaInventarioService,
                               ReporteInventarioService reporteInventarioService,
                               PDFGeneratorService pdfGeneratorService,
                               InventarioVentaService inventarioVentaService) {
        this.inventarioRepository = inventarioRepository;
        this.cargaMasivaInventarioService = cargaMasivaInventarioService;
        this.reporteInventarioService = reporteInventarioService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.inventarioVentaService = inventarioVentaService;
    }

    // 👉 Listar inventario con filtros
    @GetMapping
    public String index(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String estadoLogistico,
            @RequestParam(required = false, defaultValue = "true") Boolean mostrarActivos,
            Model model) {

        List<Inventario> inventarios;

        if ((marca != null && !marca.isEmpty()) ||
            (modelo != null && !modelo.isEmpty()) ||
            (color != null && !color.isEmpty()) ||
            (estadoLogistico != null && !estadoLogistico.isEmpty())) {

            inventarios = inventarioRepository.findByFiltrosAndActivo(
                    marca != null ? marca : "",
                    modelo != null ? modelo : "",
                    color != null ? color : "",
                    estadoLogistico != null ? estadoLogistico : "",
                    mostrarActivos);
        } else {
            inventarios = mostrarActivos ? 
                inventarioRepository.findByActivoTrue() : 
                inventarioRepository.findAll();
        }

        model.addAttribute("inventarios", inventarios);
        model.addAttribute("mostrarActivos", mostrarActivos);
        return "inventario/index";
    }

    // 👉 Mostrar formulario crear
    @GetMapping("/crear")
    public String crear(Model model) {
        model.addAttribute("inventario", new Inventario());
        model.addAttribute("titulo", "Agregar Vehículo al Inventario");
        return "inventario/form";
    }

    // 👉 Guardar nuevo registro con validación de chasis duplicado
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Inventario inventario, Model model) {
        if (inventarioRepository.existsByChasis(inventario.getChasis())) {
            model.addAttribute("inventario", inventario);
            model.addAttribute("error", "El chasis '" + inventario.getChasis() + "' ya existe en el inventario.");
            model.addAttribute("titulo", "Agregar Vehículo al Inventario");
            return "inventario/form";
        }
        inventario.setActivo(true); // Por defecto se crea como activo
        if (inventario.getCantidadDisponible() == null || inventario.getCantidadDisponible() < 1) {
            inventario.setCantidadDisponible(1);
        }
        inventarioRepository.save(inventario);
        inventarioVentaService.sincronizarVehiculoConInventario(inventario);
        return "redirect:/inventario";
    }

    // 👉 Editar registro
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado: " + id));
        model.addAttribute("inventario", inventario);
        model.addAttribute("titulo", "Editar Vehículo en Inventario");
        return "inventario/form";
    }

    // 👉 Actualizar registro
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Inventario inventario) {
        inventario.setIdInventario(id);
        inventarioRepository.save(inventario);
        inventarioVentaService.sincronizarVehiculoConInventario(inventario);
        return "redirect:/inventario";
    }

    @PostMapping("/cambiar-estado/{id}")
    public String toggleActivo(@PathVariable Long id, @RequestParam(required = false) Boolean mostrarActivos) {
        inventarioRepository.findById(id).ifPresent(inv -> {
            inv.setActivo(!inv.isActivo()); // Cambia el estado
            inventarioRepository.save(inv);
            inventarioVentaService.sincronizarVehiculoConInventario(inv);
        });
        
        // Redirigir manteniendo el filtro actual
        if (mostrarActivos != null && !mostrarActivos) {
            return "redirect:/inventario?mostrarActivos=false";
        }
        return "redirect:/inventario";
    }


    // 👉 Descargar en Excel SOLO lo filtrado
@GetMapping("/descargar-excel")
public ResponseEntity<byte[]> descargarExcel(
        @RequestParam(required = false, defaultValue = "") String marca,
        @RequestParam(required = false, defaultValue = "") String modelo,
        @RequestParam(required = false, defaultValue = "") String color,
        @RequestParam(required = false, defaultValue = "") String estadoLogistico
) throws Exception {
    
    // Usamos el método filtrado
    List<Inventario> inventarios = inventarioRepository.findByFiltros(marca, modelo, color, estadoLogistico);

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Inventario Filtrado");

    // Encabezados
    Row header = sheet.createRow(0);
    String[] columnas = {"ID", "Chasis", "Marca", "Modelo", "Año", "Color", "Motor", "Ubicación Actual", "Estado Logístico"};
    for (int i = 0; i < columnas.length; i++) {
        header.createCell(i).setCellValue(columnas[i]);
    }

    // Datos
    int rowIdx = 1;
    for (Inventario inv : inventarios) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(inv.getIdInventario() != null ? inv.getIdInventario() : 0);
        row.createCell(1).setCellValue(inv.getChasis() != null ? inv.getChasis() : "");
        row.createCell(2).setCellValue(inv.getMarca() != null ? inv.getMarca() : "");
        row.createCell(3).setCellValue(inv.getModelo() != null ? inv.getModelo() : "");
        row.createCell(4).setCellValue(inv.getAnio() != null ? inv.getAnio() : 0);
        row.createCell(5).setCellValue(inv.getColor() != null ? inv.getColor() : "");
        row.createCell(6).setCellValue(inv.getMotor() != null ? inv.getMotor() : "");
        row.createCell(7).setCellValue(inv.getUbicacionActual() != null ? inv.getUbicacionActual() : "");
        row.createCell(8).setCellValue(inv.getEstadoLogistico() != null ? inv.getEstadoLogistico() : "");
    }

    // Convertir a bytes
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    workbook.write(bos);
    workbook.close();

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario_filtrado.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bos.toByteArray());
}

    // 👉 Mostrar formulario de carga masiva
    @GetMapping("/carga-masiva")
    public String mostrarCargaMasiva() {
        return "inventario/carga-masiva";
    }

    // 👉 Procesar archivo de carga masiva
    @PostMapping("/carga-masiva")
    public String procesarCargaMasiva(@RequestParam("archivo") MultipartFile archivo, 
                                     RedirectAttributes redirectAttributes) {
        try {
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor seleccione un archivo");
                return "redirect:/inventario/carga-masiva";
            }

            ResultadoCargaInventarioDTO resultado = cargaMasivaInventarioService.procesarArchivo(archivo);
            
            redirectAttributes.addFlashAttribute("resultado", resultado);
            
            if (resultado.isExitoso()) {
                redirectAttributes.addFlashAttribute("success", resultado.getMensaje());
            } else {
                redirectAttributes.addFlashAttribute("warning", resultado.getMensaje());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el archivo: " + e.getMessage());
        }

        return "redirect:/inventario/carga-masiva";
    }

    // 👉 Descargar plantilla Excel para inventario
    @GetMapping("/plantilla-excel")
    public void descargarPlantillaExcel(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=plantilla_inventario.xlsx");
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario");
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("chasis");
        headerRow.createCell(1).setCellValue("marca");
        headerRow.createCell(2).setCellValue("modelo");
        headerRow.createCell(3).setCellValue("anio");
        headerRow.createCell(4).setCellValue("color");
        headerRow.createCell(5).setCellValue("motor");
        headerRow.createCell(6).setCellValue("ubicacion_actual");
        headerRow.createCell(7).setCellValue("estado_logistico");
        
        // Agregar filas de ejemplo
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("ABC123456");
        row1.createCell(1).setCellValue("Toyota");
        row1.createCell(2).setCellValue("Corolla");
        row1.createCell(3).setCellValue(2024);
        row1.createCell(4).setCellValue("Blanco");
        row1.createCell(5).setCellValue("1.8L");
        row1.createCell(6).setCellValue("Bodega Principal");
        row1.createCell(7).setCellValue("Disponible");
        
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("XYZ789012");
        row2.createCell(1).setCellValue("Honda");
        row2.createCell(2).setCellValue("Civic");
        row2.createCell(3).setCellValue(2024);
        row2.createCell(4).setCellValue("Negro");
        row2.createCell(5).setCellValue("2.0L");
        row2.createCell(6).setCellValue("Bodega Norte");
        row2.createCell(7).setCellValue("En Tránsito");
        
        // Ajustar ancho de columnas
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
        
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // 👉 Descargar plantilla CSV para inventario
    @GetMapping("/plantilla-csv")
    public void descargarPlantillaCSV(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=plantilla_inventario.csv");
        
        String csvContent = "chasis,marca,modelo,anio,color,motor,ubicacion_actual,estado_logistico\n" +
                           "ABC123456,Toyota,Corolla,2024,Blanco,1.8L,Bodega Principal,Disponible\n" +
                           "XYZ789012,Honda,Civic,2024,Negro,2.0L,Bodega Norte,En Tránsito\n";
        
        response.getWriter().write(csvContent);
    }

    // 👉 Mostrar formulario de reportes
    @GetMapping("/reportes")
    public String mostrarReportes(Model model) {
        model.addAttribute("filtro", new FiltroReporteDTO());
        return "inventario/reportes";
    }

    // 👉 Generar reporte PDF con filtros personalizados
    @PostMapping("/generar-reporte-pdf")
    public ResponseEntity<byte[]> generarReportePDF(@ModelAttribute FiltroReporteDTO filtro) {
        try {
            // Generar el reporte con estadísticas
            ReporteInventarioDTO reporte = reporteInventarioService.generarReporte(filtro);
            
            // Generar el PDF con gráficos
            byte[] pdfBytes = pdfGeneratorService.generarReportePDF(reporte);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_inventario.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 👉 Generar reporte PDF rápido (sin filtros)
    @GetMapping("/generar-reporte-pdf-rapido")
    public ResponseEntity<byte[]> generarReportePDFRapido() {
        try {
            FiltroReporteDTO filtro = new FiltroReporteDTO();
            filtro.setSoloActivos(true);
            
            ReporteInventarioDTO reporte = reporteInventarioService.generarReporte(filtro);
            byte[] pdfBytes = pdfGeneratorService.generarReportePDF(reporte);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_inventario_rapido.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
