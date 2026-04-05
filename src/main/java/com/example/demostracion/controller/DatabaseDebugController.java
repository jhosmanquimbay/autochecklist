package com.example.demostracion.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demostracion.model.Rol;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.RolRepository;
import com.example.demostracion.service.UsuarioService;

@RestController
@RequestMapping("/db-debug")
public class DatabaseDebugController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    public DatabaseDebugController(UsuarioService usuarioService, RolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
    }

    @GetMapping("/roles")
    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    @GetMapping("/usuarios")
    public List<Usuario> getAllUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/wendy")
    public String findWendy() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        for (Usuario u : usuarios) {
            if (u.getNombre().toLowerCase().contains("wendy") || 
                u.getCorreo().toLowerCase().contains("wendy")) {
                return "Usuario encontrado: " + u.getNombre() + " - " + u.getCorreo() + 
                       " - Rol: " + (u.getRol() != null ? u.getRol().getNombre() : "Sin rol") +
                       " - Rol ID: " + (u.getRol() != null ? u.getRol().getIdRol() : "Sin rol") +
                       " - Authority esperada: ROLE_" + (u.getRol() != null ? u.getRol().getNombre() : "Sin rol");
            }
        }
        return "Usuario wendy no encontrado";
    }

    @GetMapping("/rol/{id}")
    public String getRolById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return rolRepository.findById(id)
                .map(rol -> "Rol ID " + id + ": " + rol.getNombre())
                .orElse("Rol con ID " + id + " no encontrado");
    }
}