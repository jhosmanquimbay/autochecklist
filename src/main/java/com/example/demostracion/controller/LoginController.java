package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Muestra la página de login
    @GetMapping("/login")
    public String login() {
        return "login"; // carga login.html
    }
}
