package com.example.demostracion.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demostracion.dto.EmailRequest;
import com.example.demostracion.dto.EmailResponse;
import com.example.demostracion.exception.EmailException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final String TEMP_DIR =
            System.getProperty("java.io.tmpdir") + "/email-attachments/";

    public EmailResponse enviarCorreosMasivos(EmailRequest request) {
        return procesarEnvioCorreos(request);
    }

    @Async
    public CompletableFuture<EmailResponse> enviarCorreosMasivosAsync(EmailRequest request) {
        EmailResponse response = procesarEnvioCorreos(request);
        return CompletableFuture.completedFuture(response);
    }

    public EmailResponse enviarCorreosConAdjuntos(EmailRequest request, List<MultipartFile> archivos) {

        List<String> rutasArchivos = new ArrayList<>();

        try {

            Files.createDirectories(Paths.get(TEMP_DIR));

            if (archivos != null) {
                for (MultipartFile archivo : archivos) {

                    if (!archivo.isEmpty()) {

                        String nombre = StringUtils.cleanPath(
                                Objects.requireNonNull(archivo.getOriginalFilename()));

                        Path ruta = Paths.get(TEMP_DIR + nombre);

                        Files.copy(archivo.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

                        rutasArchivos.add(ruta.toString());
                    }
                }
            }

            request.setArchivosAdjuntos(rutasArchivos);

            return procesarEnvioCorreos(request);

        } catch (IOException e) {

            throw new EmailException("Error procesando adjuntos: " + e.getMessage());

        } finally {

            limpiarArchivosTemporales(rutasArchivos);
        }
    }

    private EmailResponse procesarEnvioCorreos(EmailRequest request) {

        if (request.getDestinatarios() == null || request.getDestinatarios().isEmpty()) {
            return EmailResponse.error("No hay destinatarios");
        }

        List<String> emailsValidos = validarEmails(request.getDestinatarios());

        int enviados = 0;
        List<String> errores = new ArrayList<>();

        for (String email : emailsValidos) {

            try {

                enviarCorreoIndividual(email, request);

                enviados++;

            } catch (MessagingException | MailException e) {

                log.error("Error enviando a {}", email, e);

                errores.add(email + " -> " + e.getMessage());
            }
        }

        return errores.isEmpty()
                ? EmailResponse.exitoso(enviados, LocalDateTime.now())
                : EmailResponse.conErrores(enviados, emailsValidos.size() - enviados, errores, LocalDateTime.now());
    }

    private void enviarCorreoIndividual(String destinatario, EmailRequest request) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        String remitenteConfigurado = normalizarTexto(mailFrom);
        String destinatarioSeguro = normalizarTexto(destinatario);
        String asuntoSeguro = normalizarTexto(request.getAsunto());
        String mensajeSeguro = normalizarTexto(request.getMensaje());

        if (remitenteConfigurado.isBlank()) {
            throw new EmailException("No hay remitente SMTP configurado.");
        }
        if (destinatarioSeguro.isBlank()) {
            throw new EmailException("No hay destinatario de correo.");
        }

        helper.setFrom(remitenteConfigurado);
        helper.setReplyTo(remitenteConfigurado);
        helper.setTo(destinatarioSeguro);
        helper.setSentDate(new java.util.Date());

        helper.setSubject(asuntoSeguro);

        if (request.isEsHtml()) {
            helper.setText(generarTextoPlano(mensajeSeguro), mensajeSeguro);
        } else {
            helper.setText(mensajeSeguro, false);
        }

        if (request.getArchivosAdjuntos() != null) {

            for (String ruta : request.getArchivosAdjuntos()) {

                File archivo = new File(ruta);

                if (archivo.exists()) {

                    String nombreAdjunto = normalizarTexto(archivo.getName());
                    helper.addAttachment(nombreAdjunto.isBlank() ? "adjunto" : nombreAdjunto, new FileSystemResource(archivo));
                }
            }
        }

        mailSender.send(message);

        log.info("Correo enviado a {}", destinatarioSeguro);
    }

    private List<String> validarEmails(List<String> emails) {

        return emails.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(e -> EMAIL_PATTERN.matcher(e).matches())
                .distinct()
                .toList();
    }

    private void limpiarArchivosTemporales(List<String> rutas) {

        for (String ruta : rutas) {

            try {

                Files.deleteIfExists(Paths.get(ruta));

            } catch (IOException ignored) {
            }
        }
    }

    public boolean validarConfiguracion() {
        return mailSender != null && !normalizarTexto(mailFrom).isBlank();
    }

    public void sendEmail(
            String destino,
            String asunto,
            String mensaje,
            MultipartFile[] archivos) {

        EmailRequest request = new EmailRequest();
        request.setDestinatarios(List.of(destino));
        request.setAsunto(asunto);
        request.setMensaje(mensaje);
        request.setEsHtml(true);

        List<MultipartFile> listaArchivos =
                archivos != null ? Arrays.asList(archivos) : Collections.emptyList();

        EmailResponse response = enviarCorreosConAdjuntos(request, listaArchivos);
        if (!response.isExitoso() || response.getTotalEnviados() == 0) {
            String detalle = response.getErrores() == null || response.getErrores().isEmpty()
                    ? response.getMensaje()
                    : String.join(" | ", response.getErrores());
            throw new EmailException("No fue posible enviar el correo a " + destino + ": " + detalle);
        }
    }


    private String generarTextoPlano(String mensajeHtml) {
        String contenido = normalizarTexto(mensajeHtml);
        return Jsoup.parse(contenido).text();
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}