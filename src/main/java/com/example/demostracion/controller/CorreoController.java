package com.example.demostracion.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.model.Mensaje;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.MensajeRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.service.AdjuntoService;
import com.example.demostracion.service.EmailService;
import com.example.demostracion.service.InboundMailService;
import com.example.demostracion.service.MensajeService;

@Controller
@RequestMapping("/correo")
public class CorreoController {

    private final MensajeService mensajeService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final AdjuntoService adjuntoService;
    private final InboundMailService inboundMailService;
    private final MensajeRepository mensajeRepository;

    public CorreoController(MensajeService mensajeService,
                            UsuarioRepository usuarioRepository,
                            EmailService emailService,
                            AdjuntoService adjuntoService,
                            InboundMailService inboundMailService,
                            MensajeRepository mensajeRepository) {
        this.mensajeService = mensajeService;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.adjuntoService = adjuntoService;
        this.inboundMailService = inboundMailService;
        this.mensajeRepository = mensajeRepository;
    }

    // Autocomplete simple (usa tu repo)
    @GetMapping("/api/search-usuarios")
    @ResponseBody
    public List<java.util.Map<String, String>> searchUsuarios(@RequestParam String q) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getCorreo() != null && u.getCorreo().toLowerCase().contains(q.toLowerCase()))
                .limit(10)
                .map(u -> java.util.Map.of(
                        "correo", u.getCorreo(),
                        "nombre", u.getNombre() != null ? u.getNombre() : u.getCorreo(),
                        "id", String.valueOf(u.getIdUsuario())
                ))
                .collect(Collectors.toList());
    }

    // Redirect autenticado -> inbox/{id}
    @GetMapping("/inbox")
    public String inboxAutenticado(Authentication auth) {
        if (auth == null || auth.getName() == null) return "redirect:/login";
        return usuarioRepository.findByCorreo(auth.getName())
                .map(u -> "redirect:/correo/inbox/" + u.getIdUsuario())
                .orElse("redirect:/login");
    }

    // Redirect autenticado -> enviados/{id}
    @GetMapping("/enviados")
    public String enviadosAutenticado(Authentication auth) {
        if (auth == null || auth.getName() == null) return "redirect:/login";
        return usuarioRepository.findByCorreo(auth.getName())
                .map(u -> "redirect:/correo/enviados/" + u.getIdUsuario())
                .orElse("redirect:/login");
    }

    // Redirect autenticado -> papelera/{id}
    @GetMapping("/papelera")
    public String papeleraAutenticado(Authentication auth) {
        if (auth == null || auth.getName() == null) return "redirect:/login";
        return usuarioRepository.findByCorreo(auth.getName())
                .map(u -> "redirect:/correo/papelera/" + u.getIdUsuario())
                .orElse("redirect:/login");
    }

    @GetMapping("/inbox/{id}")
    public String inbox(@PathVariable Long id, Model model) {
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        model.addAttribute("mensajes", mensajeService.recibir(id));
        agregarContextoUsuario(model, u);
        return "correo/inbox";
    }

    // Ruta de diagnóstico para verificar el estado
    @GetMapping("/debug/{id}")
    public String debug(@PathVariable Long id, Model model) {
        try {
            Usuario u = usuarioRepository.findById(id).orElse(null);
            if (u == null) {
                model.addAttribute("error", "Usuario no encontrado con ID: " + id);
                model.addAttribute("usuarios", usuarioRepository.findAll());
                return "correo/debug";
            }
            
            List<Mensaje> mensajes = mensajeService.recibir(id);
            model.addAttribute("usuario", u);
            model.addAttribute("mensajes", mensajes);
            model.addAttribute("totalMensajes", mensajes.size());
            agregarContextoUsuario(model, u);
            
            return "correo/debug";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("usuarios", usuarioRepository.findAll());
            return "correo/debug";
        }
    }

    @GetMapping("/enviados/{id}")
    public String enviados(@PathVariable Long id, Model model) {
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        model.addAttribute("mensajes", mensajeService.enviados(id));
        agregarContextoUsuario(model, u);
        return "correo/enviados";
    }

    // enviar desde modal / form
    @PostMapping("/enviar")
    public String enviar(@RequestParam Long remitente,
                         @RequestParam String destinatarios,
                         @RequestParam String asunto,
                         @RequestParam String contenido,
                         @RequestParam(value = "origen", defaultValue = "inbox") String origen,
                         @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                         RedirectAttributes ra) throws Exception {

        System.out.println("\n\n========== ENDPOINT /correo/enviar LLAMADO ==========");
        System.out.println("[CorreoController] Remitente: " + remitente);
        System.out.println("[CorreoController] Destinatarios: " + destinatarios);
        System.out.println("[CorreoController] Asunto: " + asunto);
        System.out.println("[CorreoController] Contenido: " + (contenido != null ? contenido.substring(0, Math.min(50, contenido.length())) : "NULL"));
        System.out.println("[CorreoController] Archivos: " + (archivos != null ? archivos.length : "NULL"));

        if (destinatarios == null || destinatarios.isBlank()) {
            System.err.println("[CorreoController] ERROR: destinatarios vacío");
            ra.addFlashAttribute("msg", "Debe agregar destinatarios");
            ra.addFlashAttribute("msgType", "danger");
            return redirigirABandeja(origen, remitente);
        }

        // Validar que el contenido no esté vacío
        if (contenido == null || contenido.trim().isEmpty()) {
            System.err.println("[CorreoController] ERROR: contenido vacío");
            ra.addFlashAttribute("msg", "El mensaje no puede estar vacío");
            ra.addFlashAttribute("msgType", "danger");
            return redirigirABandeja(origen, remitente);
        }

        // Cambio de Set a List para permitir correos duplicados
        // Si el usuario escribe el mismo correo varias veces, se enviará varias veces
        List<String> listaCorreos = Arrays.stream(destinatarios.split("[,;\\s]+"))
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());

        System.out.println("[CorreoController] Correos parseados: " + listaCorreos.size());

        int internos = 0, externos = 0, errores = 0;

        for (String correo : listaCorreos) {
            try {
                String correoNormalizado = normalizarCorreo(correo);
                System.out.println("[CorreoController] Procesando correo: " + correoNormalizado);
                Optional<Usuario> usuarioOpt = buscarUsuarioPorCorreo(correoNormalizado);
                if (usuarioOpt.isPresent()) {
                    System.out.println("[CorreoController] ✓ Es usuario interno, llamando enviarMensaje()");
                    mensajeService.enviarMensaje(remitente, usuarioOpt.get().getIdUsuario(), asunto, contenido, archivos);
                    internos++;
                    System.out.println("[CorreoController] ✓ enviarMensaje() completado");
                } else {
                    System.out.println("[CorreoController] Validando formato de correo externo: " + correoNormalizado);
                    if (!correoNormalizado.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        System.err.println("[CorreoController] ✗ Formato inválido: " + correoNormalizado);
                        errores++;
                        continue;
                    }
                    System.out.println("[CorreoController] ✓ Es correo externo, llamando enviarAExterna()");
                    mensajeService.enviarAExterna(remitente, correoNormalizado, asunto, contenido, archivos);
                    externos++;
                    System.out.println("[CorreoController] ✓ enviarAExterna() completado");
                }
            } catch (Exception e) {
                System.err.println("[CorreoController] ✗ EXCEPCION al procesar " + correo);
                e.printStackTrace();
                errores++;
            }
        }

        String msgFinal = "Internos: " + internos + " | Externos: " + externos + " | Errores: " + errores;
        System.out.println("[CorreoController] RESULTADO FINAL: " + msgFinal);
        System.out.println("========== FIN ENDPOINT /correo/enviar ==========\n\n");

        ra.addFlashAttribute("msg", msgFinal);
        ra.addFlashAttribute("msgType", errores == 0 ? "success" : "warning");
        return redirigirABandeja(origen, remitente);
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Authentication auth, Model model) {
        Mensaje mensaje = mensajeService.verMensaje(id);
        if (auth != null) mensajeService.marcarComoLeido(id);

        model.addAttribute("mensaje", mensaje);
        model.addAttribute("adjuntos", mensaje.getAdjuntos());
        // cargar respuestas (hijos) para mostrarlas en la columna derecha
        model.addAttribute("respuestas", mensajeService.getRespuestas(id));
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(u -> agregarContextoUsuario(model, u));
        }
        return "correo/ver";
    }

    // Endpoint para devolver solo el fragmento HTML del mensaje (para cargar en la bandeja)
    @GetMapping("/fragment/{id}")
    public String verFragment(@PathVariable Long id, Authentication auth, Model model) {
        Mensaje mensaje = mensajeService.verMensaje(id);
        if (auth != null) mensajeService.marcarComoLeido(id);

        model.addAttribute("mensaje", mensaje);
        model.addAttribute("adjuntos", mensaje.getAdjuntos());
        model.addAttribute("respuestas", mensajeService.getRespuestas(id));
        if (auth != null) {
            usuarioRepository.findByCorreo(auth.getName()).ifPresent(u -> agregarContextoUsuario(model, u));
        }
        return "correo/verFragment";
    }

    @GetMapping("/adjunto/{id}")
    public org.springframework.http.ResponseEntity<byte[]> descargarAdjunto(@PathVariable Long id) {
        return adjuntoService.descargarAdjunto(id);
    }

    @PostMapping("/responder")
    public String responder(@RequestParam Long mensajePadreId,
                            @RequestParam Long remitente,
                            @RequestParam String contenido,
                            @RequestParam(value = "origen", defaultValue = "inbox") String origen,
                            @RequestParam(value = "archivos", required = false) MultipartFile[] archivos,
                            RedirectAttributes ra) {
        try {
            mensajeService.responder(mensajePadreId, remitente, contenido, archivos);
            ra.addFlashAttribute("msg", "Respuesta enviada correctamente");
            ra.addFlashAttribute("msgType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("msg", "Error al enviar la respuesta");
            ra.addFlashAttribute("msgType", "danger");
        }
        return redirigirABandeja(origen, remitente);
    }

    // Papelera
    @GetMapping("/papelera/{id}")
    public String papelera(@PathVariable Long id, Model model) {
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        model.addAttribute("mensajes", mensajeService.papelera(id));
        agregarContextoUsuario(model, u);
        return "correo/papelera";
    }

    // Mover a papelera
    @PostMapping("/mover-papelera/{id}")
    public String moverAPapelera(@PathVariable Long id,
                                 @RequestParam Long usuarioId,
                                 @RequestParam(value = "origen", defaultValue = "inbox") String origen,
                                 RedirectAttributes ra) {
        try {
            mensajeService.moverAPapelera(id);
            ra.addFlashAttribute("msg", "Mensaje movido a papelera");
            ra.addFlashAttribute("msgType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("msg", "Error al mover a papelera");
            ra.addFlashAttribute("msgType", "danger");
        }
        return redirigirABandeja(origen, usuarioId);
    }

    // Restaurar de papelera
    @PostMapping("/restaurar/{id}")
    public String restaurarDePapelera(@PathVariable Long id, @RequestParam Long usuarioId, RedirectAttributes ra) {
        try {
            mensajeService.restaurarDePapelera(id, usuarioId);
            ra.addFlashAttribute("msg", "Mensaje restaurado");
            ra.addFlashAttribute("msgType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("msg", "Error al restaurar");
            ra.addFlashAttribute("msgType", "danger");
        }
        return "redirect:/correo/papelera/" + usuarioId;
    }

    // Vaciar papelera
    @PostMapping("/vaciar-papelera/{id}")
    public String vaciarPapelera(@PathVariable Long id, RedirectAttributes ra) {
        try {
            int eliminados = mensajeService.vaciarPapelera(id);
            ra.addFlashAttribute("msg", "Papelera vaciada: " + eliminados + " mensaje(s) eliminado(s) permanentemente");
            ra.addFlashAttribute("msgType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("msg", "Error al vaciar papelera");
            ra.addFlashAttribute("msgType", "danger");
        }
        return "redirect:/correo/papelera/" + id;
    }

    // Eliminar permanentemente un email individual
    @PostMapping("/eliminar-permanente/{id}")
    public String eliminarPermanente(@PathVariable Long id, @RequestParam Long usuarioId, RedirectAttributes ra) {
        try {
            mensajeService.eliminarDefinitivo(id);
            ra.addFlashAttribute("msg", "Mensaje eliminado permanentemente");
            ra.addFlashAttribute("msgType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("msg", "Error al eliminar el mensaje");
            ra.addFlashAttribute("msgType", "danger");
        }
        return "redirect:/correo/papelera/" + usuarioId;
    }

    // Eliminar múltiples emails (AJAX)
    @PostMapping("/eliminar-multiples")
    @ResponseBody
    public java.util.Map<String, Object> eliminarMultiples(@RequestBody java.util.Map<String, Object> request) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Object idsObj = request.get("ids");
            List<Long> ids = new java.util.ArrayList<>();
            if (idsObj instanceof List<?>) {
                for (Object o : (List<?>) idsObj) {
                    try {
                        ids.add(Long.valueOf(o.toString()));
                    } catch (Exception ignore) {
                    }
                }
            }

            if (ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("message", "No se especificaron IDs");
                return response;
            }
            
            for (Long id : ids) {
                // Si el mensaje ya está en papelera (eliminado == true) lo marcamos como eliminado permanente
                // Si no está en papelera, lo movemos a papelera (borrado lógico)
                try {
                    Mensaje m = mensajeService.verMensaje(id);
                    if (m.isEliminado()) {
                        mensajeService.marcarEliminadoPermanente(id);
                    } else {
                        mensajeService.moverAPapelera(id);
                    }
                } catch (Exception ex) {
                    // Ignorar mensajes inexistentes y continuar con el resto
                    System.err.println("Error procesando id=" + id + " en eliminarMultiples: " + ex.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", ids.size() + " mensaje(s) eliminado(s)");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar: " + e.getMessage());
        }
        return response;
    }

    // Marcar/desmarcar como importante (AJAX)
    @PostMapping("/toggle-importante/{id}")
    @ResponseBody
    public java.util.Map<String, Object> toggleImportante(@PathVariable Long id) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Mensaje msg = mensajeService.verMensaje(id);
            // Por ahora, solo respondemos OK - el cambio visual se hace en cliente
            // Para persistencia real, necesitaríamos agregar un campo 'importante' a la BDD
            response.put("success", true);
            response.put("message", "Estado actualizado");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // Correos masivos
    @GetMapping("/masivos/{id}")
    public String correosMasivos(@PathVariable Long id, Model model) {
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        agregarContextoUsuario(model, u);
        model.addAttribute("emailRequest", new com.example.demostracion.dto.EmailWebRequest());
        return "correo/masivos";
    }

    private void agregarContextoUsuario(Model model, Usuario usuario) {
        model.addAttribute("usuarioId", usuario.getIdUsuario());
        model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
        model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
        model.addAttribute("usuarioRolNormalizado", normalizarRol(usuario));
    }

    private String normalizarRol(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            return "";
        }

        String rol = usuario.getRol().getNombre().trim().toUpperCase(Locale.ROOT);
        return rol.startsWith("ROLE_") ? rol.substring(5) : rol;
    }

    private Optional<Usuario> buscarUsuarioPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByCorreoIgnoreCase(correo.trim());
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private String redirigirABandeja(String origen, Long usuarioId) {
        String destino = origen == null ? "inbox" : origen.trim().toLowerCase(Locale.ROOT);

        return switch (destino) {
            case "enviados" -> "redirect:/correo/enviados/" + usuarioId;
            case "papelera" -> "redirect:/correo/papelera/" + usuarioId;
            case "masivos" -> "redirect:/correo/masivos/" + usuarioId;
            default -> "redirect:/correo/inbox/" + usuarioId;
        };
    }

    // Endpoint para verificar estado del correo
    @GetMapping("/api/status")
    @ResponseBody
    public java.util.Map<String, Object> verificarEstadoCorreo() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        
        try {
            // Verificar configuración SMTP
            boolean smtpOk = emailService.validarConfiguracion();
            status.put("smtp", smtpOk ? "OK" : "ERROR");
            
            // Información adicional
            status.put("timestamp", java.time.LocalDateTime.now().toString());
            status.put("status", "active");
            
        } catch (Exception e) {
            status.put("smtp", "ERROR");
            status.put("error", e.getMessage());
            status.put("status", "error");
        }
        
        return status;
    }

    // Endpoint para forzar sincronización de correos entrantes
    @PostMapping("/api/sync-inbox")
    @ResponseBody
    public java.util.Map<String, Object> sincronizarCorreos() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            System.out.println("[API] Forzando sincronización manual de correos IMAP...");
            inboundMailService.pollInbox();
            result.put("status", "success");
            result.put("message", "✅ Sincronización completada. Revisa la consola para detalles.");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
        } catch (Exception e) {
            System.err.println("[API] Error en sincronización: " + e.getMessage());
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "❌ Error en sincronización: " + e.getMessage());
        }
        
        return result;
    }

    // DEBUG: listar mensajes relacionados a una dirección externa (entrantes y salientes)
    @GetMapping("/debug/external")
    @ResponseBody
    public java.util.Map<String, Object> debugExternal(@RequestParam String email) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        try {
            var salientes = mensajeRepository.findByDestinatarioExternoOrderByFechaEnvioDesc(email);
            var entrantes = mensajeRepository.findByRemitente_CorreoOrderByFechaEnvioDesc(email);
            resp.put("email", email);
            resp.put("salientes_count", salientes.size());
            resp.put("entrantes_count", entrantes.size());
            resp.put("salientes", salientes.stream().map(m -> java.util.Map.of("id", m.getId(), "asunto", m.getAsunto(), "fecha", m.getFechaEnvio().toString(), "remitente", m.getRemitente() != null ? m.getRemitente().getCorreo() : null)).collect(Collectors.toList()));
            resp.put("entrantes", entrantes.stream().map(m -> java.util.Map.of("id", m.getId(), "asunto", m.getAsunto(), "fecha", m.getFechaEnvio().toString(), "destinatario", m.getDestinatario() != null ? m.getDestinatario().getCorreo() : m.getDestinatarioExterno())).collect(Collectors.toList()));
        } catch (Exception e) {
            resp.put("error", e.getMessage());
        }
        return resp;
    }

    // ENDPOINT DE DEBUG COMPLETO - Verificar estado de envíos
    @GetMapping("/debug-status/{usuarioId}")
    @ResponseBody
    public java.util.Map<String, Object> debugStatus(@PathVariable Long usuarioId) {
        java.util.Map<String, Object> debug = new java.util.HashMap<>();
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            
            if (usuario == null) {
                debug.put("error", "Usuario no encontrado con ID: " + usuarioId);
                debug.put("timestamp", java.time.LocalDateTime.now().toString());
                return debug;
            }

            // Información del usuario
            debug.put("usuario_id", usuario.getIdUsuario());
            debug.put("usuario_nombre", usuario.getNombre());
            debug.put("usuario_correo", usuario.getCorreo());

            // Contar todos los mensajes del usuario
            List<Mensaje> todosMensajes = mensajeRepository.findByRemitenteOrderByFechaEnvioDesc(usuario);

            debug.put("total_mensajes_enviados", todosMensajes.size());

            // Últimos 5 men    sajes enviados
            List<java.util.Map<String, Object>> ultimos = todosMensajes.stream()
                    .limit(5)
                    .map(m -> java.util.Map.<String, Object>of(
                        "id", m.getId(),
                        "asunto", m.getAsunto(),
                        "contenido_preview", (m.getContenido() != null ? m.getContenido().substring(0, Math.min(50, m.getContenido().length())) : "VACIO") + "...",
                        "destinatario_interno", m.getDestinatario() != null ? m.getDestinatario().getCorreo() : "N/A",
                        "destinatario_externo", m.getDestinatarioExterno() != null ? m.getDestinatarioExterno() : "N/A",
                        "carpeta", m.getCarpeta(),
                        "fechaEnvio", m.getFechaEnvio().toString(),
                        "leido", m.isLeido(),
                        "eliminado", m.isEliminado()
                    ))
                    .collect(Collectors.toList());

            debug.put("ultimos_5_mensajes", ultimos);

            // Mensajes en carpeta 'sent'
            List<Mensaje> mensajesSent = mensajeRepository.findByCarpetaOrderByFechaEnvioDesc("sent").stream()
                    .filter(m -> m.getRemitente().getIdUsuario().equals(usuarioId))
                    .collect(Collectors.toList());

            debug.put("mensajes_en_carpeta_sent", mensajesSent.size());

            // Información de SMTP
            debug.put("smtp_configurado", true);
            debug.put("smtp_host", "smtp.gmail.com");
            debug.put("smtp_puerto", 587);
            debug.put("smtp_usuario", "autochecklistoficial@gmail.com");

            debug.put("timestamp", java.time.LocalDateTime.now().toString());
            debug.put("success", true);

        } catch (Exception e) {
            debug.put("error", "Error en debug: " + e.getMessage());
            debug.put("exception", e.getClass().getSimpleName());
            e.printStackTrace();
            debug.put("success", false);
        }
        return debug;
    }

    // TEST DIRECTO EN BD - Sin pasar por servicio
    @GetMapping("/test-send-direct/{remitenteId}/{destinatarioId}")
    @ResponseBody
    public java.util.Map<String, Object> testSendDirect(@PathVariable Long remitenteId, @PathVariable Long destinatarioId) {
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        
        try {
            System.out.println("\n\n========== TEST DIRECTO EN BD ==========");
            System.out.println("[TEST] Insertando mensaje: remitente=" + remitenteId + ", destinatario=" + destinatarioId);
            
            Usuario remitente = usuarioRepository.findById(remitenteId).orElse(null);
            Usuario destinatario = usuarioRepository.findById(destinatarioId).orElse(null);
            
            if (remitente == null || destinatario == null) {
                resultado.put("success", false);
                resultado.put("error", "Usuario no encontrado");
                return resultado;
            }
            
            // Crear mensaje 1 para remitente (sent)
            Mensaje msg1 = new Mensaje();
            msg1.setRemitente(remitente);
            msg1.setDestinatario(destinatario);
            msg1.setAsunto("[TEST] Mensaje de prueba");
            msg1.setContenido("Este es un mensaje de prueba enviado directamente a la BD");
            msg1.setFechaEnvio(java.time.LocalDateTime.now());
            msg1.setCarpeta("sent");
            msg1.setLeido(true);
            msg1.setEliminado(false);
            
            // Crear mensaje 2 para destinatario (inbox)
            Mensaje msg2 = new Mensaje();
            msg2.setRemitente(remitente);
            msg2.setDestinatario(destinatario);
            msg2.setAsunto("[TEST] Mensaje de prueba");
            msg2.setContenido("Este es un mensaje de prueba enviado directamente a la BD");
            msg2.setFechaEnvio(java.time.LocalDateTime.now());
            msg2.setCarpeta("inbox");
            msg2.setLeido(false);
            msg2.setEliminado(false);
            
            // Insertar
            msg1 = mensajeRepository.save(msg1);
            System.out.println("[TEST] ✓ Mensaje para remitente guardado con ID: " + msg1.getId());
            
            msg2 = mensajeRepository.save(msg2);
            System.out.println("[TEST] ✓ Mensaje para destinatario guardado con ID: " + msg2.getId());
            
            // Verificar que están en BD
            List<Mensaje> checkRemitente = mensajeRepository.findByRemitenteOrderByFechaEnvioDesc(remitente);
            List<Mensaje> checkDestinatario = mensajeRepository.findByDestinatarioOrderByFechaEnvioDesc(destinatario);
            
            System.out.println("[TEST] Total mensajes de remitente en BD: " + checkRemitente.size());
            System.out.println("[TEST] Total mensajes de destinatario en BD: " + checkDestinatario.size());
            System.out.println("========== FIN TEST ==========\n\n");
            
            resultado.put("success", true);
            resultado.put("msg1_id", msg1.getId());
            resultado.put("msg2_id", msg2.getId());
            resultado.put("remitente_total_mensajes", checkRemitente.size());
            resultado.put("destinatario_total_mensajes", checkDestinatario.size());
            
        } catch (Exception e) {
            System.err.println("[TEST] ✗ ERROR: " + e.getMessage());
            e.printStackTrace();
            resultado.put("success", false);
            resultado.put("error", e.getMessage());
            resultado.put("exception", e.getClass().getSimpleName());
        }
        
        return resultado;
    }
}
