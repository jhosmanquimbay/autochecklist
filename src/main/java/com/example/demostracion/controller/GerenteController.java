package com.example.demostracion.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;     
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

import com.example.demostracion.dto.ResultadoCargaInventarioDTO;
import com.example.demostracion.dto.DetalleContableVentaDTO;
import com.example.demostracion.dto.ResumenFinanciamientoDTO;
import com.example.demostracion.dto.ResumenContableDTO;
import com.example.demostracion.dto.SeguimientoFinanciamientoForm;
import com.example.demostracion.model.ContabilidadVenta;
import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Notificacion;
import com.example.demostracion.model.Novedad;
import com.example.demostracion.model.Pedido;
import com.example.demostracion.model.SolicitudFinanciamiento;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.model.Cliente;
import com.example.demostracion.model.SolicitudPrueba;
import com.example.demostracion.repository.ConductorRepository;
import com.example.demostracion.repository.ContabilidadVentaRepository;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.NotificacionRepository;
import com.example.demostracion.repository.NovedadRepository;
import com.example.demostracion.repository.PedidoRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import com.example.demostracion.repository.ClienteRepository;
import com.example.demostracion.repository.SolicitudPruebaRepository;
import com.example.demostracion.service.CargaMasivaInventarioService;
import com.example.demostracion.service.FinanciamientoService;
import com.example.demostracion.service.GerenteContabilidadService;
import com.example.demostracion.service.InventarioVentaService;
import com.example.demostracion.service.MensajeService;
import com.example.demostracion.service.VehiculoPublicacionService;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

@Controller
@RequestMapping("/gerente")
public class GerenteController {

