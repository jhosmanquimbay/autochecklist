package com.example.demostracion.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demostracion.dto.FinanciamientoResultadoDTO;
import com.example.demostracion.dto.VehiculoClienteDTO;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@Service
public class OficinaClienteService {

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final FinanciamientoService financiamientoService;

    public OficinaClienteService(VehiculoRepository vehiculoRepository,
                                 InventarioRepository inventarioRepository,
                                 FinanciamientoService financiamientoService) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.financiamientoService = financiamientoService;
    }

    public List<String> obtenerMarcasDisponibles() {
        return vehiculoRepository.findAllByOrderByFechaCreacionDesc().stream()
            .filter(Vehiculo::isActivo)
            .filter(Vehiculo::estaPublicado)
                .map(Vehiculo::getMarca)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(marca -> !marca.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<VehiculoClienteDTO> obtenerVehiculos(String marca) {
        Map<String, Inventario> inventarioPorChasis = inventarioRepository.findAll().stream()
                .filter(inventario -> inventario.getChasis() != null && !inventario.getChasis().isBlank())
                .collect(Collectors.toMap(
                        inventario -> normalizarClave(inventario.getChasis()),
                        inventario -> inventario,
                        this::seleccionarInventario,
                        LinkedHashMap::new));

        return vehiculoRepository.findAllByOrderByFechaCreacionDesc().stream()
            .filter(Vehiculo::isActivo)
            .filter(Vehiculo::estaPublicado)
                .filter(vehiculo -> filtrarPorMarca(vehiculo, marca))
                .map(vehiculo -> construirDto(vehiculo, inventarioPorChasis.get(normalizarClave(vehiculo.getChasis()))))
                .sorted(Comparator
                        .comparing(VehiculoClienteDTO::isDisponible).reversed()
                        .thenComparing(VehiculoClienteDTO::getPopularidad, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(VehiculoClienteDTO::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<VehiculoClienteDTO> obtenerDestacados(List<VehiculoClienteDTO> vehiculos) {
        return vehiculos.stream()
                .sorted(Comparator
                        .comparing(VehiculoClienteDTO::isDisponible).reversed()
                        .thenComparing(VehiculoClienteDTO::getPopularidad, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(VehiculoClienteDTO::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
    }

    public List<VehiculoClienteDTO> obtenerVehiculosPorIds(Set<Long> idsVehiculo) {
        if (idsVehiculo == null || idsVehiculo.isEmpty()) {
            return List.of();
        }

        return obtenerVehiculos(null).stream()
                .filter(vehiculo -> idsVehiculo.contains(vehiculo.getIdVehiculo()))
                .toList();
    }

    public VehiculoClienteDTO obtenerVehiculoDetalle(Long idVehiculo) {
        if (idVehiculo == null) {
            throw new IllegalArgumentException("Vehículo no encontrado.");
        }

        Map<String, Inventario> inventarioPorChasis = inventarioRepository.findAll().stream()
                .filter(inventario -> inventario.getChasis() != null && !inventario.getChasis().isBlank())
                .collect(Collectors.toMap(
                        inventario -> normalizarClave(inventario.getChasis()),
                        inventario -> inventario,
                        this::seleccionarInventario,
                        LinkedHashMap::new));

        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
            .filter(Vehiculo::isActivo)
            .filter(Vehiculo::estaPublicado)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));

        return construirDto(vehiculo, inventarioPorChasis.get(normalizarClave(vehiculo.getChasis())));
    }

    public List<VehiculoClienteDTO> obtenerRelacionados(Long idVehiculo, String marca) {
        return obtenerVehiculos(marca).stream()
                .filter(vehiculo -> !vehiculo.getIdVehiculo().equals(idVehiculo))
                .limit(4)
                .toList();
    }

    private Inventario seleccionarInventario(Inventario actual, Inventario candidato) {
        if (actual.isActivo() && !candidato.isActivo()) {
            return actual;
        }
        if (!actual.isActivo() && candidato.isActivo()) {
            return candidato;
        }
        return actual.getIdInventario() != null && candidato.getIdInventario() != null
                && actual.getIdInventario() > candidato.getIdInventario() ? actual : candidato;
    }

    private VehiculoClienteDTO construirDto(Vehiculo vehiculo, Inventario inventario) {
        List<String> imagenes = new ArrayList<>();

        if (vehiculo.getImagen() != null) {
            imagenes.add("/imagenes/" + vehiculo.getIdVehiculo() + "/1");
        } else if (vehiculo.getImagenUrl() != null && !vehiculo.getImagenUrl().isBlank()) {
            imagenes.add(vehiculo.getImagenUrl());
        }

        if (vehiculo.getImagen2() != null) {
            imagenes.add("/imagenes/" + vehiculo.getIdVehiculo() + "/2");
        }
        if (vehiculo.getImagen3() != null) {
            imagenes.add("/imagenes/" + vehiculo.getIdVehiculo() + "/3");
        }
        if (vehiculo.getImagen4() != null) {
            imagenes.add("/imagenes/" + vehiculo.getIdVehiculo() + "/4");
        }

        if (imagenes.isEmpty()) {
            imagenes.add("/images/mercedez.png");
        }

        int stockDisponible = calcularStockDisponible(vehiculo, inventario);
    FinanciamientoResultadoDTO referenciaFinanciamiento = valor(vehiculo.getPrecio()) > 0.0
        ? financiamientoService.calcularOfertaReferencial(vehiculo)
        : null;

        return new VehiculoClienteDTO(
                vehiculo.getIdVehiculo(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getPrecio(),
                vehiculo.getDescripcion(),
                vehiculo.getEspecificacionesTecnicas(),
                vehiculo.getTipoCombustible(),
                vehiculo.getTransmision(),
                vehiculo.getCilindrada(),
                stockDisponible,
                stockDisponible > 0,
                popularidadSegura(vehiculo),
                vehiculo.getFechaCreacion(),
                imagenes,
                referenciaFinanciamiento != null ? referenciaFinanciamiento.getCuotaMensualTotal() : null,
                referenciaFinanciamiento != null ? referenciaFinanciamiento.getPlazoMeses() : null,
                referenciaFinanciamiento != null ? referenciaFinanciamiento.getPorcentajeCuotaInicialMinima() : null);
    }

    private int calcularStockDisponible(Vehiculo vehiculo, Inventario inventario) {
        if (!vehiculo.isActivo() || !vehiculo.estaPublicado()) {
            return 0;
        }
        if (inventario == null || !inventario.isActivo()) {
            return 0;
        }
        Integer cantidad = inventario.getCantidadDisponible();
        return cantidad == null ? 1 : Math.max(cantidad, 0);
    }

    private boolean filtrarPorMarca(Vehiculo vehiculo, String marca) {
        if (marca == null || marca.isBlank()) {
            return true;
        }
        if (vehiculo.getMarca() == null) {
            return false;
        }
        return vehiculo.getMarca().trim().equalsIgnoreCase(marca.trim());
    }

    private String normalizarClave(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    private double valor(Number numero) {
        return numero == null ? 0.0 : numero.doubleValue();
    }

    private Integer popularidadSegura(Vehiculo vehiculo) {
        if (vehiculo == null || vehiculo.getPopularidad() == null) {
            return 0;
        }
        return vehiculo.getPopularidad();
    }
}