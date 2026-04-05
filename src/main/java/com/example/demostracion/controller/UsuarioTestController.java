package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.service.UsuarioService;

@Controller
@RequestMapping("/usuarios-test")
public class UsuarioTestController {

    private final UsuarioService usuarioService;

    public UsuarioTestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String testUsuarios(Model model) {
        try {
            model.addAttribute("usuarios", usuarioService.listarUsuarios());
            model.addAttribute("mensaje", "Datos cargados correctamente");
            return "Usuarios/listar";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "error/error";
        }
    }
}