package com.example.demostracion.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.model.Rol;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.RolRepository;
import com.example.demostracion.service.UsuarioService;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 👉 Listar todos
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "Usuarios/listar";
    }

    // 👉 Mostrar formulario nuevo
    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolRepository.findAll());
        return "Usuarios/crear";
    }

    // 👉 Guardar usuario
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getIdRol() != null) {
            Rol rol = rolRepository.findById(usuario.getRol().getIdRol())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            usuario.setRol(rol);
        }
        // ✅ Hashear contraseña con BCrypt
        if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        usuarioService.guardar(usuario);
        return "redirect:/usuarios"; // vuelve al listado
    }

    // 👉 Mostrar formulario editar
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolRepository.findAll());
        return "Usuarios/editar";
    }

    // 👉 Actualizar usuario
    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario) {
        if (usuario.getRol() != null && usuario.getRol().getIdRol() != null) {
            Rol rol = rolRepository.findById(usuario.getRol().getIdRol())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            usuario.setRol(rol);
        }
        // ✅ Hashear contraseña con BCrypt si se proporciona
        if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        usuarioService.guardar(usuario);
        return "redirect:/usuarios";
    }

    // 👉 Eliminar usuario
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return "redirect:/usuarios";
    }
}
