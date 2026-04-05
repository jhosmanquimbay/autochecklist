package com.example.demostracion.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.repository.UsuarioRepository;

@Controller
@RequestMapping("/clima")
public class ClimaController {

    private final UsuarioRepository usuarioRepository;

    public ClimaController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String clima(Authentication auth, Model model) {
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(u -> {
                model.addAttribute("usuarioId", u.getIdUsuario());
                model.addAttribute("usuarioNombre", u.getNombre());
                model.addAttribute("usuarioRol", u.getRol() != null ? u.getRol().getNombre() : "Usuario");
            });
        }
        return "clima/clima";
    }
}