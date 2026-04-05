package com.example.demostracion.controller;

import com.example.demostracion.model.Mensaje;
import com.example.demostracion.service.MensajeService;
import com.example.demostracion.service.AdjuntoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/mensaje")
public class MensajeController {

    private final MensajeService mensajeService;
    private final AdjuntoService adjuntoService;

    public MensajeController(MensajeService mensajeService, AdjuntoService adjuntoService) {
        this.mensajeService = mensajeService;
        this.adjuntoService = adjuntoService;
    }

    // -----------------------------------------
    // 📌 NUEVAS RUTAS PARA EL DASHBOARD
    // -----------------------------------------

    // 📥 Inbox
    @GetMapping("/inbox")
    public String inbox(Model model) {
        model.addAttribute("mensajes", mensajeService.listarPorCarpeta("inbox"));
        return "correo/inbox"; // Vista del inbox
    }

    // 📤 Enviados
    @GetMapping("/enviados")
    public String enviados(Model model) {
        model.addAttribute("mensajes", mensajeService.listarPorCarpeta("sent"));
        return "correo/enviados";
    }

    // 📝 Redactar nuevo mensaje
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("mensaje", new Mensaje());
        return "correo/nuevo";
    }

    // -----------------------------------------
    // 📌 FUNCIONALIDADES EXISTENTES
    // -----------------------------------------

    // 📌 Ver un mensaje
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Mensaje mensaje = mensajeService.verMensaje(id);
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("adjuntos", mensaje.getAdjuntos());
        return "correo/ver";
    }

    // 📌 Marcar como leído
    @PostMapping("/marcar-leido/{id}")
    public String marcarLeido(@PathVariable Long id) {
        mensajeService.marcarComoLeido(id);
        return "redirect:/mensaje/ver/" + id;
    }

    // 📌 Mover mensaje a otra carpeta
    @PostMapping("/mover/{id}")
    public String mover(@PathVariable Long id, @RequestParam String carpeta) {
        mensajeService.moverCarpeta(id, carpeta);
        return "redirect:/mensaje/ver/" + id;
    }

    // 📌 Eliminar → mueve a la papelera
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        mensajeService.moverCarpeta(id, "trash");
        return "redirect:/mensaje/inbox";
    }

    // 📌 Eliminar DEFINITIVO
    @PostMapping("/eliminar-def/{id}")
    public String eliminarDef(@PathVariable Long id) {
        mensajeService.eliminarDefinitivo(id);
        return "redirect:/mensaje/trash";
    }

    // 📌 Descargar adjunto
    @GetMapping("/adjunto/{id}")
    public ResponseEntity<byte[]> descargarAdjunto(@PathVariable Long id) {
        return adjuntoService.descargarAdjunto(id);
    }

    // 📌 Papelera
    @GetMapping("/trash")
    public String trash(Model model) {
        model.addAttribute("mensajes", mensajeService.listarPorCarpeta("trash"));
        return "correo/trash";
    }
}
