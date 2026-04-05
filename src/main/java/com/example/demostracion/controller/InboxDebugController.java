package com.example.demostracion.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demostracion.model.Mensaje;
import com.example.demostracion.repository.MensajeRepository;

@RestController
@RequestMapping("/inbox-debug")
public class InboxDebugController {

    private final MensajeRepository mensajeRepository;

    public InboxDebugController(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @GetMapping("/recent")
    public List<Mensaje> getRecentMessages() {
        // Obtener los últimos 10 mensajes ordenados por fecha
        return mensajeRepository.findTop10ByOrderByFechaEnvioDesc();
    }

    @GetMapping("/inbox")
    public List<Mensaje> getInboxMessages() {
        // Obtener mensajes de la carpeta inbox
        return mensajeRepository.findByCarpetaOrderByFechaEnvioDesc("inbox");
    }

    @GetMapping("/count")
    public String getMessageCount() {
        long total = mensajeRepository.count();
        long inbox = mensajeRepository.countByCarpeta("inbox");
        long enviados = mensajeRepository.countByCarpeta("enviados");
        
        return String.format("Total: %d, Inbox: %d, Enviados: %d", total, inbox, enviados);
    }
}