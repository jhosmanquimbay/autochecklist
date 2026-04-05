package com.example.demostracion.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demostracion.exception.EmailException;
import com.example.demostracion.model.Adjunto;
import com.example.demostracion.model.Mensaje;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.AdjuntoRepository;
import com.example.demostracion.repository.MensajeRepository;
import com.example.demostracion.repository.UsuarioRepository;

@Service
@Transactional
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final EmailService emailService;

    @Value("${mail.inbound.attachments-base:uploads/correos}")
    private String attachmentsBase;

    // ✅ CONSTANTES DE CARPETAS
    private static final String INBOX = "inbox";
    private static final String SENT = "sent";
    private static final String TRASH = "trash";

    public MensajeService(MensajeRepository mensajeRepository,
                          UsuarioRepository usuarioRepository,
                          AdjuntoRepository adjuntoRepository,
                          EmailService emailService) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.adjuntoRepository = adjuntoRepository;
        this.emailService = emailService;
    }

    // =====================================================
    // 📩 ENVIAR MENSAJE
    // =====================================================
    public void enviarMensaje(Long remitenteId,
                              Long destinatarioId,
                              String asunto,
                              String contenido,
                              MultipartFile[] archivos) {

        Usuario remitente = usuarioRepository.findById(remitenteId).orElseThrow();
        Usuario destinatario = usuarioRepository.findById(destinatarioId).orElseThrow();

        LocalDateTime ahora = LocalDateTime.now();

        // copia remitente (ENVIADOS)
        Mensaje mensajeRemitente = new Mensaje();
        mensajeRemitente.setRemitente(remitente);
        mensajeRemitente.setDestinatario(destinatario);
        mensajeRemitente.setAsunto(asunto);
        mensajeRemitente.setContenido(contenido);
        mensajeRemitente.setFechaEnvio(ahora);
        mensajeRemitente.setLeido(true);
        mensajeRemitente.setCarpeta(SENT);
        mensajeRemitente.setEliminado(false);

        mensajeRemitente = mensajeRepository.save(mensajeRemitente);
        guardarAdjuntos(mensajeRemitente, archivos);

        // copia destinatario (INBOX)
        Mensaje mensajeDestinatario = new Mensaje();
        mensajeDestinatario.setRemitente(remitente);
        mensajeDestinatario.setDestinatario(destinatario);
        mensajeDestinatario.setAsunto(asunto);
        mensajeDestinatario.setContenido(contenido);
        mensajeDestinatario.setFechaEnvio(ahora);
        mensajeDestinatario.setLeido(false);
        mensajeDestinatario.setCarpeta(INBOX);
        mensajeDestinatario.setEliminado(false);

        mensajeDestinatario = mensajeRepository.save(mensajeDestinatario);
        guardarAdjuntos(mensajeDestinatario, archivos);
    }

    @Transactional(noRollbackFor = EmailException.class)
    public void enviarAExterna(Long remitenteId,
                               String destinatarioExterno,
                               String asunto,
                               String contenido,
                               MultipartFile[] archivos) {

        Usuario remitente = usuarioRepository.findById(remitenteId).orElseThrow();

        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(null);
        mensaje.setDestinatarioExterno(destinatarioExterno);
        mensaje.setAsunto(asunto);
        mensaje.setContenido(contenido);
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(true);
        mensaje.setCarpeta(SENT);
        mensaje.setEliminado(false);

        mensaje = mensajeRepository.save(mensaje);
        guardarAdjuntos(mensaje, archivos);

        // Enviar correo externo usando el servicio de correo
        emailService.sendEmail(destinatarioExterno, asunto, contenido, archivos);
    }

    // =====================================================
    // 📥 RECIBIDOS
    // =====================================================
    public List<Mensaje> recibir(Long usuarioId) {

        Usuario u = usuarioRepository.findById(usuarioId).orElseThrow();

        return mensajeRepository
                .findByDestinatarioAndCarpetaAndEliminadoFalseOrderByFechaEnvioDesc(u, INBOX);
    }

    // =====================================================
    // 📤 ENVIADOS
    // =====================================================
    public List<Mensaje> enviados(Long usuarioId) {

        Usuario u = usuarioRepository.findById(usuarioId).orElseThrow();

        return mensajeRepository
                .findByRemitenteAndCarpetaAndEliminadoFalseOrderByFechaEnvioDesc(u, SENT);
    }

    // =====================================================
    // 🔔 CONTAR NO LEÍDOS
    // =====================================================
    public long contarNoLeidos(Long usuarioId) {

        Usuario u = usuarioRepository.findById(usuarioId).orElseThrow();

        return mensajeRepository.countByDestinatarioAndLeidoFalseAndEliminadoFalse(u);
    }

    // =====================================================
    // 🗑️ MOVER CARPETA
    // =====================================================
    public void moverCarpeta(Long id, String carpeta) {

        Optional<Mensaje> optional = mensajeRepository.findById(id);

        if (optional.isPresent()) {

            Mensaje msg = optional.get();

            if (TRASH.equalsIgnoreCase(carpeta)) {
                msg.setEliminado(true);
                msg.setEliminadoPermanente(false);
            } else {
                msg.setCarpeta(carpeta);
                msg.setEliminado(false);
                msg.setEliminadoPermanente(false);
            }

            mensajeRepository.save(msg);
        }
    }

    // =====================================================
    // 🗑️ PAPELERA
    // =====================================================
    public List<Mensaje> papelera(Long usuarioId) {

        usuarioRepository.findById(usuarioId).orElseThrow();

        return mensajeRepository.findTrashByUsuarioId(usuarioId);
    }

    // =====================================================
    // 👁️ MARCAR COMO LEÍDO
    // =====================================================
    public void marcarLeido(Long mensajeId) {

        mensajeRepository.findById(mensajeId).ifPresent(m -> {
            m.setLeido(true);
            mensajeRepository.save(m);
        });
    }

    // =====================================================
    // 💬 RESPONDER MENSAJE
    // =====================================================
    @Transactional(noRollbackFor = EmailException.class)
    public void responder(Long mensajeOriginalId,
                          Long remitenteId,
                          String contenido,
                          MultipartFile[] archivos) {

        Mensaje original = mensajeRepository.findById(mensajeOriginalId).orElseThrow();
        Usuario remitente = usuarioRepository.findById(remitenteId).orElseThrow();
        Usuario destinatarioRespuesta = resolverUsuarioDestinoRespuesta(original, remitente);
        String correoExternoRespuesta = resolverCorreoExternoRespuesta(original, remitente, destinatarioRespuesta);

        if (destinatarioRespuesta == null && (correoExternoRespuesta == null || correoExternoRespuesta.isBlank())) {
            throw new IllegalStateException("No se pudo determinar el destinatario de la respuesta");
        }

        boolean destinoInterno = esUsuarioInterno(destinatarioRespuesta);
        LocalDateTime fechaRespuesta = LocalDateTime.now();
        String asuntoRespuesta = "Re: " + original.getAsunto();

        Mensaje respuesta = new Mensaje();

        respuesta.setRemitente(remitente);
        respuesta.setDestinatario(destinoInterno ? destinatarioRespuesta : null);
        respuesta.setDestinatarioExterno(destinoInterno ? null : correoExternoRespuesta);
        respuesta.setAsunto(asuntoRespuesta);
        respuesta.setContenido(contenido);
        respuesta.setFechaEnvio(fechaRespuesta);
        respuesta.setCarpeta(SENT);
        respuesta.setLeido(true);
        respuesta.setEliminado(false);
        respuesta.setIdPadre(original.getId());

        respuesta = mensajeRepository.save(respuesta);
        guardarAdjuntos(respuesta, archivos);

        if (destinoInterno && destinatarioRespuesta != null) {
            Mensaje copiaDestinatario = new Mensaje();
            copiaDestinatario.setRemitente(remitente);
            copiaDestinatario.setDestinatario(destinatarioRespuesta);
            copiaDestinatario.setAsunto(asuntoRespuesta);
            copiaDestinatario.setContenido(contenido);
            copiaDestinatario.setFechaEnvio(fechaRespuesta);
            copiaDestinatario.setCarpeta(INBOX);
            copiaDestinatario.setLeido(false);
            copiaDestinatario.setEliminado(false);
            copiaDestinatario.setIdPadre(original.getId());

            copiaDestinatario = mensajeRepository.save(copiaDestinatario);
            guardarAdjuntos(copiaDestinatario, archivos);
            return;
        }

        if (correoExternoRespuesta != null && !correoExternoRespuesta.isBlank()) {
            emailService.sendEmail(correoExternoRespuesta, asuntoRespuesta, contenido, archivos);
        }
    }

    public void moverAPapelera(Long id) {
        moverCarpeta(id, TRASH);
    }

    public void restaurarDePapelera(Long id) {
        restaurarDePapelera(id, null);
    }

    public void restaurarDePapelera(Long id, Long usuarioId) {
        mensajeRepository.findById(id).ifPresent(m -> {
            m.setEliminado(false);
            m.setEliminadoPermanente(false);
            m.setCarpeta(determinarCarpetaRestauracion(m, usuarioId));
            mensajeRepository.save(m);
        });
    }

    public int vaciarPapelera(Long usuarioId) {
        usuarioRepository.findById(usuarioId).orElseThrow();

        List<Mensaje> papelera = mensajeRepository.findDeletedByUsuarioId(usuarioId);

        int eliminados = 0;
        for (Mensaje m : papelera) {
            if (!m.isEliminadoPermanente()) {
                m.setEliminadoPermanente(true);
                mensajeRepository.save(m);
                eliminados++;
            }
        }
        return eliminados;
    }

    public void eliminarDefinitivo(Long id) {
        mensajeRepository.findById(id).ifPresent(m -> {
            m.setEliminadoPermanente(true);
            mensajeRepository.save(m);
        });
    }

    public void marcarEliminadoPermanente(Long id) {
        eliminarDefinitivo(id);
    }

    // =====================================================
    // 📌 CONSULTAS DE MENSAJES
    // =====================================================
    public List<Mensaje> listarPorCarpeta(String carpeta) {
        if (TRASH.equalsIgnoreCase(carpeta)) {
            return mensajeRepository.findByEliminadoTrueAndEliminadoPermanenteFalseOrderByFechaEnvioDesc();
        }
        return mensajeRepository.findByCarpetaOrderByFechaEnvioDesc(carpeta);
    }

    public Mensaje verMensaje(Long id) {
        return mensajeRepository.findById(id).orElseThrow();
    }

    public void marcarComoLeido(Long mensajeId) {
        marcarLeido(mensajeId);
    }

    public List<Mensaje> getRespuestas(Long mensajeId) {
        return mensajeRepository.findByIdPadreOrderByFechaEnvioAsc(mensajeId);
    }

    private Usuario resolverUsuarioDestinoRespuesta(Mensaje original, Usuario remitenteActual) {
        if (original.getRemitente() != null && !esMismoUsuario(original.getRemitente(), remitenteActual)) {
            return original.getRemitente();
        }

        if (original.getDestinatario() != null && !esMismoUsuario(original.getDestinatario(), remitenteActual)) {
            return original.getDestinatario();
        }

        return null;
    }

    private String resolverCorreoExternoRespuesta(Mensaje original,
                                                  Usuario remitenteActual,
                                                  Usuario destinatarioRespuesta) {
        if (original.getDestinatarioExterno() != null
                && !original.getDestinatarioExterno().isBlank()
                && esMismoUsuario(original.getRemitente(), remitenteActual)) {
            return original.getDestinatarioExterno().trim();
        }

        if (destinatarioRespuesta != null && !esUsuarioInterno(destinatarioRespuesta)) {
            return destinatarioRespuesta.getCorreo();
        }

        return null;
    }

    private boolean esUsuarioInterno(Usuario usuario) {
        return usuario != null
                && usuario.getRol() != null
                && usuario.getRol().getNombre() != null
                && !usuario.getRol().getNombre().isBlank();
    }

    private boolean esMismoUsuario(Usuario usuario1, Usuario usuario2) {
        if (usuario1 == null || usuario2 == null) {
            return false;
        }

        return usuario1.getIdUsuario() != null
                && usuario1.getIdUsuario().equals(usuario2.getIdUsuario());
    }

    private String determinarCarpetaRestauracion(Mensaje mensaje, Long usuarioId) {
        if (mensaje.getCarpeta() != null && !TRASH.equalsIgnoreCase(mensaje.getCarpeta())) {
            return mensaje.getCarpeta();
        }

        if (usuarioId != null
                && mensaje.getRemitente() != null
                && mensaje.getRemitente().getIdUsuario() != null
                && mensaje.getRemitente().getIdUsuario().equals(usuarioId)) {
            return SENT;
        }

        if (mensaje.getDestinatarioExterno() != null && !mensaje.getDestinatarioExterno().isBlank()) {
            return SENT;
        }

        return INBOX;
    }

    // =====================================================
    // 📎 ADJUNTOS
    // =====================================================
    private void guardarAdjuntos(Mensaje mensaje, MultipartFile[] archivos) {
        if (archivos == null || archivos.length == 0) {
            return;
        }

        String basePath = attachmentsBase;
        if (basePath == null || basePath.isBlank()) {
            basePath = "uploads/correos";
        }

        File baseDir = new File(basePath, String.valueOf(mensaje.getId()));
        baseDir.mkdirs();

        for (MultipartFile archivo : archivos) {
            if (archivo == null || archivo.isEmpty()) {
                continue;
            }

            String nombre = archivo.getOriginalFilename();
            if (nombre == null) {
                continue;
            }

            File destino = new File(baseDir, nombre);
            try (FileOutputStream fos = new FileOutputStream(destino)) {
                fos.write(archivo.getBytes());
            } catch (IOException e) {
                // Ignorar errores de adjuntos para no bloquear el envío
                System.err.println("Error guardando adjunto: " + e.getMessage());
                continue;
            }

            Adjunto adjunto = new Adjunto();
            adjunto.setNombreArchivo(nombre);
            adjunto.setRutaArchivo(destino.getAbsolutePath());
            adjunto.setMensaje(mensaje);
            adjuntoRepository.save(adjunto);
        }
    }
}
