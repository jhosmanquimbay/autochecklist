package com.example.demostracion.service;

import com.example.demostracion.dto.InventarioCargaDTO;
import com.example.demostracion.dto.ResultadoCargaInventarioDTO;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CargaMasivaInventarioService {

    private final InventarioRepository inventarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public CargaMasivaInventarioService(InventarioRepository inventarioRepository,
                                       VehiculoRepository vehiculoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional
    public ResultadoCargaInventarioDTO procesarArchivo(MultipartFile archivo) throws IOException {
        String nombreArchivo = archivo.getOriginalFilename();
        
        if (nombreArchivo == null) {
            throw new IllegalArgumentException("El archivo no tiene nombre");
        }

        if (nombreArchivo.endsWith(".xlsx") || nombreArchivo.endsWith(".xls")) {
            return procesarExcel(archivo);
        } else if (nombreArchivo.endsWith(".csv")) {
            return procesarCSV(archivo);
        } else {
            throw new IllegalArgumentException("Formato de archivo no soportado. Use Excel (.xlsx, .xls) o CSV (.csv)");
        }
    }

    private ResultadoCargaInventarioDTO procesarExcel(MultipartFile archivo) throws IOException {
        List<InventarioCargaDTO> inventariosParaCargar = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int filaNumero = 0;

            for (Row row : sheet) {
                filaNumero++;
                
                // Saltar la fila de encabezados
                if (filaNumero == 1) {
                    continue;
                }

                // Saltar filas vacías
                if (esFilaVacia(row)) {
                    continue;
                }

                InventarioCargaDTO inventarioDTO = extraerInventarioDeExcel(row, filaNumero);
                inventariosParaCargar.add(inventarioDTO);
            }
        }

        return procesarYGuardarInventarios(inventariosParaCargar);
    }

    private ResultadoCargaInventarioDTO procesarCSV(MultipartFile archivo) throws IOException {
        List<InventarioCargaDTO> inventariosParaCargar = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            int filaNumero = 1;
            
            for (CSVRecord record : csvParser) {
                filaNumero++;
                InventarioCargaDTO inventarioDTO = extraerInventarioDeCSV(record, filaNumero);
                inventariosParaCargar.add(inventarioDTO);
            }
        }

        return procesarYGuardarInventarios(inventariosParaCargar);
    }

    private InventarioCargaDTO extraerInventarioDeExcel(Row row, int filaNumero) {
        InventarioCargaDTO dto = new InventarioCargaDTO();
        dto.setFila(filaNumero);
        dto.setValido(true);
        StringBuilder errores = new StringBuilder();

        try {
            // Columna 0: Chasis
            Cell chasisCell = row.getCell(0);
            if (chasisCell != null) {
                dto.setChasis(obtenerValorCelda(chasisCell));
            }

            // Columna 1: Marca
            Cell marcaCell = row.getCell(1);
            if (marcaCell != null) {
                dto.setMarca(obtenerValorCelda(marcaCell));
            }

            // Columna 2: Modelo
            Cell modeloCell = row.getCell(2);
            if (modeloCell != null) {
                dto.setModelo(obtenerValorCelda(modeloCell));
            }

            // Columna 3: Año
            Cell anioCell = row.getCell(3);
            if (anioCell != null && anioCell.getCellType() != CellType.BLANK) {
                try {
                    String valor = obtenerValorCelda(anioCell);
                    if (!valor.isEmpty()) {
                        dto.setAnio(Integer.parseInt(valor));
                    }
                } catch (NumberFormatException e) {
                    errores.append("Año inválido. ");
                }
            }

            // Columna 4: Color
            Cell colorCell = row.getCell(4);
            if (colorCell != null) {
                dto.setColor(obtenerValorCelda(colorCell));
            }

            // Columna 5: Motor
            Cell motorCell = row.getCell(5);
            if (motorCell != null) {
                dto.setMotor(obtenerValorCelda(motorCell));
            }

            // Columna 6: Ubicación Actual
            Cell ubicacionCell = row.getCell(6);
            if (ubicacionCell != null) {
                dto.setUbicacionActual(obtenerValorCelda(ubicacionCell));
            }

            // Columna 7: Estado Logístico
            Cell estadoCell = row.getCell(7);
            if (estadoCell != null) {
                dto.setEstadoLogistico(obtenerValorCelda(estadoCell));
            }

            // Validaciones obligatorias
            if (dto.getChasis() == null || dto.getChasis().trim().isEmpty()) {
                errores.append("Chasis es obligatorio. ");
            }
            if (dto.getMarca() == null || dto.getMarca().trim().isEmpty()) {
                errores.append("Marca es obligatoria. ");
            }
            if (dto.getModelo() == null || dto.getModelo().trim().isEmpty()) {
                errores.append("Modelo es obligatorio. ");
            }

            if (errores.length() > 0) {
                dto.setValido(false);
                dto.setError(errores.toString());
            }

        } catch (Exception e) {
            dto.setValido(false);
            dto.setError("Error al procesar la fila: " + e.getMessage());
        }

        return dto;
    }

    private InventarioCargaDTO extraerInventarioDeCSV(CSVRecord record, int filaNumero) {
        InventarioCargaDTO dto = new InventarioCargaDTO();
        dto.setFila(filaNumero);
        dto.setValido(true);
        StringBuilder errores = new StringBuilder();

        try {
            dto.setChasis(record.get("chasis").trim());
            dto.setMarca(record.get("marca").trim());
            dto.setModelo(record.get("modelo").trim());
            
            String anioStr = record.get("anio").trim();
            if (!anioStr.isEmpty()) {
                try {
                    dto.setAnio(Integer.parseInt(anioStr));
                } catch (NumberFormatException e) {
                    errores.append("Año inválido. ");
                }
            }
            
            dto.setColor(record.get("color").trim());
            dto.setMotor(record.get("motor").trim());
            dto.setUbicacionActual(record.get("ubicacion_actual").trim());
            dto.setEstadoLogistico(record.get("estado_logistico").trim());

            // Validaciones obligatorias
            if (dto.getChasis() == null || dto.getChasis().isEmpty()) {
                errores.append("Chasis es obligatorio. ");
            }
            if (dto.getMarca() == null || dto.getMarca().isEmpty()) {
                errores.append("Marca es obligatoria. ");
            }
            if (dto.getModelo() == null || dto.getModelo().isEmpty()) {
                errores.append("Modelo es obligatorio. ");
            }

            if (errores.length() > 0) {
                dto.setValido(false);
                dto.setError(errores.toString());
            }

        } catch (Exception e) {
            dto.setValido(false);
            dto.setError("Error al procesar la fila: " + e.getMessage());
        }

        return dto;
    }

    @Transactional
    private ResultadoCargaInventarioDTO procesarYGuardarInventarios(List<InventarioCargaDTO> inventariosDTO) {
        ResultadoCargaInventarioDTO resultado = new ResultadoCargaInventarioDTO();
        resultado.setTotalFilas(inventariosDTO.size());
        resultado.setFilasExitosas(0);
        resultado.setFilasConError(0);
        resultado.setErrores(new ArrayList<>());
        int filasActualizadas = 0;

        for (InventarioCargaDTO dto : inventariosDTO) {
            if (!dto.isValido()) {
                resultado.setFilasConError(resultado.getFilasConError() + 1);
                resultado.getErrores().add(dto);
                continue;
            }

            try {
                Inventario inventario = inventarioRepository.findByChasis(dto.getChasis())
                        .orElseGet(Inventario::new);
                boolean inventarioExistente = inventario.getIdInventario() != null;

                inventario.setChasis(dto.getChasis());
                inventario.setMarca(dto.getMarca());
                inventario.setModelo(dto.getModelo());
                inventario.setAnio(dto.getAnio());
                inventario.setColor(dto.getColor());
                inventario.setMotor(dto.getMotor());
                inventario.setUbicacionActual(dto.getUbicacionActual());
                inventario.setEstadoLogistico(dto.getEstadoLogistico());
                inventario.setActivo(true);

                inventarioRepository.save(inventario);
                sincronizarCatalogoVehiculos(dto);
                resultado.setFilasExitosas(resultado.getFilasExitosas() + 1);
                if (inventarioExistente) {
                    filasActualizadas++;
                }

            } catch (Exception e) {
                dto.setValido(false);
                dto.setError("Error al guardar: " + e.getMessage());
                resultado.setFilasConError(resultado.getFilasConError() + 1);
                resultado.getErrores().add(dto);
            }
        }

        // Establecer resultado general
        resultado.setExitoso(resultado.getFilasConError() == 0);
        if (resultado.isExitoso()) {
            resultado.setMensaje("Carga completada exitosamente. " + resultado.getFilasExitosas() + " vehículos procesados (" + filasActualizadas + " actualizados).");
        } else {
            resultado.setMensaje("Carga completada con errores. " + resultado.getFilasExitosas() + " exitosos (" + filasActualizadas + " actualizados), " + resultado.getFilasConError() + " con errores.");
        }

        return resultado;
    }

    private void sincronizarCatalogoVehiculos(InventarioCargaDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findByChasis(dto.getChasis())
                .orElseGet(Vehiculo::new);

        boolean esNuevo = vehiculo.getIdVehiculo() == null;

        vehiculo.setChasis(dto.getChasis());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setActivo(true);
        if (esNuevo) {
            vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_BORRADOR);
        }

        if (vehiculo.getTipoCombustible() == null || vehiculo.getTipoCombustible().isBlank()) {
            vehiculo.setTipoCombustible("Gasolina");
        }
        if (vehiculo.getTransmision() == null || vehiculo.getTransmision().isBlank()) {
            vehiculo.setTransmision("Manual");
        }
        if (vehiculo.getDescripcion() == null || vehiculo.getDescripcion().isBlank()) {
            vehiculo.setDescripcion("Vehiculo incorporado desde carga masiva de inventario.");
        }

        vehiculoRepository.save(vehiculo);
    }

    private String obtenerValorCelda(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private boolean esFilaVacia(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
