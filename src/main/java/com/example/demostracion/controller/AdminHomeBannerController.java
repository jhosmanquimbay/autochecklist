package com.example.demostracion.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.HomeBannerForm;
import com.example.demostracion.service.HomeBannerService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/promociones")
public class AdminHomeBannerController {

    private final HomeBannerService homeBannerService;

    public AdminHomeBannerController(HomeBannerService homeBannerService) {
        this.homeBannerService = homeBannerService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("banners", homeBannerService.listarTodos());
        return "admin/promociones";
    }

    @GetMapping("/crear")
    public String crearForm(Model model) {
        prepararFormulario(model, new HomeBannerForm(), false);
        return "admin/promociones-form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("bannerForm") HomeBannerForm bannerForm,
                        BindingResult bindingResult,
                        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, bannerForm, false);
            return "admin/promociones-form";
        }

        try {
            homeBannerService.crear(bannerForm, imageFile);
            redirectAttributes.addFlashAttribute("success", "Promoción creada correctamente.");
            return "redirect:/admin/promociones";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            prepararFormulario(model, bannerForm, false);
            model.addAttribute("uploadError", ex.getMessage());
            return "admin/promociones-form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            prepararFormulario(model, homeBannerService.obtenerPorId(id), true);
            return "admin/promociones-form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/promociones";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable String id,
                             @Valid @ModelAttribute("bannerForm") HomeBannerForm bannerForm,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            bannerForm.setId(id);
            prepararFormulario(model, bannerForm, true);
            model.addAttribute("bannerId", id);
            return "admin/promociones-form";
        }

        try {
            homeBannerService.actualizar(id, bannerForm, imageFile);
            redirectAttributes.addFlashAttribute("success", "Promoción actualizada correctamente.");
            return "redirect:/admin/promociones";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bannerForm.setId(id);
            prepararFormulario(model, bannerForm, true);
            model.addAttribute("bannerId", id);
            model.addAttribute("uploadError", ex.getMessage());
            return "admin/promociones-form";
        }
    }

    @PostMapping("/{id}/publicacion")
    public String cambiarPublicacion(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            homeBannerService.cambiarEstadoPublicacion(id);
            redirectAttributes.addFlashAttribute("success", "Estado de publicación actualizado.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/promociones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            homeBannerService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Promoción eliminada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/promociones";
    }

    private void prepararFormulario(Model model, HomeBannerForm form, boolean modoEdicion) {
        model.addAttribute("bannerForm", form);
        model.addAttribute("bannerId", form.getId());
        model.addAttribute("modoEdicion", modoEdicion);
        model.addAttribute("themes", List.of("gold", "blue", "emerald"));
    }
}