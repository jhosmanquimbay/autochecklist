package com.example.demostracion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demostracion.model.Novedad;
import com.example.demostracion.repository.NovedadRepository;
import com.example.demostracion.repository.UsuarioRepository;

@Controller
@RequestMapping("/novedades")
@PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
public class NovedadController {

    private final NovedadRepository novedadRepository;
    private final UsuarioRepository usuarioRepository;

    private final String UPLOAD_DIR = "uploads/";

    public NovedadController(NovedadRepository novedadRepository, UsuarioRepository usuarioRepository) {
        this.novedadRepository = novedadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // 👉 Listar novedades
    @GetMapping
    public String index(Model model) {
        List<Novedad> novedades = novedadRepository.findAll();
        model.addAttribute("novedades", novedades);
        return "novedades/index";
    }

    // 👉 Mostrar formulario crear
    @GetMapping("/crear")
    public String crear(Model model) {
        model.addAttribute("novedad", new Novedad());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "novedades/form";
    }

    // 👉 Guardar nueva novedad
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Novedad novedad,
                          @RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            novedad.setEvidencia(fileName);
        }
        novedadRepository.save(novedad);
        return "redirect:/novedades";
    }

    // 👉 Editar novedad
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Novedad novedad = novedadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada: " + id));
        model.addAttribute("novedad", novedad);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "novedades/form";
    }

    // 👉 Actualizar novedad
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
                             @ModelAttribute Novedad novedad,
                             @RequestParam("file") MultipartFile file) throws IOException {
        Novedad existente = novedadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada: " + id));

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            existente.setEvidencia(fileName);
        }

        existente.setTipoNovedad(novedad.getTipoNovedad());
        existente.setDescripcion(novedad.getDescripcion());
        existente.setEstado(novedad.getEstado());
        existente.setUsuario(novedad.getUsuario());

        novedadRepository.save(existente);
        return "redirect:/novedades";
    }

    // 👉 Eliminar novedad
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        novedadRepository.deleteById(id);
        return "redirect:/novedades";
    }
}
