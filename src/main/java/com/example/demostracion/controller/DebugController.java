package com.example.demostracion.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/user")
    public String debugUser(Model model, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        model.addAttribute("username", principal != null ? principal.getName() : "No autenticado");
        model.addAttribute("authorities", auth != null ? auth.getAuthorities() : "Sin autoridades");
        model.addAttribute("authenticated", auth != null ? auth.isAuthenticated() : false);
        
        return "debug/user-info";
    }
}