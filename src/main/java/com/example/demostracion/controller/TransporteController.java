package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransporteController {

    @GetMapping("/transporte")
    public String transporteMenu() {
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/transporte/seguimiento")
    public String seguimientoEnvios() {
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/transporte/programacion")
    public String programarTransporte() {
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/transporte/documentos")
    public String documentos() {
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/transporte/detalle/{id}")
    public String detalleEnvio() {
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/gerente/transporte")
    public String transporteGerente() {
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/gerente/transporte/**")
    public String transporteGerenteSubrutas() {
        return "redirect:/gerente/inventario";
    }
}
