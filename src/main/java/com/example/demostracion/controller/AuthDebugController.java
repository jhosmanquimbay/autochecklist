package com.example.demostracion.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.service.UsuarioDetailsService;

@Controller
@RequestMapping("/auth-debug")
public class AuthDebugController {

    private final UsuarioDetailsService usuarioDetailsService;

    public AuthDebugController(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @GetMapping("/current-user")
    public String currentUserDebug(Model model, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (principal != null) {
            try {
                // Cargar detalles del usuario usando el servicio
                UserDetails userDetails = usuarioDetailsService.loadUserByUsername(principal.getName());
                
                model.addAttribute("username", principal.getName());
                model.addAttribute("authenticated", auth.isAuthenticated());
                model.addAttribute("authorities", auth.getAuthorities());
                model.addAttribute("userDetailsAuthorities", userDetails.getAuthorities());
                model.addAttribute("hasRoleAdmin", auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
                model.addAttribute("hasRoleGerente", auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE")));
                
            } catch (Exception e) {
                model.addAttribute("error", "Error cargando usuario: " + e.getMessage());
            }
        } else {
            model.addAttribute("error", "No hay usuario autenticado");
        }
        
        return "debug/auth-info";
    }
}