package com.example.demostracion.service;

import com.example.demostracion.dto.ResultadoCargaDTO;
import com.example.demostracion.dto.VehiculoCargaDTO;
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
import java.util.Optional;

@Service
public class CargaMasivaService {

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;

    public CargaMasivaService(VehiculoRepository vehiculoRepository, InventarioRepository inventarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Transactional
    public ResultadoCargaDTO procesarArchivo(MultipartFile archivo) throws IOException {
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

    private ResultadoCargaDTO procesarExcel(MultipartFile archivo) throws IOException {
        ResultadoCargaDTO resultado = new ResultadoCargaDTO();
        List<VehiculoCargaDTO> vehiculosParaCargar = new ArrayList<>();
        
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

                VehiculoCargaDTO vehiculoDTO = extraerVehiculoDeExcel(row, filaNumero);
                vehiculosParaCargar.add(vehiculoDTO);
            }
        }

        return procesarYGuardarVehiculos(vehiculosParaCargar);
    }

    private ResultadoCargaDTO procesarCSV(MultipartFile archivo) throws IOException {
        ResultadoCargaDTO resultado = new ResultadoCargaDTO();
        List<VehiculoCargaDTO> vehiculosParaCargar = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            int filaNumero = 1; // Empezamos en 1 porque el header es la fila 0
            
            for (CSVRecord record : csvParser) {
                filaNumero++;
                VehiculoCargaDTO vehiculoDTO = extraerVehiculoDeCSV(record, filaNumero);
                vehiculosParaCargar.add(vehiculoDTO);
            }
        }

        return procesarYGuardarVehiculos(vehiculosParaCargar);
    }

    private VehiculoCargaDTO extraerVehiculoDeExcel(Row row, int filaNumero) {
        VehiculoCargaDTO dto = new VehiculoCargaDTO();
        dto.setFila(filaNumero);
        dto.setValido(true);
        StringBuilder errores = new StringBuilder();

        try {
            // Columna 0: Chasis
            Cell chasisCell = row.getCell(0);
            if (chasisCell != null) {
                dto.setChasis(obtenerValorCelda(chasisCell));
            }

            // Columna 1: Modelo
            Cell modeloCell = row.getCell(1);
            if (modeloCell != null) {
                dto.setModelo(obtenerValorCelda(modeloCell));
            }

            // Columna 2: ID Inventario (opcional)
            Cell inventarioCell = row.getCell(2);
            if (inventarioCell != null && inventarioCell.getCellType() != CellType.BLANK) {
                try {
                    String valor = obtenerValorCelda(inventarioCell);
                    if (!valor.isEmpty()) {
                        dto.setInventarioId(Long.parseLong(valor));
                    }
                } catch (NumberFormatException e) {
                    errores.append("ID de inventario inválido. ");
                }
            }

            // Validaciones
            if (dto.getChasis() == null || dto.getChasis().trim().isEmpty()) {
                errores.append("Chasis es obligatorio. ");
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

    private VehiculoCargaDTO extraerVehiculoDeCSV(CSVRecord record, int filaNumero) {
        VehiculoCargaDTO dto = new VehiculoCargaDTO();
        dto.setFila(filaNumero);
        dto.setValido(true);
        StringBuilder errores = new StringBuilder();

        try {
            // Leer columnas por nombre o índice
            dto.setChasis(record.get("chasis").trim());
            dto.setModelo(record.get("modelo").trim());
            
            String inventarioIdStr = record.get("inventario_id").trim();
            if (!inventarioIdStr.isEmpty()) {
                try {
                    dto.setInventarioId(Long.parseLong(inventarioIdStr));
                } catch (NumberFormatException e) {
                    errores.append("ID de inventario inválido. ");
                }
            }

            // Validaciones
            if (dto.getChasis() == null || dto.getChasis().isEmpty()) {
                errores.append("Chasis es obligatorio. ");
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
    private ResultadoCargaDTO procesarYGuardarVehiculos(List<VehiculoCargaDTO> vehiculosDTO) {
        ResultadoCargaDTO resultado = new ResultadoCargaDTO();
        resultado.setTotalFilas(vehiculosDTO.size());
        resultado.setFilasExitosas(0);
        resultado.setFilasConError(0);
        resultado.setErrores(new ArrayList<>());

        for (VehiculoCargaDTO dto : vehiculosDTO) {
            if (!dto.isValido()) {
                resultado.setFilasConError(resultado.getFilasConError() + 1);
                resultado.getErrores().add(dto);
                continue;
            }

            try {
                // Validar que el chasis no exista
                if (vehiculoRepository.existsByChasis(dto.getChasis())) {
                    dto.setValido(false);
                    dto.setError("El chasis ya existe en la base de datos");
                    resultado.setFilasConError(resultado.getFilasConError() + 1);
                    resultado.getErrores().add(dto);
                    continue;
                }

                // Crear el vehículo
                Vehiculo vehiculo = new Vehiculo();
                vehiculo.setChasis(dto.getChasis());
                vehiculo.setModelo(dto.getModelo());
                vehiculo.setActivo(true);
                vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_BORRADOR);

                // Asignar inventario si se proporcionó
                if (dto.getInventarioId() != null) {
                    Optional<Inventario> inventario = inventarioRepository.findById(dto.getInventarioId());
                    if (inventario.isPresent()) {
                        vehiculo.setInventario(inventario.get());
                    } else {
                        dto.setValido(false);
                        dto.setError("El inventario con ID " + dto.getInventarioId() + " no existe");
                        resultado.setFilasConError(resultado.getFilasConError() + 1);
                        resultado.getErrores().add(dto);
                        continue;
                    }
                }

                vehiculoRepository.save(vehiculo);
                resultado.setFilasExitosas(resultado.getFilasExitosas() + 1);

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
            resultado.setMensaje("Carga completada exitosamente. " + resultado.getFilasExitosas() + " vehículos registrados.");
        } else {
            resultado.setMensaje("Carga completada con errores. " + resultado.getFilasExitosas() + " exitosos, " + resultado.getFilasConError() + " con errores.");
        }

        return resultado;
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
