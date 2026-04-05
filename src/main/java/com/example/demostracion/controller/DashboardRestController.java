package com.example.demostracion.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demostracion.repository.ConductorRepository;
import com.example.demostracion.repository.NotificacionRepository;
import com.example.demostracion.repository.PedidoRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@RestController
@RequestMapping("/api/estadisticas")
public class DashboardRestController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final PedidoRepository pedidoRepository;
    private final NotificacionRepository notificacionRepository;

    public DashboardRestController(
            UsuarioRepository usuarioRepository, 
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository,
            PedidoRepository pedidoRepository,
            NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.pedidoRepository = pedidoRepository;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Obtiene todas las estadísticas del dashboard
     */
    @GetMapping("/general")
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        // Usuarios totales activos
        long totalUsuarios = usuarioRepository.count();
        stats.put("totalUsuarios", totalUsuarios);
        
        // Vehículos totales activos
        long totalVehiculos = vehiculoRepository.count();
        stats.put("totalVehiculos", totalVehiculos);
        
        // Vehículos activos
        long vehiculosActivos = vehiculoRepository.findByActivoTrue().size();
        stats.put("vehiculosActivos", vehiculosActivos);
        
        // Vehículos inactivos
        long vehiculosInactivos = vehiculoRepository.findByActivoFalse().size();
        stats.put("vehiculosInactivos", vehiculosInactivos);
        
        // Usuarios por rol
        stats.put("usuariosPorRol", usuarioRepository.findAll().stream()
            .filter(u -> u.getRol() != null)
            .collect(java.util.stream.Collectors.groupingByConcurrent(
                u -> u.getRol().getNombre(),
                java.util.stream.Collectors.counting()
            ))
        );
        
        // Vehículos por marca
        stats.put("vehiculosPorMarca", vehiculoRepository.findAll().stream()
            .filter(v -> v.getMarca() != null)
            .collect(java.util.stream.Collectors.groupingByConcurrent(
                v -> v.getMarca(),
                java.util.stream.Collectors.counting()
            ))
        );
        
        return stats;
    }

    /**
     * Obtiene solo usuarios activos
     */
    @GetMapping("/usuarios")
    public Map<String, Object> obtenerEstadisticasUsuarios() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("usuariosPorRol", usuarioRepository.findAll().stream()
            .filter(u -> u.getRol() != null)
            .collect(java.util.stream.Collectors.groupingByConcurrent(
                u -> u.getRol().getNombre(),
                java.util.stream.Collectors.counting()
            ))
        );
        return stats;
    }

    /**
     * Obtiene solo vehículos
     */
    @GetMapping("/vehiculos")
    public Map<String, Object> obtenerEstadisticasVehiculos() {
        Map<String, Object> stats = new HashMap<>();
        long totalVehiculos = vehiculoRepository.count();
        long vehiculosActivos = vehiculoRepository.findByActivoTrue().size();
        stats.put("totalVehiculos", totalVehiculos);
        stats.put("vehiculosActivos", vehiculosActivos);
        stats.put("vehiculosInactivos", totalVehiculos - vehiculosActivos);
        stats.put("vehiculosPorMarca", vehiculoRepository.findAll().stream()
            .filter(v -> v.getMarca() != null)
            .collect(java.util.stream.Collectors.groupingByConcurrent(
                v -> v.getMarca(),
                java.util.stream.Collectors.counting()
            ))
        );
        return stats;
    }

    /**
     * Obtiene estadísticas de conductores
     */
    @GetMapping("/conductores")
    public Map<String, Object> obtenerEstadisticasConductores() {
        Map<String, Object> stats = new HashMap<>();
        long totalConductores = conductorRepository.count();
        long conductoresConVehiculo = conductorRepository.findAll().stream()
            .filter(c -> c.getVehiculo() != null)
            .count();
        stats.put("totalConductores", totalConductores);
        stats.put("conductoresConVehiculo", conductoresConVehiculo);
        stats.put("conductoresSinVehiculo", totalConductores - conductoresConVehiculo);
        return stats;
    }

    /**
     * Obtiene estadísticas de pedidos/órdenes
     */
    @GetMapping("/pedidos")
    public Map<String, Object> obtenerEstadisticasPedidos() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPedidos", pedidoRepository.count());
        stats.put("pedidosPorEstado", pedidoRepository.findAll().stream()
            .collect(java.util.stream.Collectors.groupingByConcurrent(
                p -> p.getEstado() != null ? p.getEstado() : "sin_estado",
                java.util.stream.Collectors.counting()
            ))
        );
        
        // Alertas de pedidos pendientes
        long pedidosPendientes = pedidoRepository.findAll().stream()
            .filter(p -> "pendiente".equalsIgnoreCase(p.getEstado()))
            .count();
        stats.put("pedidosPendientes", pedidosPendientes);
        
        return stats;
    }

    /**
     * Obtiene alertas y adverstencias críticas
     */
    @GetMapping("/alertas")
    public Map<String, Object> obtenerAlertas() {
        Map<String, Object> alertas = new HashMap<>();
        
        // Notificaciones no leídas
        long notificacionesNoLeidas = notificacionRepository.findAll().stream()
            .filter(n -> !n.isLeida())
            .count();
        alertas.put("notificacionesNoLeidas", notificacionesNoLeidas);
        
        // Vehículos inactivos (posible mantenimiento)
        alertas.put("vehiculosInactivos", vehiculoRepository.findByActivoFalse().size());
        
        // Inventario bajo (menos de 5 vehículos disponibles)
        alertas.put("inventarioBajo", vehiculoRepository.findByActivoTrue().size() < 5);
        
        return alertas;
    }

    /**
     * Obtiene todas las estadísticas combinadas (para admin) - OPTIMIZADO
     */
    @GetMapping("/completo")
    public Map<String, Object> obtenerEstadisticasCompletas() {
        Map<String, Object> datos = new HashMap<>();
        
        // Usar el endpoint /general que ya es eficiente
        datos.putAll(obtenerEstadisticas());
        
        // Agregar conductores (una sola llamada)
        long totalConductores = conductorRepository.count();
        long conductoresConVehiculo = conductorRepository.findAll().stream()
            .filter(c -> c.getVehiculo() != null)
            .count();
        
        Map<String, Object> conductores = new HashMap<>();
        conductores.put("totalConductores", totalConductores);
        conductores.put("conductoresConVehiculo", conductoresConVehiculo);
        conductores.put("conductoresSinVehiculo", totalConductores - conductoresConVehiculo);
        datos.put("conductores", conductores);
        
        // Agregar pedidos (una sola llamada)
        long totalPedidos = pedidoRepository.count();
        Map<String, Object> pedidos = new HashMap<>();
        pedidos.put("totalPedidos", totalPedidos);
        pedidos.put("pedidosPorEstado", pedidoRepository.findAll().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                p -> p.getEstado() != null ? p.getEstado() : "sin_estado",
                java.util.stream.Collectors.counting()
            ))
        );
        datos.put("pedidos", pedidos);
        
        // Agregar alertas (una sola llamada optimizada)
        long notificacionesNoLeidas = notificacionRepository.findAll().stream()
            .filter(n -> !n.isLeida())
            .count();
        long vehiculosInactivos = vehiculoRepository.findByActivoFalse().size();
        
        Map<String, Object> alertas = new HashMap<>();
        alertas.put("notificacionesNoLeidas", notificacionesNoLeidas);
        alertas.put("vehiculosInactivos", vehiculosInactivos);
        alertas.put("inventarioBajo", vehiculoRepository.findByActivoTrue().size() < 5);
        datos.put("alertas", alertas);
        
        return datos;
    }
}