    private static final Set<String> ESTADOS_INTERES = Set.of("pendiente", "contactado", "negociando");
    private static final Set<String> ESTADOS_VENTA = Set.of("vendido", "entregado");
    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_FECHA_REPORTE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_ES);

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final ConductorRepository conductorRepository;
    private final NovedadRepository novedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ContabilidadVentaRepository contabilidadVentaRepository;
    private final CargaMasivaInventarioService cargaMasivaInventarioService;
    private final InventarioVentaService inventarioVentaService;
    private final MensajeService mensajeService;
    private final VehiculoPublicacionService vehiculoPublicacionService;
    private final GerenteContabilidadService gerenteContabilidadService;
    private final FinanciamientoService financiamientoService;
    private final ClienteRepository clienteRepository;
    private final SolicitudPruebaRepository solicitudPruebaRepository;

    private static final String UPLOAD_DIR = "uploads/novedades/";

    public GerenteController(VehiculoRepository vehiculoRepository,
                             InventarioRepository inventarioRepository,
                             NotificacionRepository notificacionRepository,
                             ConductorRepository conductorRepository,
                             NovedadRepository novedadRepository,
                             UsuarioRepository usuarioRepository,
                             PedidoRepository pedidoRepository,
                             ContabilidadVentaRepository contabilidadVentaRepository,
                             CargaMasivaInventarioService cargaMasivaInventarioService,
                             InventarioVentaService inventarioVentaService,
                             MensajeService mensajeService,
                             VehiculoPublicacionService vehiculoPublicacionService,
                             GerenteContabilidadService gerenteContabilidadService,
                             FinanciamientoService financiamientoService,
                             ClienteRepository clienteRepository,
                             SolicitudPruebaRepository solicitudPruebaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.conductorRepository = conductorRepository;
        this.novedadRepository = novedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.contabilidadVentaRepository = contabilidadVentaRepository;
        this.cargaMasivaInventarioService = cargaMasivaInventarioService;
        this.inventarioVentaService = inventarioVentaService;
        this.mensajeService = mensajeService;
        this.vehiculoPublicacionService = vehiculoPublicacionService;
        this.gerenteContabilidadService = gerenteContabilidadService;
        this.financiamientoService = financiamientoService;
        this.clienteRepository = clienteRepository;
        this.solicitudPruebaRepository = solicitudPruebaRepository;
    }

    // ===============================
    // Dashboard ejecutivo
    // ===============================
    @GetMapping
    public String gerenteHome(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);

        List<Inventario> inventarios = inventarioRepository.findAll();
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<Vehiculo> vehiculosPublicacion = vehiculoPublicacionService.listarVehiculos("todos");

        long vehiculosActivos = inventarios.stream().filter(Inventario::isActivo).count();
        long clientesInteresados = pedidos.stream()
                .filter(p -> ESTADOS_INTERES.contains(normalizarEstado(p.getEstado())))
                .count();
        long interesadosSinAsignar = pedidos.stream()
            .filter(this::esOportunidadActiva)
            .filter(p -> p.getConductor() == null)
            .count();
        long leadsWebActivos = pedidos.stream()
            .filter(this::esOportunidadActiva)
            .filter(this::esOportunidadWeb)
            .count();

        YearMonth mesActual = YearMonth.now();

        long ventasTotales = pedidos.stream()
            .filter(p -> ESTADOS_VENTA.contains(normalizarEstado(p.getEstado())))
            .count();

        Map<Long, ContabilidadVenta> contabilidadPorPedido = cargarContabilidadPorPedido();

        double ingresosMes = pedidos.stream()
            .filter(p -> ESTADOS_VENTA.contains(normalizarEstado(p.getEstado())))
            .filter(p -> {
                LocalDateTime fechaComercial = resolverFechaComercial(p, contabilidadPorPedido.get(obtenerPedidoId(p)));
                return fechaComercial != null && YearMonth.from(fechaComercial).equals(mesActual);
            })
            .mapToDouble(p -> calcularIngresoPedido(p, contabilidadPorPedido.get(obtenerPedidoId(p))))
            .sum();

        double ingresosTotales = pedidos.stream()
            .mapToDouble(p -> calcularIngresoPedido(p, contabilidadPorPedido.get(obtenerPedidoId(p))))
            .sum();

        double ticketPromedio = ventasTotales == 0 ? 0.0 : ingresosTotales / ventasTotales;
        List<Map<String, Object>> resumenMensual = construirResumenMensual(pedidos, contabilidadPorPedido, 6);
        String periodoMesActual = String.format(LOCALE_ES, "%04d-%02d", mesActual.getYear(), mesActual.getMonthValue());
        List<DetalleContableVentaDTO> detalleContableMes = gerenteContabilidadService.listarDetalle(periodoMesActual, "cerradas");
        ResumenContableDTO resumenContableMes = gerenteContabilidadService.construirResumen(detalleContableMes);
        ResumenFinanciamientoDTO resumenFinanciamiento = financiamientoService.construirResumenGeneral();

        model.addAttribute("vehiculosActivos", vehiculosActivos);
        model.addAttribute("clientesInteresados", clientesInteresados);
        model.addAttribute("interesadosSinAsignar", interesadosSinAsignar);
        model.addAttribute("leadsWebActivos", leadsWebActivos);
        model.addAttribute("ventasTotales", ventasTotales);
        model.addAttribute("ingresosMes", formatearMoneda(ingresosMes));
        model.addAttribute("ingresosTotales", formatearMoneda(ingresosTotales));
        model.addAttribute("ticketPromedio", formatearMoneda(ticketPromedio));
        model.addAttribute("resumenMensual", resumenMensual);
        model.addAttribute("mesesLabels", resumenMensual.stream().map(m -> (String) m.get("mes")).toList());
        model.addAttribute("ventasMensuales", resumenMensual.stream().map(m -> ((Number) m.get("ventas")).longValue()).toList());
        model.addAttribute("ingresosMensuales", resumenMensual.stream().map(m -> ((Number) m.get("ingresos")).doubleValue()).toList());
        model.addAttribute("utilidadNetaMes", formatearMoneda(resumenContableMes.getUtilidadNeta()));
        model.addAttribute("comisionesMes", formatearMoneda(resumenContableMes.getComisiones()));
        model.addAttribute("reinversionMes", formatearMoneda(resumenContableMes.getReinversionSugerida()));
        model.addAttribute("pendientesLiquidacion", resumenContableMes.getPendientesLiquidacion());
        model.addAttribute("operacionesLiquidadasMes", resumenContableMes.getOperacionesLiquidadas());
        model.addAttribute("actualizadoEn", FORMATO_FECHA_REPORTE.format(LocalDateTime.now()));
        model.addAttribute("vehiculosMasSolicitados", topVehiculosSolicitados(pedidos, 5));
        model.addAttribute("rendimientoVendedores", construirRendimientoVendedores(pedidos));
        model.addAttribute("conteoPendientesPublicacion", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_PENDIENTE));
        model.addAttribute("conteoPublicadosPublicacion", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_PUBLICADO));
        model.addAttribute("conteoDevueltosPublicacion", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_DEVUELTO));
        model.addAttribute("vehiculosSeguimiento", vehiculosPublicacion.stream().limit(5).toList());
        model.addAttribute("resumenFinanciamientoGeneral", resumenFinanciamiento);
        model.addAttribute("financiamientosRecientes", financiamientoService.listarRecientes());

        // Estadísticas adicionales para el dashboard
        model.addAttribute("totalInventario", inventarioRepository.count());
        model.addAttribute("totalVehiculos", vehiculoRepository.count());
        model.addAttribute("novedadesPendientes",
                novedadRepository.findAll().stream()
                .filter(this::esNovedadAbierta)
                        .count());
        model.addAttribute("pedidosTotales", pedidos.size());

        return "gerente/gerente";
    }

    @GetMapping("/contabilidad")
    public String verContabilidad(@RequestParam(required = false) String periodo,
                                  @RequestParam(required = false, defaultValue = "cerradas") String estado,
                                  Model model,
                                  Authentication auth) {
        agregarDatosCorreo(model, auth);

        List<DetalleContableVentaDTO> detalleContable = gerenteContabilidadService.listarDetalle(periodo, estado);
        ResumenContableDTO resumenContable = gerenteContabilidadService.construirResumen(detalleContable);
        ResumenFinanciamientoDTO resumenFinanciamiento = financiamientoService.construirResumenGeneral();

        model.addAttribute("detalleContable", detalleContable);
        model.addAttribute("resumenContable", resumenContable);
        model.addAttribute("resumenFinanciamientoGeneral", resumenFinanciamiento);
        model.addAttribute("financiamientosSeguimiento", financiamientoService.listarSeguimientoOperativo());
        if (!model.containsAttribute("seguimientoFinanciamientoForm")) {
            model.addAttribute("seguimientoFinanciamientoForm", construirFormularioSeguimiento(financiamientoService.listarSeguimientoOperativo()));
        }
        model.addAttribute("periodoFiltro", periodo == null ? "" : periodo);
        model.addAttribute("estadoFiltroContable", gerenteContabilidadService.normalizarEstadoFiltro(estado));
        model.addAttribute("descripcionFiltroContable", gerenteContabilidadService.describirFiltro(periodo, estado));
        return "gerente/contabilidad";
    }

    @PostMapping("/contabilidad/financiamiento/seguimiento")
    public String actualizarSeguimientoFinanciamiento(@ModelAttribute("seguimientoFinanciamientoForm") SeguimientoFinanciamientoForm seguimientoForm,
                                                      Authentication auth,
                                                      RedirectAttributes redirectAttributes) {
        Usuario gerente = obtenerUsuarioAutenticado(auth);
        try {
            financiamientoService.actualizarSeguimiento(
                    seguimientoForm.getSolicitudId(),
                    seguimientoForm,
                    gerente != null ? gerente.getCorreo() : (auth != null ? auth.getName() : ""),
                    gerente != null ? gerente.getNombre() : "Gerencia");
            redirectAttributes.addFlashAttribute("success", "Seguimiento de financiación actualizado desde contabilidad.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("seguimientoFinanciamientoForm", seguimientoForm);
        }
        return "redirect:/gerente/contabilidad#desembolso";
    }

    @GetMapping("/contabilidad/pedido/{id}")
    public String editarContabilidadVenta(@PathVariable Long id,
                                          Model model,
                                          Authentication auth) {
        agregarDatosCorreo(model, auth);
        model.addAttribute("contabilidad", gerenteContabilidadService.prepararRegistro(id));
        return "gerente/contabilidad-form";
    }

    @PostMapping("/contabilidad/pedido/{id}")
    public String guardarContabilidadVenta(@PathVariable Long id,
                                           @ModelAttribute ContabilidadVenta contabilidad,
                                           Model model,
                                           Authentication auth,
                                           RedirectAttributes redirectAttributes) {
        try {
            gerenteContabilidadService.guardarRegistro(id, contabilidad);
        } catch (IllegalArgumentException e) {
            ContabilidadVenta vista = gerenteContabilidadService.prepararRegistro(id);
            vista.setPrecioPublicadoSnapshot(contabilidad.getPrecioPublicadoSnapshot());
            vista.setPrecioVentaFinal(contabilidad.getPrecioVentaFinal());
            vista.setCostoBase(contabilidad.getCostoBase());
            vista.setCostoAcondicionamiento(contabilidad.getCostoAcondicionamiento());
            vista.setCostoTraslado(contabilidad.getCostoTraslado());
            vista.setCostoAdministrativo(contabilidad.getCostoAdministrativo());
            vista.setGastoPublicacion(contabilidad.getGastoPublicacion());
            vista.setGastosCierre(contabilidad.getGastosCierre());
            vista.setPorcentajeComision(contabilidad.getPorcentajeComision());
            vista.setPorcentajeReinversion(contabilidad.getPorcentajeReinversion());
            vista.setNotas(contabilidad.getNotas());
            agregarDatosCorreo(model, auth);
            model.addAttribute("contabilidad", vista);
            model.addAttribute("error", e.getMessage());
            return "gerente/contabilidad-form";
        }

        redirectAttributes.addFlashAttribute("success", "Liquidación financiera guardada correctamente.");
        return "redirect:/gerente/contabilidad";
    }

    @GetMapping("/contabilidad/reporte.pdf")
    public ResponseEntity<byte[]> descargarReporteContablePdf(@RequestParam(required = false) String periodo,
                                                              @RequestParam(required = false, defaultValue = "cerradas") String estado) {
        byte[] pdf = gerenteContabilidadService.generarReportePdf(periodo, estado);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_contabilidad_ventas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ===============================
    // Vehiculos
    // ===============================
    @GetMapping("/vehiculos")
    public String listarVehiculos(@RequestParam(required = false, defaultValue = "todos") String estadoPublicacion,
                                  @RequestParam(required = false) Long resaltar,
                                  Model model,
                                  Authentication auth) {
        agregarDatosCorreo(model, auth);
        model.addAttribute("vehiculos", vehiculoPublicacionService.listarVehiculos(estadoPublicacion));
        model.addAttribute("estadoPublicacionFiltro", vehiculoPublicacionService.normalizarFiltro(estadoPublicacion));
        model.addAttribute("vehiculoResaltadoId", resaltar);
        model.addAttribute("conteoPendientesPublicacion", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_PENDIENTE));
        model.addAttribute("conteoPublicados", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_PUBLICADO));
        model.addAttribute("conteoDevueltos", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_DEVUELTO));
        model.addAttribute("conteoBorradores", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_BORRADOR));
        model.addAttribute("conteoDespublicados", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_DESPUBLICADO));
        return "gerente/vehiculos/listar";
    }

    @GetMapping("/vehiculos/crear")
    public String crearVehiculoForm(@RequestParam(required = false) Long inventarioId, Model model, Authentication auth) {
        prepararFormularioVehiculoGerente(model, vehiculoPublicacionService.prepararNuevoVehiculoGerencia(inventarioId), auth);
        return "gerente/vehiculos/form";
    }

    @PostMapping("/vehiculos/crear")
    public String guardarVehiculo(@ModelAttribute Vehiculo vehiculo,
                                  @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                  @RequestParam(value = "imagenFile2", required = false) MultipartFile imagenFile2,
                                  @RequestParam(value = "imagenFile3", required = false) MultipartFile imagenFile3,
                                  @RequestParam(value = "imagenFile4", required = false) MultipartFile imagenFile4,
                                  Model model,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) throws IOException {
        Vehiculo creado;
        try {
            creado = vehiculoPublicacionService.crearDesdeGerencia(vehiculo, imagenFile, imagenFile2, imagenFile3, imagenFile4);
        } catch (IllegalArgumentException e) {
            prepararFormularioVehiculoGerente(model, vehiculo, auth);
            model.addAttribute("error", e.getMessage());
            return "gerente/vehiculos/form";
        }
        redirectAttributes.addFlashAttribute("success", "Vehículo cargado correctamente y enviado a revisión de publicación.");
        redirectAttributes.addAttribute("estadoPublicacion", Vehiculo.ESTADO_PUBLICACION_PENDIENTE);
        redirectAttributes.addAttribute("resaltar", creado.getIdVehiculo());
        return "redirect:/gerente/vehiculos";
    }

    @GetMapping("/vehiculos/editar/{id}")
    public String editarVehiculo(@PathVariable Long id, Model model, Authentication auth) {
        Vehiculo vehiculo = vehiculoPublicacionService.prepararVehiculoExistenteParaFormulario(
                vehiculoPublicacionService.obtenerVehiculo(id));
        prepararFormularioVehiculoGerente(model, vehiculo, auth);
        return "gerente/vehiculos/form";
    }

    @PostMapping("/vehiculos/editar/{id}")
    public String actualizarVehiculo(@PathVariable Long id,
                                     @ModelAttribute Vehiculo vehiculo,
                                     @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                     @RequestParam(value = "imagenFile2", required = false) MultipartFile imagenFile2,
                                     @RequestParam(value = "imagenFile3", required = false) MultipartFile imagenFile3,
                                     @RequestParam(value = "imagenFile4", required = false) MultipartFile imagenFile4,
                                     Model model,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) throws IOException {
        Vehiculo actualizado;
        try {
            actualizado = vehiculoPublicacionService.actualizarDesdeGerencia(id, vehiculo, imagenFile, imagenFile2, imagenFile3, imagenFile4);
        } catch (IllegalArgumentException e) {
            vehiculo.setIdVehiculo(id);
            prepararFormularioVehiculoGerente(model, vehiculo, auth);
            model.addAttribute("error", e.getMessage());
            return "gerente/vehiculos/form";
        }
        redirectAttributes.addFlashAttribute("success", "Los cambios del vehículo quedaron listos y fueron reenviados a revisión de admin.");
        redirectAttributes.addAttribute("estadoPublicacion", Vehiculo.ESTADO_PUBLICACION_PENDIENTE);
        redirectAttributes.addAttribute("resaltar", actualizado.getIdVehiculo());
        return "redirect:/gerente/vehiculos";
    }

    @PostMapping("/vehiculos/eliminar/{id}")
    public String eliminarVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vehiculo vehiculo = vehiculoPublicacionService.moverABorrador(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo retirado del flujo de publicación.");
        redirectAttributes.addAttribute("estadoPublicacion", Vehiculo.ESTADO_PUBLICACION_BORRADOR);
        redirectAttributes.addAttribute("resaltar", vehiculo.getIdVehiculo());
        return "redirect:/gerente/vehiculos";
    }

    @GetMapping("/vehiculos/imagen/{id}")
    public ResponseEntity<byte[]> obtenerImagenVehiculoGerente(@PathVariable Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);
        if (vehiculo == null || vehiculo.getImagen() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(vehiculo.getImagen());
    }

    private void prepararFormularioVehiculoGerente(Model model, Vehiculo vehiculo, Authentication auth) {
        agregarDatosCorreo(model, auth);
        model.addAttribute("vehiculo", vehiculoPublicacionService.prepararVehiculoExistenteParaFormulario(vehiculo));
        model.addAttribute("inventarios", vehiculoPublicacionService.listarInventariosSeleccionables());
    }

    // ===============================
    // Inventario
    // ===============================
    @GetMapping("/inventario")
    public String listarInventario(@RequestParam(required = false, defaultValue = "todos") String estado,
                                   Model model,
                                   Authentication auth) {
        agregarDatosCorreo(model, auth);

        String filtroEstado = estado == null ? "todos" : estado.trim().toLowerCase(Locale.ROOT);
        List<Inventario> inventarios;
        switch (filtroEstado) {
            case "activos" -> inventarios = inventarioRepository.findByActivoTrue();
            case "inactivos" -> inventarios = inventarioRepository.findByActivoFalse();
            default -> {
                filtroEstado = "todos";
                inventarios = inventarioRepository.findAll();
            }
        }

        model.addAttribute("inventarios", inventarios);
        model.addAttribute("filtroEstado", filtroEstado);
        agregarResumenInventario(model, inventarioRepository.findAll());
        return "gerente/inventario/listar";
    }

    @GetMapping("/inventario/inactivos")
    public String listarInventarioInactivo() {
        return "redirect:/gerente/inventario?estado=inactivos";
    }

    @GetMapping("/inventario/crear")
    public String crearInventarioForm(Model model, Authentication auth) {
        prepararFormularioInventarioGerente(model, new Inventario(), auth);
        return "gerente/inventario/form";
    }

    @PostMapping("/inventario/crear")
    public String guardarInventario(@ModelAttribute Inventario inventario, Model model, Authentication auth) {
        try {
            validarInventarioGerente(inventario, null);
        } catch (IllegalArgumentException e) {
            prepararFormularioInventarioGerente(model, inventario, auth);
            model.addAttribute("error", e.getMessage());
            return "gerente/inventario/form";
        }
        inventario.setActivo(true);
        if (inventario.getCantidadDisponible() == null || inventario.getCantidadDisponible() < 1) {
            inventario.setCantidadDisponible(1);
        }
        inventario = inventarioRepository.save(inventario);
        sincronizarVehiculoConInventario(inventario);
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/inventario/editar/{id}")
    public String editarInventario(@PathVariable Long id, Model model, Authentication auth) {
        Inventario inventario = inventarioRepository.findById(id).orElseThrow();
        prepararFormularioInventarioGerente(model, inventario, auth);
        return "gerente/inventario/form";
    }

    @PostMapping("/inventario/editar/{id}")
    public String actualizarInventario(@PathVariable Long id,
                                       @ModelAttribute Inventario inventario,
                                       Model model,
                                       Authentication auth) {
        inventario.setIdInventario(id);
        try {
            validarInventarioGerente(inventario, id);
        } catch (IllegalArgumentException e) {
            prepararFormularioInventarioGerente(model, inventario, auth);
            model.addAttribute("error", e.getMessage());
            return "gerente/inventario/form";
        }
        if (inventario.getCantidadDisponible() == null || inventario.getCantidadDisponible() < 1) {
            inventario.setCantidadDisponible(1);
        }
        inventario = inventarioRepository.save(inventario);
        sincronizarVehiculoConInventario(inventario);
        return "redirect:/gerente/inventario";
    }

    @PostMapping("/inventario/cambiar-estado/{id}")
    public String cambiarEstadoInventario(@PathVariable Long id,
                                          @RequestParam(required = false, defaultValue = "todos") String estado) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        inventario.setActivo(!inventario.isActivo());
        inventario = inventarioRepository.save(inventario);
        sincronizarVehiculoConInventario(inventario);

        return "redirect:/gerente/inventario?estado=" + estado;
    }

    @GetMapping("/inventario/carga-masiva")
    public String mostrarCargaMasivaGerente(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        return "gerente/inventario/carga-masiva";
    }

    @PostMapping("/inventario/carga-masiva")
    public String procesarCargaMasivaGerente(@RequestParam("archivo") MultipartFile archivo,
                                             RedirectAttributes redirectAttributes) {
        try {
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor seleccione un archivo");
                return "redirect:/gerente/inventario/carga-masiva";
            }

            ResultadoCargaInventarioDTO resultado = cargaMasivaInventarioService.procesarArchivo(archivo);
            redirectAttributes.addFlashAttribute("resultado", resultado);
            redirectAttributes.addFlashAttribute(resultado.isExitoso() ? "success" : "warning", resultado.getMensaje());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar archivo: " + e.getMessage());
        }

        return "redirect:/gerente/inventario/carga-masiva";
    }

    // ===============================
    // Clientes interesados (CRM)
    // ===============================
    @GetMapping("/clientes/interesados")
    public String listarInteresados(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("interesados", construirVistaInteresados(pedidos));
        model.addAttribute("resumenEstados", resumenEstados(pedidos));
        model.addAttribute("vehiculosMasSolicitados", topVehiculosSolicitados(pedidos, 10));
        model.addAttribute("rendimientoVendedores", construirRendimientoVendedores(pedidos));
        model.addAttribute("interesadosSinAsignar", pedidos.stream().filter(this::esOportunidadActiva).filter(p -> p.getConductor() == null).count());
        model.addAttribute("leadsWebActivos", pedidos.stream().filter(this::esOportunidadActiva).filter(this::esOportunidadWeb).count());
        model.addAttribute("vendedoresDisponibles", conductorRepository.findAll().stream()
            .sorted(Comparator.comparing(c -> c.getNombre() == null ? "" : c.getNombre().toLowerCase(Locale.ROOT)))
            .toList());

        return "gerente/clientes/interesados";
    }

    @PostMapping("/clientes/interesados/{id}/estado")
        public String cambiarEstadoInteresado(@PathVariable Long id,
                          @RequestParam String estado,
                          @RequestParam(required = false) String conductorId,
                          RedirectAttributes redirectAttributes) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interesado no encontrado"));
        String estadoAnterior = pedido.getEstado();

        String estadoNormalizado = normalizarEstado(estado);
        if (!Set.of("pendiente", "contactado", "negociando", "vendido", "perdido", "entregado").contains(estadoNormalizado)) {
            throw new IllegalArgumentException("Estado no permitido: " + estado);
        }

        pedido.setEstado(estadoNormalizado);
        pedido.setConductor(resolverConductor(conductorId));
        pedido = pedidoRepository.save(pedido);
        inventarioVentaService.sincronizarStockPorEstado(pedido, estadoAnterior);
        sincronizarClienteDesdeOportunidad(pedido);

        redirectAttributes.addFlashAttribute("success", "La oportunidad se actualizó correctamente.");

        return "redirect:/gerente/clientes/interesados";
    }

    // ===============================
    // Reportes gerenciales
    // ===============================
    @GetMapping("/reportes/pdf")
    public ResponseEntity<byte[]> descargarReporteGerencialPdf() throws IOException {
        List<Inventario> inventarios = inventarioRepository.findAll();
        List<Pedido> pedidos = pedidoRepository.findAll();
        Map<Long, ContabilidadVenta> contabilidadPorPedido = cargarContabilidadPorPedido();

        long vehiculosDisponibles = inventarios.stream().filter(Inventario::isActivo).count();
        long interesadosRegistrados = pedidos.stream().filter(p -> ESTADOS_INTERES.contains(normalizarEstado(p.getEstado()))).count();
        long ventasRealizadas = pedidos.stream().filter(p -> ESTADOS_VENTA.contains(normalizarEstado(p.getEstado()))).count();
        double ingresosTotales = pedidos.stream()
            .mapToDouble(p -> calcularIngresoPedido(p, contabilidadPorPedido.get(obtenerPedidoId(p))))
            .sum();
        List<Map<String, Object>> resumenMensual = construirResumenMensual(pedidos, contabilidadPorPedido, 12);

        String modeloMasSolicitado = topVehiculosSolicitados(pedidos, 1).stream()
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse("Sin datos");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        DeviceRgb colorMarca = new DeviceRgb(10, 29, 55);
        DeviceRgb colorAcento = new DeviceRgb(245, 166, 35);

        document.add(new Paragraph("REPORTE EJECUTIVO DE OPERACION")
            .setBold()
            .setFontSize(18)
            .setFontColor(colorMarca)
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Generado: " + FORMATO_FECHA_REPORTE.format(LocalDateTime.now()))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.DARK_GRAY)
            .setMarginBottom(15));

        Table kpis = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1})).useAllAvailableWidth();
        kpis.addCell(celdaKpi("Vehiculos disponibles", String.valueOf(vehiculosDisponibles), colorAcento));
        kpis.addCell(celdaKpi("Interesados", String.valueOf(interesadosRegistrados), colorAcento));
        kpis.addCell(celdaKpi("Ventas cerradas", String.valueOf(ventasRealizadas), colorAcento));
        kpis.addCell(celdaKpi("Ingresos", formatearMoneda(ingresosTotales), colorAcento));
        document.add(kpis);

        document.add(new Paragraph("\nModelo mas solicitado: " + modeloMasSolicitado)
            .setBold()
            .setFontColor(colorMarca));

        document.add(new Paragraph("\nResumen mensual de ventas e ingresos")
            .setBold()
            .setFontSize(13)
            .setFontColor(colorMarca));

        Table tablaMensual = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1})).useAllAvailableWidth();
        tablaMensual.addHeaderCell(celdaHeader("Mes", colorMarca));
        tablaMensual.addHeaderCell(celdaHeader("Interesados", colorMarca));
        tablaMensual.addHeaderCell(celdaHeader("Ventas", colorMarca));
        tablaMensual.addHeaderCell(celdaHeader("Ingresos", colorMarca));

        for (Map<String, Object> fila : resumenMensual) {
            tablaMensual.addCell(celdaDato((String) fila.get("mes")));
            tablaMensual.addCell(celdaDato(String.valueOf(((Number) fila.get("interesados")).longValue())));
            tablaMensual.addCell(celdaDato(String.valueOf(((Number) fila.get("ventas")).longValue())));
            tablaMensual.addCell(celdaDato(formatearMoneda(((Number) fila.get("ingresos")).doubleValue())));
        }
        document.add(tablaMensual);

        byte[] chart = generarGraficoVentasIngresos(resumenMensual);
        if (chart.length > 0) {
            Image grafico = new Image(ImageDataFactory.create(chart));
            grafico.setAutoScale(true);
            grafico.setMarginTop(15);
            document.add(grafico);
        }

        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=informe_gerencial.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/reportes/excel")
    public ResponseEntity<byte[]> descargarReporteGerencialExcel() throws IOException {
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        Map<Long, ContabilidadVenta> contabilidadPorPedido = cargarContabilidadPorPedido();
        List<Map<String, Object>> resumenMensual = construirResumenMensual(pedidos, contabilidadPorPedido, 12);

        Workbook workbook = new XSSFWorkbook();
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

        Sheet detalle = workbook.createSheet("Detalle Ventas");
        Row headerDetalle = detalle.createRow(0);
        String[] columnasDetalle = {"Cliente", "Vehiculo", "Estado", "Fecha", "Vendedor", "Ingreso"};
        for (int i = 0; i < columnasDetalle.length; i++) {
            headerDetalle.createCell(i).setCellValue(columnasDetalle[i]);
            headerDetalle.getCell(i).setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Pedido pedido : pedidos) {
            Row row = detalle.createRow(rowNum++);
            row.createCell(0).setCellValue(extraerCliente(pedido));
            row.createCell(1).setCellValue(extraerVehiculo(pedido));
            row.createCell(2).setCellValue(pedido.getEstado() != null ? pedido.getEstado().toUpperCase(Locale.ROOT) : "PENDIENTE");
            row.createCell(3).setCellValue(pedido.getFechaCreacion() != null ? pedido.getFechaCreacion().toString() : "");
            row.createCell(4).setCellValue(pedido.getConductor() != null ? pedido.getConductor().getNombre() : "Sin asignar");
            var ingresoCell = row.createCell(5);
            ingresoCell.setCellValue(calcularIngresoPedido(pedido, contabilidadPorPedido.get(obtenerPedidoId(pedido))));
            ingresoCell.setCellStyle(moneyStyle);
        }

        for (int i = 0; i < columnasDetalle.length; i++) {
            detalle.autoSizeColumn(i);
        }

        Sheet resumen = workbook.createSheet("Resumen Mensual");
        Row headerResumen = resumen.createRow(0);
        String[] columnasResumen = {"Mes", "Interesados", "Ventas", "Ingresos"};
        for (int i = 0; i < columnasResumen.length; i++) {
            headerResumen.createCell(i).setCellValue(columnasResumen[i]);
            headerResumen.getCell(i).setCellStyle(headerStyle);
        }

        int resumenRow = 1;
        for (Map<String, Object> fila : resumenMensual) {
            Row row = resumen.createRow(resumenRow++);
            row.createCell(0).setCellValue((String) fila.get("mes"));
            row.createCell(1).setCellValue(((Number) fila.get("interesados")).longValue());
            row.createCell(2).setCellValue(((Number) fila.get("ventas")).longValue());
            var ingresoCell = row.createCell(3);
            ingresoCell.setCellValue(((Number) fila.get("ingresos")).doubleValue());
            ingresoCell.setCellStyle(moneyStyle);
        }

        for (int i = 0; i < columnasResumen.length; i++) {
            resumen.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_interesados.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(baos.toByteArray());
    }

    // ===============================
    // Vendedores
    // ===============================
    @GetMapping("/vendedores")
    public String gestionVendedores(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        model.addAttribute("rendimientoVendedores", construirRendimientoVendedores(pedidoRepository.findAll()));
        model.addAttribute("vendedores", conductorRepository.findAll());
        return "gerente/clientes/interesados";
    }

    // ===============================
    // Notificaciones
    // ===============================
    @GetMapping("/notificaciones")
    public String listarNotificaciones(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) Long conductorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model,
            Authentication auth) {

        agregarDatosCorreo(model, auth);

        List<Notificacion> notificaciones = new ArrayList<>(notificacionRepository.findAll());

        if (leida != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.isLeida() == leida)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (conductorId != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.getConductor() != null && n.getConductor().getIdConductor().equals(conductorId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (desde != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.getFecha() != null && !n.getFecha().toLocalDate().isBefore(desde))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (hasta != null) {
            notificaciones = notificaciones.stream()
                    .filter(n -> n.getFecha() != null && !n.getFecha().toLocalDate().isAfter(hasta))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        List<Notificacion> alertasInteligentes = construirAlertasInteligentes();
        notificaciones.addAll(0, alertasInteligentes);

        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("conductores", conductorRepository.findAll());
        model.addAttribute("alertasInteligentes", alertasInteligentes);
        return "gerente/notificacion/lista";
    }

    @GetMapping("/notificaciones/nuevo")
    public String crearNotificacionForm(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        model.addAttribute("notificacion", new Notificacion());
        model.addAttribute("conductores", conductorRepository.findAll());
        return "gerente/notificacion/form";
    }

    @PostMapping("/notificaciones/guardar")
    public String guardarNotificacion(@ModelAttribute Notificacion notificacion) {
        if (notificacion.getConductor() != null && notificacion.getConductor().getIdConductor() != null) {
            Conductor conductor = conductorRepository.findById(notificacion.getConductor().getIdConductor())
                    .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
            notificacion.setConductor(conductor);
        }
        notificacionRepository.save(notificacion);
        return "redirect:/gerente/notificaciones";
    }

    @GetMapping("/notificaciones/editar/{id}")
    public String editarNotificacion(@PathVariable Long id, Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        Notificacion notificacion = notificacionRepository.findById(id).orElseThrow();
        model.addAttribute("notificacion", notificacion);
        model.addAttribute("conductores", conductorRepository.findAll());
        return "gerente/notificacion/form";
    }

    @PostMapping("/notificaciones/editar/{id}")
    public String actualizarNotificacion(@PathVariable Long id, @ModelAttribute Notificacion notificacion) {
        notificacion.setIdNotificacion(id);

        if (notificacion.getConductor() != null && notificacion.getConductor().getIdConductor() != null) {
            Conductor conductor = conductorRepository.findById(notificacion.getConductor().getIdConductor())
                    .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
            notificacion.setConductor(conductor);
        }

        notificacionRepository.save(notificacion);
        return "redirect:/gerente/notificaciones";
    }

    @PostMapping("/notificaciones/eliminar/{id}")
    public String eliminarNotificacion(@PathVariable Long id) {
        notificacionRepository.deleteById(id);
        return "redirect:/gerente/notificaciones";
    }

    // ===============================
    // Novedades (CRUD)
    // ===============================
    @GetMapping("/novedades")
    public String listarNovedades(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        List<Novedad> novedades = novedadRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Novedad n) -> esNovedadAbierta(n) ? 0 : 1)
                        .thenComparing((Novedad n) -> pesoPrioridadNovedad(n.getPrioridad()), Comparator.reverseOrder())
                        .thenComparing(Novedad::getFechaReporte, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("novedades", novedades);
        model.addAttribute("novedadesAbiertas", novedades.stream().filter(this::esNovedadAbierta).count());
        model.addAttribute("novedadesEnRevision", novedades.stream()
                .filter(n -> "en_revision".equals(normalizarEstadoNovedad(n.getEstado())))
                .count());
        model.addAttribute("novedadesGarantia", novedades.stream()
                .filter(n -> Boolean.TRUE.equals(n.getAplicaGarantia()))
                .filter(this::esNovedadAbierta)
                .count());
        model.addAttribute("novedadesCriticas", novedades.stream().filter(this::esNovedadCritica).count());
        return "gerente/novedades/listar";
    }

    @GetMapping("/novedades/crear")
    public String crearNovedadForm(Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        Novedad novedad = new Novedad();
        novedad.setEstado("pendiente");
        novedad.setPrioridad("media");
        novedad.setOrigenReporte("recepcion_tienda");
        novedad.setAplicaGarantia(Boolean.FALSE);
        prepararFormularioNovedad(model, novedad);
        return "gerente/novedades/form";
    }

    @PostMapping("/novedades/guardar")
    public String guardarNovedad(@ModelAttribute Novedad novedad,
                                 @RequestParam("file") MultipartFile file,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) throws IOException {
        novedad.setUsuario(obtenerUsuarioAutenticado(auth));
        novedad.setEstado(normalizarEstadoNovedad(novedad.getEstado()));
        novedad.setPrioridad(normalizarPrioridadNovedad(novedad.getPrioridad()));
        novedad.setFechaReporte(LocalDateTime.now());
        novedad.setFechaGestion(LocalDateTime.now());
        if (novedad.getAplicaGarantia() == null) {
            novedad.setAplicaGarantia(Boolean.FALSE);
        }
        completarDatosVehiculoNovedad(novedad);

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            novedad.setEvidencia(fileName);
        }
        novedadRepository.save(novedad);
        redirectAttributes.addFlashAttribute("success", "La novedad fue reportada y quedó disponible para gestión gerencial.");
        return "redirect:/gerente/novedades";
    }

    @GetMapping("/novedades/editar/{id}")
    public String editarNovedadForm(@PathVariable Long id, Model model, Authentication auth) {
        agregarDatosCorreo(model, auth);
        Novedad novedad = novedadRepository.findById(id).orElseThrow();
        prepararFormularioNovedad(model, novedad);
        return "gerente/novedades/form";
    }

    @PostMapping("/novedades/actualizar/{id}")
    public String actualizarNovedad(@PathVariable Long id,
                                    @ModelAttribute Novedad novedad,
                                    @RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) throws IOException {
        Novedad existente = novedadRepository.findById(id).orElseThrow();

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            existente.setEvidencia(fileName);
        }

        existente.setTipoNovedad(novedad.getTipoNovedad());
        existente.setDescripcion(novedad.getDescripcion());
        existente.setEstado(normalizarEstadoNovedad(novedad.getEstado()));
        existente.setVehiculoChasis(novedad.getVehiculoChasis());
        existente.setOrigenReporte(novedad.getOrigenReporte());
        existente.setPrioridad(normalizarPrioridadNovedad(novedad.getPrioridad()));
        existente.setAplicaGarantia(Boolean.TRUE.equals(novedad.getAplicaGarantia()));
        existente.setAccionRequerida(novedad.getAccionRequerida());
        existente.setObservacionGerente(novedad.getObservacionGerente());
        existente.setFechaGestion(LocalDateTime.now());
        completarDatosVehiculoNovedad(existente);

        novedadRepository.save(existente);
        redirectAttributes.addFlashAttribute("success", "La novedad fue actualizada correctamente.");
        return "redirect:/gerente/novedades";
    }

    @PostMapping("/novedades/{id}/estado")
    public String cambiarEstadoNovedad(@PathVariable Long id,
                                       @RequestParam String estado,
                                       RedirectAttributes redirectAttributes) {
        Novedad novedad = novedadRepository.findById(id).orElseThrow();
        novedad.setEstado(normalizarEstadoNovedad(estado));
        novedad.setFechaGestion(LocalDateTime.now());
        novedadRepository.save(novedad);
        redirectAttributes.addFlashAttribute("success", "El estado de la novedad fue actualizado.");
        return "redirect:/gerente/novedades";
    }

    @PostMapping("/novedades/eliminar/{id}")
    public String eliminarNovedad(@PathVariable Long id) {
        novedadRepository.deleteById(id);
        return "redirect:/gerente/novedades";
    }

    private void agregarDatosCorreo(Model model, Authentication auth) {
        if (auth == null) {
            return;
        }

        usuarioRepository.findByCorreo(auth.getName()).ifPresent(usuario -> {
            model.addAttribute("usuarioId", usuario.getIdUsuario());
            model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
            model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
        });
    }

    private String normalizarEstado(String estado) {
        return estado == null ? "pendiente" : estado.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarEstadoNovedad(String estado) {
        if (estado == null || estado.isBlank()) {
            return "pendiente";
        }

        String valor = estado.trim().toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace(' ', '_');

        return switch (valor) {
            case "en_revision", "revision" -> "en_revision";
            case "gestion_garantia", "garantia" -> "gestion_garantia";
            case "resuelto", "cerrado" -> "resuelto";
            default -> "pendiente";
        };
    }

    private String normalizarPrioridadNovedad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            return "media";
        }

        String valor = prioridad.trim().toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");

        return switch (valor) {
            case "baja", "media", "alta", "critica" -> valor;
            default -> "media";
        };
    }

    private int pesoPrioridadNovedad(String prioridad) {
        return switch (normalizarPrioridadNovedad(prioridad)) {
            case "critica" -> 4;
            case "alta" -> 3;
            case "media" -> 2;
            default -> 1;
        };
    }

    private boolean esNovedadAbierta(Novedad novedad) {
        return !"resuelto".equals(normalizarEstadoNovedad(novedad.getEstado()));
    }

    private boolean esNovedadCritica(Novedad novedad) {
        return esNovedadAbierta(novedad) && pesoPrioridadNovedad(novedad.getPrioridad()) >= 3;
    }

    private void prepararFormularioNovedad(Model model, Novedad novedad) {
        model.addAttribute("novedad", novedad);
        model.addAttribute("inventariosDisponibles", inventarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Inventario::getMarca, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Inventario::getModelo, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList());
    }

    private SeguimientoFinanciamientoForm construirFormularioSeguimiento(List<SolicitudFinanciamiento> solicitudes) {
        SeguimientoFinanciamientoForm formulario = new SeguimientoFinanciamientoForm();
        if (solicitudes.isEmpty()) {
            formulario.setCrearNegocio(Boolean.FALSE);
            formulario.setEstadoDocumental("PENDIENTE");
            formulario.setEtapaProceso("RADICACION_PENDIENTE");
            return formulario;
        }

        SolicitudFinanciamiento base = solicitudes.get(0);
        formulario.setSolicitudId(base.getIdSolicitudFinanciamiento());
        formulario.setCrearNegocio(base.isNegocioCreado());
        formulario.setEstadoDocumental(base.getEstadoDocumental());
        formulario.setEtapaProceso(base.getEtapaProceso());
        formulario.setEntidadFinanciera(base.getEntidadFinanciera());
        formulario.setMontoDesembolsado(base.getMontoDesembolsado());
        formulario.setFechaDesembolsoProgramada(base.getFechaDesembolsoProgramada());
        return formulario;
    }

    private Usuario obtenerUsuarioAutenticado(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
    }

    private void completarDatosVehiculoNovedad(Novedad novedad) {
        if (novedad.getVehiculoChasis() == null || novedad.getVehiculoChasis().isBlank()) {
            novedad.setVehiculoReferencia("Sin vehiculo asociado");
            return;
        }

        inventarioRepository.findByChasis(novedad.getVehiculoChasis()).ifPresentOrElse(inventario -> {
            String marca = inventario.getMarca() != null ? inventario.getMarca() : "";
            String modelo = inventario.getModelo() != null ? inventario.getModelo() : "";
            String referencia = (marca + " " + modelo).trim();
            novedad.setVehiculoReferencia(referencia.isBlank() ? inventario.getChasis() : referencia);
        }, () -> novedad.setVehiculoReferencia(novedad.getVehiculoChasis()));
    }

    private String extraerVehiculo(Pedido pedido) {
        if (pedido.getVehiculo() == null) {
            return "Sin vehiculo";
        }

        String marca = pedido.getVehiculo().getMarca() != null ? pedido.getVehiculo().getMarca() : "";
        String modelo = pedido.getVehiculo().getModelo() != null ? pedido.getVehiculo().getModelo() : "";
        String vehiculo = (marca + " " + modelo).trim();

        return vehiculo.isBlank() ? "Sin vehiculo" : vehiculo;
    }

    private String extraerCliente(Pedido pedido) {
        return pedido.getDescripcion() != null && !pedido.getDescripcion().isBlank()
                ? pedido.getDescripcion()
                : "Cliente sin nombre";
    }

    private List<Map<String, Object>> construirVistaInteresados(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(pedido -> {
                    Cliente cliente = buscarClienteRelacionado(pedido);
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("idPedido", pedido.getIdPedido());
                    fila.put("nombreCliente", extraerNombreCliente(pedido, cliente));
                    fila.put("correoCliente", extraerCorreoCliente(pedido, cliente));
                    fila.put("telefonoCliente", extraerTelefonoCliente(pedido, cliente));
                    fila.put("mensajeCliente", extraerMensajeCliente(pedido));
                    fila.put("vehiculo", extraerVehiculo(pedido));
                    fila.put("vendedor", pedido.getConductor() != null && pedido.getConductor().getNombre() != null
                            ? pedido.getConductor().getNombre()
                            : "Sin vendedor");
                    fila.put("conductorId", pedido.getConductor() != null ? pedido.getConductor().getIdConductor() : null);
                    fila.put("fechaCreacion", pedido.getFechaCreacion());
                    fila.put("estado", normalizarEstado(pedido.getEstado()));
                    fila.put("origen", esOportunidadWeb(pedido) ? "Web" : "Interno");
                    return fila;
                })
                .toList();
    }

    private Cliente buscarClienteRelacionado(Pedido pedido) {
        if (pedido.getClienteUsuario() != null) {
            if (pedido.getClienteUsuario().getCorreo() != null && !pedido.getClienteUsuario().getCorreo().isBlank()) {
                Cliente cliente = clienteRepository.findByCorreo(pedido.getClienteUsuario().getCorreo().trim().toLowerCase(Locale.ROOT)).orElse(null);
                if (cliente != null) {
                    return cliente;
                }
            }
            if (pedido.getClienteUsuario().getCedula() != null && !pedido.getClienteUsuario().getCedula().isBlank()) {
                Cliente cliente = clienteRepository.findByCedula(pedido.getClienteUsuario().getCedula().trim()).orElse(null);
                if (cliente != null) {
                    return cliente;
                }
            }
        }

        String correo = extraerValorDescripcion(pedido.getDescripcion(), "Correo: ");
        if (correo != null && !correo.isBlank()) {
            return clienteRepository.findByCorreo(correo.trim().toLowerCase(Locale.ROOT)).orElse(null);
        }

        return null;
    }

    private String extraerNombreCliente(Pedido pedido, Cliente cliente) {
        if (pedido.getClienteUsuario() != null && pedido.getClienteUsuario().getNombre() != null
                && !pedido.getClienteUsuario().getNombre().isBlank()) {
            return pedido.getClienteUsuario().getNombre();
        }
        if (cliente != null && cliente.getNombre() != null && !cliente.getNombre().isBlank()) {
            return cliente.getNombre();
        }

        String descripcion = extraerCliente(pedido);
        if (descripcion.startsWith("[WEB]")) {
            descripcion = descripcion.substring(5).trim();
        }
        int separador = descripcion.indexOf(" | ");
        return separador >= 0 ? descripcion.substring(0, separador).trim() : descripcion.trim();
    }

    private String extraerCorreoCliente(Pedido pedido, Cliente cliente) {
        if (pedido.getClienteUsuario() != null && pedido.getClienteUsuario().getCorreo() != null
                && !pedido.getClienteUsuario().getCorreo().isBlank()) {
            return pedido.getClienteUsuario().getCorreo();
        }
        if (cliente != null && cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
            return cliente.getCorreo();
        }
        return extraerValorDescripcion(pedido.getDescripcion(), "Correo: ");
    }

    private String extraerTelefonoCliente(Pedido pedido, Cliente cliente) {
        if (pedido.getClienteUsuario() != null && pedido.getClienteUsuario().getTelefono() != null
                && !pedido.getClienteUsuario().getTelefono().isBlank()) {
            return pedido.getClienteUsuario().getTelefono();
        }
        if (cliente != null && cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
            return cliente.getTelefono();
        }
        return extraerValorDescripcion(pedido.getDescripcion(), "Tel: ");
    }

    private String extraerMensajeCliente(Pedido pedido) {
        return extraerValorDescripcion(pedido.getDescripcion(), "Nota: ");
    }

    private String extraerValorDescripcion(String descripcion, String etiqueta) {
        if (descripcion == null || descripcion.isBlank() || etiqueta == null || etiqueta.isBlank()) {
            return null;
        }

        int inicio = descripcion.indexOf(etiqueta);
        if (inicio < 0) {
            return null;
        }

        int valorInicio = inicio + etiqueta.length();
        int fin = descripcion.indexOf(" | ", valorInicio);
        String valor = fin >= 0 ? descripcion.substring(valorInicio, fin) : descripcion.substring(valorInicio);
        return valor == null ? null : valor.trim();
    }

    private boolean esOportunidadActiva(Pedido pedido) {
        return ESTADOS_INTERES.contains(normalizarEstado(pedido.getEstado()));
    }

    private boolean esOportunidadWeb(Pedido pedido) {
        return pedido.getDescripcion() != null && pedido.getDescripcion().startsWith("[WEB]");
    }

    private Conductor resolverConductor(String conductorId) {
        if (conductorId == null || conductorId.isBlank()) {
            return null;
        }

        Long idConductor = Long.parseLong(conductorId.trim());
        return conductorRepository.findById(idConductor)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));
    }

    private void sincronizarClienteDesdeOportunidad(Pedido pedido) {
        Cliente cliente = buscarClienteRelacionado(pedido);
        if (cliente == null) {
            return;
        }

        cliente.setEstado(mapearEstadoCliente(pedido.getEstado()));
        cliente.setInteresVehiculo(extraerVehiculo(pedido));
        cliente.setFechaUltimaInteraccion(LocalDateTime.now());
        cliente.setNotas(anexarNotaGestion(cliente.getNotas(), pedido));
        clienteRepository.save(cliente);
    }

    private String mapearEstadoCliente(String estadoPedido) {
        return switch (normalizarEstado(estadoPedido)) {
            case "contactado" -> "Contactado";
            case "negociando", "vendido" -> "Negociando";
            case "entregado" -> "Comprador";
            case "perdido" -> "Perdido";
            default -> "Interesado";
        };
    }

    private String anexarNotaGestion(String notasActuales, Pedido pedido) {
        StringBuilder builder = new StringBuilder();
        if (notasActuales != null && !notasActuales.isBlank()) {
            builder.append(notasActuales.trim()).append(System.lineSeparator()).append(System.lineSeparator());
        }

        builder.append("[Gestión gerente] Estado: ")
                .append(normalizarEstado(pedido.getEstado()));

        if (pedido.getConductor() != null && pedido.getConductor().getNombre() != null && !pedido.getConductor().getNombre().isBlank()) {
            builder.append(" | Vendedor: ").append(pedido.getConductor().getNombre());
        }

        builder.append(" | Fecha: ").append(FORMATO_FECHA_REPORTE.format(LocalDateTime.now()));
        return builder.toString();
    }

    private List<Map.Entry<String, Long>> topVehiculosSolicitados(List<Pedido> pedidos, int limite) {
        return pedidos.stream()
                .collect(Collectors.groupingBy(this::extraerVehiculo, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limite)
                .toList();
    }

    private Map<String, Long> resumenEstados(List<Pedido> pedidos) {
        return pedidos.stream()
                .collect(Collectors.groupingBy(p -> normalizarEstado(p.getEstado()), Collectors.counting()));
    }

    private List<Map<String, Object>> construirRendimientoVendedores(List<Pedido> pedidos) {
        Map<String, List<Pedido>> porVendedor = pedidos.stream()
                .collect(Collectors.groupingBy(p -> {
                    if (p.getConductor() == null || p.getConductor().getNombre() == null || p.getConductor().getNombre().isBlank()) {
                        return "Sin vendedor";
                    }
                    return p.getConductor().getNombre();
                }));

        return porVendedor.entrySet().stream()
                .map(entry -> {
                    String vendedor = entry.getKey();
                    List<Pedido> pedidosVendedor = entry.getValue();
                    long oportunidades = pedidosVendedor.size();
                    long ventas = pedidosVendedor.stream()
                            .filter(p -> ESTADOS_VENTA.contains(normalizarEstado(p.getEstado())))
                            .count();

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("vendedor", vendedor);
                    datos.put("interesados", oportunidades);
                    datos.put("ventas", ventas);
                    datos.put("conversion", oportunidades == 0 ? 0.0 : (ventas * 100.0) / oportunidades);
                    return datos;
                })
                .sorted((a, b) -> Long.compare((long) b.get("ventas"), (long) a.get("ventas")))
                .toList();
    }

    private List<Notificacion> construirAlertasInteligentes() {
        List<Notificacion> alertas = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        long inventarioBajo = inventarioRepository.findByActivoTrue().size();
        if (inventarioBajo < 5) {
            Notificacion n = new Notificacion();
            n.setTitulo("Inventario bajo");
            n.setMensaje("Solo hay " + inventarioBajo + " vehiculos activos en inventario.");
            n.setFecha(ahora);
            n.setLeida(false);
            alertas.add(n);
        }

        long clientesSinContacto = pedidoRepository.findAll().stream()
                .filter(p -> "pendiente".equals(normalizarEstado(p.getEstado())))
                .filter(p -> p.getFechaCreacion() != null && p.getFechaCreacion().isBefore(ahora.minusDays(5)))
                .count();
        if (clientesSinContacto > 0) {
            Notificacion n = new Notificacion();
            n.setTitulo("Clientes sin contacto");
            n.setMensaje(clientesSinContacto + " clientes llevan mas de 5 dias sin contacto.");
            n.setFecha(ahora);
            n.setLeida(false);
            alertas.add(n);
        }

        Map<Long, Boolean> vendidoPorVehiculo = pedidoRepository.findAll().stream()
                .filter(p -> p.getVehiculo() != null && p.getVehiculo().getIdVehiculo() != null)
                .collect(Collectors.toMap(
                        p -> p.getVehiculo().getIdVehiculo(),
                        p -> ESTADOS_VENTA.contains(normalizarEstado(p.getEstado())),
                        (a, b) -> a || b
                ));

        long vehiculosSinVenta60Dias = vehiculoRepository.findByActivoTrue().stream()
                .filter(v -> v.getFechaCreacion() != null && v.getFechaCreacion().isBefore(ahora.minusDays(60)))
                .filter(v -> !vendidoPorVehiculo.getOrDefault(v.getIdVehiculo(), false))
                .count();

        if (vehiculosSinVenta60Dias > 0) {
            Notificacion n = new Notificacion();
            n.setTitulo("Vehiculos sin rotacion");
            n.setMensaje(vehiculosSinVenta60Dias + " vehiculos llevan mas de 60 dias sin venderse.");
            n.setFecha(ahora);
            n.setLeida(false);
            alertas.add(n);
        }

        return alertas;
    }

    private void sincronizarVehiculoConInventario(Inventario inventario) {
        inventarioVentaService.sincronizarVehiculoConInventario(inventario);
    }

    private void agregarResumenInventario(Model model, List<Inventario> inventarios) {
        long activos = inventarios.stream()
                .filter(Inventario::isActivo)
                .mapToLong(this::obtenerCantidadInventario)
                .sum();
        long inactivos = inventarios.stream()
                .filter(i -> !i.isActivo())
                .mapToLong(this::obtenerCantidadInventario)
                .sum();

        model.addAttribute("totalInventarioVista", inventarios.stream().mapToLong(this::obtenerCantidadInventario).sum());
        model.addAttribute("totalActivosVista", activos);
        model.addAttribute("totalInactivosVista", inactivos);
        model.addAttribute("enBodegaVista", inventarios.stream()
                .filter(i -> i.getEstadoLogistico() != null)
                .filter(i -> i.getEstadoLogistico().toLowerCase(Locale.ROOT).contains("bodega"))
                .mapToLong(this::obtenerCantidadInventario)
                .sum());
    }

    private void prepararFormularioInventarioGerente(Model model, Inventario inventario, Authentication auth) {
        agregarDatosCorreo(model, auth);
        model.addAttribute("esAdmin", false);
        model.addAttribute("inventario", inventario);
    }

    private void validarInventarioGerente(Inventario inventario, Long idInventarioActual) {
        inventario.setChasis(normalizarTextoRequeridoInventario(inventario.getChasis(), "El chasis es obligatorio.").toUpperCase(Locale.ROOT));
        inventario.setMarca(normalizarTextoRequeridoInventario(inventario.getMarca(), "La marca es obligatoria."));
        inventario.setModelo(normalizarTextoRequeridoInventario(inventario.getModelo(), "El modelo es obligatorio."));
        inventario.setColor(normalizarTextoOpcionalInventario(inventario.getColor()));
        inventario.setMotor(normalizarTextoOpcionalInventario(inventario.getMotor()));
        inventario.setUbicacionActual(normalizarTextoOpcionalInventario(inventario.getUbicacionActual()));
        inventario.setEstadoLogistico(normalizarTextoOpcionalInventario(inventario.getEstadoLogistico()));

        if (contieneEspaciosEnBlancoInventario(inventario.getChasis())) {
            throw new IllegalArgumentException("El chasis no puede contener espacios en blanco.");
        }

        boolean duplicado = idInventarioActual == null
                ? inventarioRepository.existsByChasisIgnoreCase(inventario.getChasis())
                : inventarioRepository.existsByChasisIgnoreCaseAndIdInventarioNot(inventario.getChasis(), idInventarioActual);

        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un registro de inventario con el chasis '" + inventario.getChasis() + "'.");
        }
    }

    private String normalizarTextoRequeridoInventario(String valor, String mensajeError) {
        String normalizado = normalizarTextoOpcionalInventario(valor);
        if (normalizado == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        return normalizado;
    }

    private String normalizarTextoOpcionalInventario(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isBlank() ? null : normalizado;
    }

    private boolean contieneEspaciosEnBlancoInventario(String valor) {
        for (int i = 0; i < valor.length(); i++) {
            if (Character.isWhitespace(valor.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private long obtenerCantidadInventario(Inventario inventario) {
        return inventario.getCantidadDisponible() == null || inventario.getCantidadDisponible() < 1
                ? 1L
                : inventario.getCantidadDisponible();
    }

    private List<Map<String, Object>> construirResumenMensual(List<Pedido> pedidos,
                                                              Map<Long, ContabilidadVenta> contabilidadPorPedido,
                                                              int cantidadMeses) {
        YearMonth actual = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", LOCALE_ES);

        Map<YearMonth, Map<String, Object>> acumulado = new LinkedHashMap<>();
        for (int i = cantidadMeses - 1; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            Map<String, Object> fila = new HashMap<>();
            fila.put("mes", mes.format(formatter));
            fila.put("interesados", 0L);
            fila.put("ventas", 0L);
            fila.put("ingresos", 0.0);
            acumulado.put(mes, fila);
        }

        for (Pedido pedido : pedidos) {
            if (pedido.getFechaCreacion() != null) {
                YearMonth mesInteres = YearMonth.from(pedido.getFechaCreacion());
                Map<String, Object> filaInteres = acumulado.get(mesInteres);
                if (filaInteres != null) {
                    long interesados = ((Number) filaInteres.get("interesados")).longValue() + 1;
                    filaInteres.put("interesados", interesados);
                }
            }

            if (!ESTADOS_VENTA.contains(normalizarEstado(pedido.getEstado()))) {
                continue;
            }

            ContabilidadVenta registro = contabilidadPorPedido.get(obtenerPedidoId(pedido));
            LocalDateTime fechaComercial = resolverFechaComercial(pedido, registro);
            if (fechaComercial == null) {
                continue;
            }

            YearMonth mesVenta = YearMonth.from(fechaComercial);
            Map<String, Object> filaVenta = acumulado.get(mesVenta);
            if (filaVenta == null) {
                continue;
            }

            long ventas = ((Number) filaVenta.get("ventas")).longValue() + 1;
            double ingresos = ((Number) filaVenta.get("ingresos")).doubleValue() + calcularIngresoPedido(pedido, registro);
            filaVenta.put("ventas", ventas);
            filaVenta.put("ingresos", ingresos);
        }

        List<Map<String, Object>> resumen = new ArrayList<>(acumulado.values());
        resumen.forEach(fila -> fila.put("ingresosTexto", formatearMoneda(((Number) fila.get("ingresos")).doubleValue())));
        return resumen;
    }

    private double calcularIngresoPedido(Pedido pedido, ContabilidadVenta contabilidad) {
        if (!ESTADOS_VENTA.contains(normalizarEstado(pedido.getEstado()))) {
            return 0.0;
        }

        if (contabilidad != null) {
            if (contabilidad.getPrecioVentaFinal() != null && contabilidad.getPrecioVentaFinal() > 0.0) {
                return contabilidad.getPrecioVentaFinal();
            }
            if (contabilidad.getPrecioPublicadoSnapshot() != null && contabilidad.getPrecioPublicadoSnapshot() > 0.0) {
                return contabilidad.getPrecioPublicadoSnapshot();
            }
        }

        if (pedido.getVehiculo() == null || pedido.getVehiculo().getPrecio() == null) {
            return 0.0;
        }
        return pedido.getVehiculo().getPrecio();
    }

    private Map<Long, ContabilidadVenta> cargarContabilidadPorPedido() {
        return contabilidadVentaRepository.findAll().stream()
                .filter(contabilidad -> contabilidad.getPedido() != null && contabilidad.getPedido().getIdPedido() != null)
                .collect(Collectors.toMap(contabilidad -> contabilidad.getPedido().getIdPedido(), contabilidad -> contabilidad, (actual, reemplazo) -> reemplazo));
    }

    private Long obtenerPedidoId(Pedido pedido) {
        return pedido != null ? pedido.getIdPedido() : null;
    }

    private LocalDateTime resolverFechaComercial(Pedido pedido, ContabilidadVenta contabilidad) {
        if (pedido == null) {
            return null;
        }
        if (ESTADOS_VENTA.contains(normalizarEstado(pedido.getEstado())) && contabilidad != null && contabilidad.getFechaActualizacion() != null) {
            return contabilidad.getFechaActualizacion();
        }
        return pedido.getFechaCreacion();
    }

    private String formatearMoneda(double valor) {
        NumberFormat format = NumberFormat.getCurrencyInstance(LOCALE_ES);
        return format.format(valor);
    }

    private Cell celdaKpi(String titulo, String valor, DeviceRgb colorAcento) {
        Paragraph texto = new Paragraph(titulo + "\n" + valor)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setBold();

        return new Cell()
                .add(texto)
            .setBackgroundColor(new DeviceRgb(248, 250, 253))
            .setFontColor(colorAcento)
                .setPadding(10);
    }

    private Cell celdaHeader(String texto, DeviceRgb colorFondo) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(colorFondo)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private Cell celdaDato(String texto) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "-"))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);
    }

    private byte[] generarGraficoVentasIngresos(List<Map<String, Object>> resumenMensual) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map<String, Object> fila : resumenMensual) {
                String mes = (String) fila.get("mes");
                long ventas = ((Number) fila.get("ventas")).longValue();
                double ingresos = ((Number) fila.get("ingresos")).doubleValue();

                dataset.addValue(ventas, "Ventas", mes);
                dataset.addValue(ingresos, "Ingresos", mes);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Comportamiento mensual",
                    "Mes",
                    "Valor",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false
            );

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setRangeGridlinePaint(new java.awt.Color(214, 220, 230));

            ByteArrayOutputStream chartOutput = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(chartOutput, chart, 900, 360);
            return chartOutput.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    // ===============================
    // 👥 GESTIÓN DE CLIENTES (NUEVO)
    // ===============================
    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String estado, Model model) {
        List<Cliente> clientes;
        if (estado != null && !estado.isEmpty()) {
            clientes = clienteRepository.findByActivoTrueAndEstado(estado);
            model.addAttribute("estadoFiltro", estado);
        } else {
            clientes = clienteRepository.findByActivoTrue();
        }
        model.addAttribute("clientes", clientes);
        model.addAttribute("estadosDisponibles", new String[]{"Nuevo", "Interesado", "Contactado", "Negociando", "Comprador", "Perdido"});
        return "gerente/clientes/listar";
    }

    @GetMapping("/clientes/crear")
    public String crearClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "gerente/clientes/form";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente) {
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setFechaUltimaInteraccion(LocalDateTime.now());
        clienteRepository.save(cliente);
        return "redirect:/gerente/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editarClienteForm(@PathVariable Long id, Model model) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        return "gerente/clientes/form";
    }

    @PostMapping("/clientes/editar/{id}")
    public String actualizarCliente(@PathVariable Long id, @ModelAttribute Cliente cliente) {
        Cliente existente = clienteRepository.findById(id).orElseThrow();
        existente.setNombre(cliente.getNombre());
        existente.setCorreo(cliente.getCorreo());
        existente.setTelefono(cliente.getTelefono());
        existente.setCedula(cliente.getCedula());
        existente.setCiudad(cliente.getCiudad());
        existente.setDireccion(cliente.getDireccion());
        existente.setInteresVehiculo(cliente.getInteresVehiculo());
        existente.setPresupuesto(cliente.getPresupuesto());
        existente.setEstado(cliente.getEstado());
        existente.setNotas(cliente.getNotas());
        existente.setFechaUltimaInteraccion(LocalDateTime.now());
        clienteRepository.save(existente);
        return "redirect:/gerente/clientes";
    }

    @PostMapping("/clientes/{id}/cambiar-estado")
    public String cambiarEstadoCliente(@PathVariable Long id, @RequestParam String nuevoEstado) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setEstado(nuevoEstado);
        cliente.setFechaUltimaInteraccion(LocalDateTime.now());
        clienteRepository.save(cliente);
        return "redirect:/gerente/clientes";
    }

    @PostMapping("/clientes/desactivar/{id}")
    public String desactivarCliente(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setActivo(false);
        clienteRepository.save(cliente);
        return "redirect:/gerente/clientes";
    }

    // ===============================
    // 🏁 SOLICITUDES DE PRUEBA (NUEVO)
    // ===============================
    @GetMapping("/pruebas")
    public String listarPruebas(@RequestParam(required = false) String estado, Model model) {
        List<SolicitudPrueba> pruebas;
        if (estado != null && !estado.isEmpty()) {
            pruebas = solicitudPruebaRepository.findByEstado(estado);
            model.addAttribute("estadoFiltro", estado);
        } else {
            pruebas = solicitudPruebaRepository.findAll();
        }
        model.addAttribute("pruebas", pruebas);
        model.addAttribute("estadosDisponibles", new String[]{"Pendiente", "Aprobada", "Realizada", "Rechazada"});
        return "gerente/pruebas/listar";
    }

    @GetMapping("/pruebas/cliente/{idCliente}")
    public String listarPruebasPorCliente(@PathVariable Long idCliente, Model model) {
        Cliente cliente = clienteRepository.findById(idCliente).orElseThrow();
        List<SolicitudPrueba> pruebas = solicitudPruebaRepository.findByCliente(cliente);
        model.addAttribute("pruebas", pruebas);
        model.addAttribute("cliente", cliente);
        return "gerente/pruebas/por-cliente";
    }

    @GetMapping("/pruebas/pendientes")
    public String pruebasPendientes(Model model) {
        List<SolicitudPrueba> pruebas = solicitudPruebaRepository.findByEstado("Pendiente");
        model.addAttribute("pruebas", pruebas);
        return "gerente/pruebas/listar";
    }

    @PostMapping("/pruebas/{id}/aprobar")
    public String aprobarPrueba(@PathVariable Long id) {
        SolicitudPrueba solicitud = solicitudPruebaRepository.findById(id).orElseThrow();
        solicitud.setEstado("Aprobada");
        solicitud.setFechaAprobada(LocalDateTime.now());
        solicitudPruebaRepository.save(solicitud);
        return "redirect:/gerente/pruebas";
    }

    @PostMapping("/pruebas/{id}/rechazar")
    public String rechazarPrueba(@PathVariable Long id) {
        SolicitudPrueba solicitud = solicitudPruebaRepository.findById(id).orElseThrow();
        solicitud.setEstado("Rechazada");
        solicitudPruebaRepository.save(solicitud);
        return "redirect:/gerente/pruebas";
    }

    @PostMapping("/pruebas/{id}/marcar-realizada")
    public String marcarPruebaRealizada(
            @PathVariable Long id,
            @RequestParam String resultado,
            @RequestParam(required = false) String notas) {
        SolicitudPrueba solicitud = solicitudPruebaRepository.findById(id).orElseThrow();
        solicitud.setEstado("Realizada");
        solicitud.setFechaRealizacion(LocalDateTime.now());
        solicitud.setResultadoPrueba(resultado);
        if (notas != null) {
            solicitud.setNotas(notas);
        }
        if ("Muy Interesado".equals(resultado)) {
            Cliente cliente = solicitud.getCliente();
            cliente.setEstado("Negociando");
            cliente.setFechaUltimaInteraccion(LocalDateTime.now());
            clienteRepository.save(cliente);
        }
        solicitudPruebaRepository.save(solicitud);
        return "redirect:/gerente/pruebas";
    }
}
