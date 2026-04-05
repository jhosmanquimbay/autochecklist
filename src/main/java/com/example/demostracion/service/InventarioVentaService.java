package com.example.demostracion.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Pedido;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@Service
public class InventarioVentaService {

    private final InventarioRepository inventarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public InventarioVentaService(InventarioRepository inventarioRepository,
                                  VehiculoRepository vehiculoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    public void sincronizarVehiculoConInventario(Inventario inventario) {
        if (inventario == null || inventario.getChasis() == null || inventario.getChasis().isBlank()) {
            return;
        }

        if (inventario.getCantidadDisponible() == null) {
            inventario.setCantidadDisponible(1);
        }

        Vehiculo vehiculo = vehiculoRepository.findByChasis(inventario.getChasis())
                .orElseGet(Vehiculo::new);

        boolean esNuevo = vehiculo.getIdVehiculo() == null;

        vehiculo.setChasis(inventario.getChasis());
        vehiculo.setMarca(inventario.getMarca());
        vehiculo.setModelo(inventario.getModelo());
        vehiculo.setAnio(inventario.getAnio());
        vehiculo.setActivo(inventario.isActivo() && inventario.getCantidadDisponible() > 0);
        if (esNuevo) {
            vehiculo.setEstadoPublicacion(Vehiculo.ESTADO_PUBLICACION_BORRADOR);
        }

        if (vehiculo.getTipoCombustible() == null || vehiculo.getTipoCombustible().isBlank()) {
            vehiculo.setTipoCombustible("Gasolina");
        }
        if (vehiculo.getTransmision() == null || vehiculo.getTransmision().isBlank()) {
            vehiculo.setTransmision("Manual");
        }

        vehiculoRepository.save(vehiculo);
    }

    public void sincronizarStockPorEstado(Pedido pedido, String estadoAnterior) {
        if (pedido == null || pedido.getVehiculo() == null) {
            return;
        }

        String estadoActualNormalizado = normalizarEstadoPedido(pedido.getEstado());
        String estadoAnteriorNormalizado = normalizarEstadoPedido(estadoAnterior);

        if ("entregado".equals(estadoActualNormalizado) && !pedido.isStockDescontado()) {
            descontarStock(pedido);
            return;
        }

        if (!"entregado".equals(estadoActualNormalizado)
                && "entregado".equals(estadoAnteriorNormalizado)
                && pedido.isStockDescontado()) {
            restaurarStock(pedido);
        }
    }

    private void descontarStock(Pedido pedido) {
        Vehiculo vehiculo = pedido.getVehiculo();
        Inventario inventario = obtenerInventario(vehiculo);

        if (inventario != null) {
            int cantidadActual = normalizarCantidad(inventario.getCantidadDisponible());
            int nuevaCantidad = Math.max(cantidadActual - 1, 0);
            inventario.setCantidadDisponible(nuevaCantidad);
            inventario.setActivo(nuevaCantidad > 0);
            if (nuevaCantidad == 0) {
                inventario.setEstadoLogistico("Entregado");
            }
            inventarioRepository.save(inventario);
            vehiculo.setActivo(nuevaCantidad > 0);
        } else {
            vehiculo.setActivo(false);
        }

        vehiculoRepository.save(vehiculo);
        pedido.setStockDescontado(true);
    }

    private void restaurarStock(Pedido pedido) {
        Vehiculo vehiculo = pedido.getVehiculo();
        Inventario inventario = obtenerInventario(vehiculo);

        if (inventario != null) {
            int cantidadActual = normalizarCantidad(inventario.getCantidadDisponible());
            inventario.setCantidadDisponible(cantidadActual + 1);
            inventario.setActivo(true);

            if (inventario.getEstadoLogistico() == null
                    || inventario.getEstadoLogistico().isBlank()
                    || "entregado".equalsIgnoreCase(inventario.getEstadoLogistico())) {
                inventario.setEstadoLogistico("En bodega");
            }

            inventarioRepository.save(inventario);
        }

        vehiculo.setActivo(true);
        vehiculoRepository.save(vehiculo);
        pedido.setStockDescontado(false);
    }

    private Inventario obtenerInventario(Vehiculo vehiculo) {
        if (vehiculo == null || vehiculo.getChasis() == null || vehiculo.getChasis().isBlank()) {
            return null;
        }
        return inventarioRepository.findByChasis(vehiculo.getChasis()).orElse(null);
    }

    private int normalizarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 0) {
            return 0;
        }
        return cantidad;
    }

    private String normalizarEstadoPedido(String estado) {
        if (estado == null || estado.isBlank()) {
            return "";
        }

        return estado.trim().toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }
}