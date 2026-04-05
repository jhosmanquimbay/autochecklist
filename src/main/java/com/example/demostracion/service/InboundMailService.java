package com.example.demostracion.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demostracion.model.Adjunto;
import com.example.demostracion.model.Mensaje;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.AdjuntoRepository;
import com.example.demostracion.repository.MensajeRepository;
import com.example.demostracion.repository.UsuarioRepository;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;

@Service
public class InboundMailService {

    @Value("${mail.inbound.protocol}")
    private String protocol;

    @Value("${mail.inbound.host}")
    private String host;

    @Value("${mail.inbound.port}")
    private int port;

    @Value("${mail.inbound.user}")
    private String user;

    @Value("${mail.inbound.password}")
    private String password;

    @Value("${mail.inbound.folder:INBOX}")
    private String folderName;

    @Value("${mail.inbound.mark-seen:true}")
    private boolean markSeen;

    @Value("${mail.inbound.attachments-base:uploads/correos}")
    private String attachmentsBase;

    @Value("${mail.inbound.fallback-user-id:}")
    private String fallbackUserId;

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdjuntoRepository adjuntoRepository;

    public InboundMailService(
            MensajeRepository mensajeRepository,
            UsuarioRepository usuarioRepository,
            AdjuntoRepository adjuntoRepository) {

        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.adjuntoRepository = adjuntoRepository;
    }

    @Scheduled(fixedDelayString = "${mail.inbound.poll-interval-ms:60000}")
    public void pollInbox() {

        try {

            processInbox();

        } catch (Exception e) {

            System.err.println("Error leyendo correos IMAP");
            e.printStackTrace();
        }
    }

    @Transactional
    public void processInbox() throws Exception {

        Properties props = new Properties();

        props.put("mail.store.protocol", protocol);
        props.put("mail." + protocol + ".host", host);
        props.put("mail." + protocol + ".port", port);

        Session session = Session.getInstance(props);

        Store store = session.getStore(protocol);

        store.connect(host, port, user, password);

        Folder folder = store.getFolder(folderName);

        folder.open(Folder.READ_WRITE);

        Message[] messages = folder.search(
                new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        for (Message msg : messages) {

            procesarMensaje(msg);
        }

        folder.close(true);
        store.close();
    }

    private Usuario findInboundRecipient() {
        // Prefer user configured explicitly via mail.inbound.fallback-user-id
        if (fallbackUserId != null && !fallbackUserId.isBlank()) {
            try {
                Long id = Long.parseLong(fallbackUserId.trim());
                return usuarioRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }

        // Otherwise, use the inbound mailbox user as recipient
        if (user != null && !user.isBlank()) {
            return usuarioRepository.findByCorreo(user).orElse(null);
        }

        return null;
    }

    private void procesarMensaje(Message msg) {

        try {

            InternetAddress from = (InternetAddress) msg.getFrom()[0];

            String correo = from.getAddress();

            Usuario remitente = usuarioRepository
                    .findByCorreo(correo)
                    .orElseGet(() -> {

                        Usuario u = new Usuario();

                        u.setCorreo(correo);
                        u.setNombre(from.getPersonal());

                        return usuarioRepository.save(u);
                    });

            String asunto = msg.getSubject();

            String contenido = extraerTexto(msg);

            Date sentDate = msg.getSentDate();

            LocalDateTime fecha = LocalDateTime.ofInstant(
                    sentDate.toInstant(),
                    ZoneId.systemDefault());

            Usuario destinatario = findInboundRecipient();
            if (destinatario == null) {
                // If we cannot resolve a recipient from config, create a fallback user for the mailbox.
                destinatario = usuarioRepository.findByCorreo(user).orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setCorreo(user);
                    u.setNombre("Buzón de correo");
                    u.setContrasena(UUID.randomUUID().toString());
                    u.setActivo(true);
                    return usuarioRepository.save(u);
                });
            }

            Mensaje m = new Mensaje();

            m.setRemitente(remitente);
            m.setDestinatario(destinatario);
            m.setAsunto(asunto);
            m.setContenido(contenido);
            m.setFechaEnvio(fecha);
            m.setCarpeta("inbox");

            m = mensajeRepository.save(m);

            guardarAdjuntos(msg, m);

            if (markSeen) {

                msg.setFlag(Flags.Flag.SEEN, true);
            }

            System.out.println("Correo recibido de " + correo);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void guardarAdjuntos(Message msg, Mensaje mensaje) {

        try {

            Object content = msg.getContent();

            if (content instanceof MimeMultipart multipart) {

                String base = attachmentsBase + "/" + mensaje.getId();

                new File(base).mkdirs();

                for (int i = 0; i < multipart.getCount(); i++) {

                    BodyPart part = multipart.getBodyPart(i);

                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {

                        String filename = part.getFileName();

                        File file = new File(base + "/" + filename);

                        try (InputStream is = part.getInputStream();
                             FileOutputStream fos = new FileOutputStream(file)) {

                            is.transferTo(fos);
                        }

                        Adjunto a = new Adjunto();

                        a.setNombreArchivo(filename);
                        a.setRutaArchivo(file.getPath());
                        a.setMensaje(mensaje);

                        adjuntoRepository.save(a);
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private String extraerTexto(Message message) {

        try {

            Object content = message.getContent();

            if (content instanceof String) {

                return (String) content;
            }

            if (content instanceof MimeMultipart multipart) {

                for (int i = 0; i < multipart.getCount(); i++) {

                    BodyPart bp = multipart.getBodyPart(i);

                    if (bp.isMimeType("text/plain")) {

                        return (String) bp.getContent();
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return "";
    }
}