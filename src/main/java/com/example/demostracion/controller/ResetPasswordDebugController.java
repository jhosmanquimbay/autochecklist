package com.example.demostracion.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.service.ClienteCuentaService;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.UsuarioRepository;
import java.util.List;

/**
 * DEBUG ONLY: Rehabilita contraseñas de texto plano a BCrypt
 * Acceso: GET /reset-passwords-debug
 */
@Controller
@RequestMapping("/reset-passwords-debug")
public class ResetPasswordDebugController {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordDebugController.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteCuentaService clienteCuentaService;

    public ResetPasswordDebugController(UsuarioRepository usuarioRepository,
                                        PasswordEncoder passwordEncoder,
                                        ClienteCuentaService clienteCuentaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteCuentaService = clienteCuentaService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("message", "Endpoint para rehashear contraseñas");
        return "debug/reset-passwords";
    }

    /**
     * Rehashea TODAS las contraseñas no-BCrypt
     * Las contraseñas COMUNES se hashean con el mismo valor
     * Ejemplo: "admin123" -> "$2a$12$..."
     */
    @PostMapping("/rehash-all")
    @Transactional
    public String rehashAll(Model model) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            int actualizados = 0;
            
            for (Usuario u : usuarios) {
                if (u.getContrasena() == null || u.getContrasena().isEmpty()) {
                    continue;
                }
                
                // Si NO es BCrypt (no comienza con $2a$ o $2b$), rehashear
                if (!u.getContrasena().startsWith("$2a$") && !u.getContrasena().startsWith("$2b$")) {
                    String plainPassword = u.getContrasena();
                    String hashedPassword = passwordEncoder.encode(plainPassword);
                    u.setContrasena(hashedPassword);
                    usuarioRepository.save(u);
                    actualizados++;
                }
            }
            
            model.addAttribute("success", "✅ Se actualizaron " + actualizados + " contraseñas a BCrypt");
        } catch (Exception e) {
            model.addAttribute("error", "❌ Error: " + e.getMessage());
            log.error("Error rehasheando contraseñas", e);
        }
        
        return "debug/reset-passwords";
    }

    /**
     * Establece contraseñas seguras personalizadas
     * Patrón: 2 MAYÚS + 2 números + 1 especial
     */
    @PostMapping("/reset-to-secure")
    @Transactional
    public String resetToSecure(Model model) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            for (Usuario u : usuarios) {
                String plainPassword;
                
                // Asignar contraseña según el correo o rol
                if (u.getCorreo().contains("wendy") || u.getCorreo().contains("admin")) {
                    plainPassword = "AB12!Pass";
                } else if (u.getCorreo().contains("gerente")) {
                    plainPassword = "CD34@Pass";
                } else if (u.getCorreo().contains("rsaenz") || u.getCorreo().contains("stiven")) {
                    plainPassword = "EF56#Pass";
                } else if (u.getCorreo().contains("conductor") || u.getCorreo().contains("david") || u.getCorreo().contains("jairo") || u.getCorreo().contains("jhosman")) {
                    plainPassword = "GH78$Pass";
                } else {
                    plainPassword = "IJ90%Pass";
                }
                
                String hashedPassword = passwordEncoder.encode(plainPassword);
                u.setContrasena(hashedPassword);
                usuarioRepository.save(u);
            }
            
            model.addAttribute("success", "✅ Todas las contraseñas cambiadas a valores seguros");
            model.addAttribute("passwords", true);
        } catch (Exception e) {
            model.addAttribute("error", "❌ Error: " + e.getMessage());
            log.error("Error asignando contraseñas seguras", e);
        }
        
        return "debug/reset-passwords";
    }

    @PostMapping("/reset-gratis2021")
    @Transactional
    public String resetGratis2021(Model model) {
        try {
            int total = clienteCuentaService.resetearTodasLasContrasenas("Gratis2021**");
            model.addAttribute("success", "✅ Se actualizaron " + total + " contraseñas con el valor Gratis2021**");
        } catch (Exception e) {
            model.addAttribute("error", "❌ Error: " + e.getMessage());
            log.error("Error actualizando contraseñas a Gratis2021**", e);
        }

        return "debug/reset-passwords";
    }
}
