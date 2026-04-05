package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.repository.RolRepository;
import com.example.demostracion.service.UsuarioService;

@Controller
@RequestMapping("/info")
public class InfoController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    public InfoController(UsuarioService usuarioService, RolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
    }

    @GetMapping("/users-roles")
    public String showUsersAndRoles(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("roles", rolRepository.findAll());
        return "info/users-roles";
    }
}