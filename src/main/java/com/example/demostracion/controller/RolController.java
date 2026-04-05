package com.example.demostracion.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demostracion.model.Rol;
import com.example.demostracion.repository.RolRepository;

@Controller
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private final RolRepository rolRepository;

    public RolController(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    // Listar roles
    @GetMapping
    public String index(Model model) {
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("roles", roles);
        return "roles/index"; // vista roles/index.html
    }

    // Mostrar formulario crear
    @GetMapping("/crear")
    public String crear(Model model) {
        model.addAttribute("rol", new Rol());
        return "roles/form"; // vista roles/form.html
    }

    // Guardar rol
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Rol rol) {
        rolRepository.save(rol);
        return "redirect:/roles";
    }

    // Editar rol
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Rol rol = rolRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
        model.addAttribute("rol", rol);
        return "roles/form";
    }

    // Actualizar rol
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Rol rol) {
        rol.setIdRol(id);
        rolRepository.save(rol);
        return "redirect:/roles";
    }

    // Eliminar rol
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        rolRepository.deleteById(id);
        return "redirect:/roles";
    }
}
