package com.example.demostracion.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Notificacion;
import com.example.demostracion.service.ConductorService;
import com.example.demostracion.service.NotificacionService;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final ConductorService conductorService;

    public NotificacionController(NotificacionService notificacionService, ConductorService conductorService) {
        this.notificacionService = notificacionService;
        this.conductorService = conductorService;
    }

    // Gerente: listar todas las notificaciones
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("notificaciones", notificacionService.listarPorConductor(null));
        return "notificacion/lista"; // 📂 templates/notificacion/lista.html
    }

    // Gerente: crear notificación
    @GetMapping("/crear")
    public String crearForm(Model model) {
        model.addAttribute("notificacion", new Notificacion());
        model.addAttribute("conductores", conductorService.listarTodos());
        return "notificacion/form"; // 📂 templates/notificacion/form.html
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Notificacion notificacion) {
        notificacionService.guardar(notificacion);
        return "redirect:/notificaciones";
    }

    // Conductor: ver mis notificaciones
    @GetMapping("/conductor/{idConductor}")
    public String verPorConductor(@PathVariable Long idConductor, Model model) {
        Conductor conductor = conductorService.buscarPorId(idConductor)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado: " + idConductor));
        List<Notificacion> notificaciones = notificacionService.listarPorConductor(conductor);
        model.addAttribute("notificaciones", notificaciones);
        return "conductor/notificaciones"; // 📂 templates/conductor/notificaciones.html
    }
    
}
