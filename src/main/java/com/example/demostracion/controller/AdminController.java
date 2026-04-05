package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.model.Inventario;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.service.InventarioVentaService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final InventarioRepository inventarioRepository;
    private final InventarioVentaService inventarioVentaService;

    public AdminController(InventarioRepository inventarioRepository,
                           InventarioVentaService inventarioVentaService) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioVentaService = inventarioVentaService;
    }

    @GetMapping
    public String adminHome() {
        return "admin/admin";
    }

    // Redirigir rutas de admin para usuarios y roles
    @GetMapping("/usuarios")
    public String adminUsuarios() {
        return "redirect:/usuarios";
    }

    @GetMapping("/roles")
    public String adminRoles() {
        return "redirect:/roles";
    }

    // Vista de inventario activo
    @GetMapping("/inventario")
    public String listarInventario(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findByActivoTrue());
        return "admin/admin"; // Usar el panel del admin
    }

    // Vista de inventario inactivo
    @GetMapping("/inventario/inactivos")
    public String listarInventarioInactivo(Model model) {
        model.addAttribute("inventarios", inventarioRepository.findByActivoFalse());
        return "admin/admin"; // Usar el panel del admin
    }

    // Editar inventario
    @GetMapping("/inventario/editar/{id}")
    public String editarInventario(@PathVariable Long id, Model model) {
        Inventario inventario = inventarioRepository.findById(id).orElseThrow();
        model.addAttribute("inventario", inventario);
        model.addAttribute("esAdmin", true);
        return "gerente/inventario/form";
    }

    @PostMapping("/inventario/editar/{id}")
    public String actualizarInventario(@PathVariable Long id, @ModelAttribute Inventario inventario) {
        inventario.setIdInventario(id);
        inventarioRepository.save(inventario);
        inventarioVentaService.sincronizarVehiculoConInventario(inventario);
        return "redirect:/admin/inventario";
    }

    // Cambiar estado de un vehículo
    @PostMapping("/inventario/cambiar-estado/{id}")
    public String cambiarEstadoInventario(@PathVariable Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));
        inventario.setActivo(!inventario.isActivo());
        inventarioRepository.save(inventario);
        inventarioVentaService.sincronizarVehiculoConInventario(inventario);
        return "redirect:/admin/inventario" + (inventario.isActivo() ? "/inactivos" : "");
    }

    // Crear nuevo inventario
    @GetMapping("/inventario/crear")
    public String crearInventarioForm(Model model) {
        model.addAttribute("inventario", new Inventario());
        model.addAttribute("esAdmin", true);
        return "gerente/inventario/form";
    }

    @PostMapping("/inventario/crear")
    public String guardarInventario(@ModelAttribute Inventario inventario) {
        if (inventario.getCantidadDisponible() == null || inventario.getCantidadDisponible() < 1) {
            inventario.setCantidadDisponible(1);
        }
        inventarioRepository.save(inventario);
        inventarioVentaService.sincronizarVehiculoConInventario(inventario);
        return "redirect:/admin/inventario";
    }
}