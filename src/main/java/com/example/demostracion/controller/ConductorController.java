package com.example.demostracion.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Notificacion;
import com.example.demostracion.repository.NotificacionRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import com.example.demostracion.service.ConductorService;
import com.example.demostracion.service.MensajeService;

@Controller
@RequestMapping("/conductor")
public class ConductorController {

    private final ConductorService conductorService;
    private final VehiculoRepository vehiculoRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MensajeService mensajeService;

    public ConductorController(
            ConductorService conductorService,
            VehiculoRepository vehiculoRepository,
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository,
            MensajeService mensajeService) {
        this.conductorService = conductorService;
        this.vehiculoRepository = vehiculoRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.mensajeService = mensajeService;
    }

    // =========================================
    // CRUD usado por el GERENTE (administra conductores)
    // =========================================
    @GetMapping
    public String listarConductores(Model model) {
        model.addAttribute("conductores", conductorService.listarTodos());
        return "conductor/lista";
    }

    @GetMapping("/crear")
    public String crearForm(Model model) {
        model.addAttribute("conductor", new Conductor());
        model.addAttribute("vehiculos", vehiculoRepository.findAll());
        return "conductor/form";
    }

    @PostMapping("/guardar")
    public String guardarConductor(@ModelAttribute Conductor conductor) {
        conductorService.guardar(conductor);
        return "redirect:/conductor";
    }

    @GetMapping("/editar/{id}")
    public String editarConductor(@PathVariable Long id, Model model) {
        Conductor conductor = conductorService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado: " + id));
        model.addAttribute("conductor", conductor);
        model.addAttribute("vehiculos", vehiculoRepository.findAll());
        return "conductor/form";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarConductor(@PathVariable Long id) {
        conductorService.eliminar(id);
        return "redirect:/conductor";
    }

    // =========================================
    // PANEL usado por el CONDUCTOR (su propia oficina)
    // =========================================
    @GetMapping("/panel")
    public String panelConductor(Model model, Principal principal, Authentication auth) {
        String correoLogin = principal.getName(); // ⚡ ahora el login es el correo

        Conductor conductor = conductorService.buscarPorUsername(correoLogin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conductor no encontrado para usuario: " + correoLogin));

        model.addAttribute("conductor", conductor);

        List<Notificacion> notificaciones = notificacionRepository.findAll().stream()
                .filter(n -> n.getConductor() != null &&
                        n.getConductor().getIdConductor().equals(conductor.getIdConductor()))
                .toList();

        model.addAttribute("notificaciones", notificaciones);

        // Agregar información del usuario para el menú de correo
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(usuario -> {
                model.addAttribute("usuarioId", usuario.getIdUsuario());
                model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
                model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
            });
        }

        return "conductor/panel";
    }

    @GetMapping("/panel/perfil")
    public String perfilConductor(Model model, Principal principal) {
        String correoLogin = principal.getName();

        Conductor conductor = conductorService.buscarPorUsername(correoLogin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conductor no encontrado para usuario: " + correoLogin));

        model.addAttribute("conductor", conductor);
        return "conductor/perfil";
    }

    @GetMapping("/panel/notificaciones")
    public String notificacionesConductor(Model model, Principal principal, Authentication auth) {
        String correoLogin = principal.getName();

        Conductor conductor = conductorService.buscarPorUsername(correoLogin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conductor no encontrado para usuario: " + correoLogin));

        List<Notificacion> notificaciones = notificacionRepository.findAll().stream()
                .filter(n -> n.getConductor() != null &&
                        n.getConductor().getIdConductor().equals(conductor.getIdConductor()))
                .toList();

        model.addAttribute("notificaciones", notificaciones);

        // Agregar información del usuario para el menú de correo
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(usuario -> {
                model.addAttribute("usuarioId", usuario.getIdUsuario());
                model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
                model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
            });
        }

        // 👇 Ajustado para tu carpeta actual
        return "conductor/notificaciones/notificaciones";
    }

    // ✅ Nuevo endpoint para marcar notificaciones como leídas
    @PostMapping("/panel/notificaciones/leida/{id}")
    public String marcarNotificacionLeida(@PathVariable Long id, Principal principal) {
        String correoLogin = principal.getName();

        Conductor conductor = conductorService.buscarPorUsername(correoLogin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conductor no encontrado para usuario: " + correoLogin));

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + id));

        // Validar que la notificación pertenece al conductor logueado
        if (notificacion.getConductor() != null &&
            notificacion.getConductor().getIdConductor().equals(conductor.getIdConductor())) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }

        return "redirect:/conductor/panel/notificaciones";
    }

    @GetMapping("/panel/vehiculo")
    public String vehiculoConductor(Model model, Principal principal) {
        String correoLogin = principal.getName();

        Conductor conductor = conductorService.buscarPorUsername(correoLogin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conductor no encontrado para usuario: " + correoLogin));

        model.addAttribute("vehiculo", conductor.getVehiculo());
        return "conductor/vehiculo";
    }
}
    