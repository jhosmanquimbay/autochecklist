package com.example.demostracion.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import com.example.demostracion.service.MensajeService;

@Controller
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final MensajeService mensajeService;

    public DashboardController(UsuarioRepository usuarioRepository,
                               VehiculoRepository vehiculoRepository,
                               InventarioRepository inventarioRepository,
                               MensajeService mensajeService) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.mensajeService = mensajeService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("total_usuarios", usuarioRepository.count());
        model.addAttribute("total_vehiculos", vehiculoRepository.count());
        model.addAttribute("total_inventario", inventarioRepository.count());
        
        // Agregar información del usuario para el menú de correo
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(usuario -> {
                model.addAttribute("usuarioId", usuario.getIdUsuario());
                model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
                model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
            });
        }
        
        return "dashboard"; // archivo dashboard.html en templates
    }
}
