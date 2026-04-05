package com.example.demostracion.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demostracion.model.Rol;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.RolRepository;
import com.example.demostracion.service.UsuarioService;

@Controller
@RequestMapping("/setup")
public class SetupController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(UsuarioService usuarioService, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String setupPage(Model model) {
        long userCount = usuarioService.listarUsuarios().size();
        model.addAttribute("userCount", userCount);
        return "setup/setup";
    }

    @PostMapping("/create-admin")
    public String createAdmin(Model model) {
        try {
            // Crear rol ADMIN si no existe
            Rol adminRole = rolRepository.findByNombre("ADMIN")
                    .orElseGet(() -> {
                        Rol newRole = new Rol();
                        newRole.setNombre("ADMIN");
                        return rolRepository.save(newRole);
                    });

            // Crear usuario admin si no existe
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setCorreo("admin@test.com");
            admin.setContrasena(passwordEncoder.encode("admin123"));  // ✅ Hashear con BCrypt
            admin.setRol(adminRole);
            
            usuarioService.guardar(admin);
            
            model.addAttribute("success", "Usuario admin creado exitosamente. Email: admin@test.com, Password: admin123");
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear admin: " + e.getMessage());
        }
        
        return "setup/setup";
    }

    // DEBUG: Permite resetear contraseña de un usuario (solo entorno de desarrollo)
    @PostMapping("/debug/reset-password")
    public String resetPassword(Model model,
                                @RequestParam String correo,
                                @RequestParam String password) {
        try {
            var usuarioOpt = usuarioService.listarUsuarios().stream()
                    .filter(u -> correo.equalsIgnoreCase(u.getCorreo()))
                    .findFirst();
            if (usuarioOpt.isEmpty()) {
                model.addAttribute("error", "Usuario no encontrado: " + correo);
                return "setup/setup";
            }
            var u = usuarioOpt.get();
            u.setContrasena(passwordEncoder.encode(password));
            usuarioService.guardar(u);
            model.addAttribute("success", "Contraseña restablecida para " + correo + ".");
        } catch (Exception e) {
            model.addAttribute("error", "Error al restablecer contraseña: " + e.getMessage());
        }
        return "setup/setup";
    }
}