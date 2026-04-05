package com.example.demostracion.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import com.example.demostracion.model.PasswordResetToken;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.PasswordResetTokenRepository;
import com.example.demostracion.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RecuperacionContrasenaService {

    private static final int CODIGO_DIGITOS = 6;
    private static final int EXPIRACION_MINUTOS = 10;
    private static final Pattern PASSWORD_POLICY = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");
    private static final Pattern CODIGO_PATTERN = Pattern.compile("^\\d{6}$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperacionContrasenaService(UsuarioRepository usuarioRepository,
                                         PasswordResetTokenRepository passwordResetTokenRepository,
                                         PasswordEncoder passwordEncoder,
                                         EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public boolean solicitarRestablecimiento(String correo, String baseUrl) {
        String correoNormalizado = normalizarCorreo(correo);
        if (correoNormalizado.isBlank()) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoIgnoreCase(correoNormalizado);
        if (usuarioOpt.isEmpty()) {
            log.warn("Solicitud de restablecimiento ignorada para correo no registrado: {}", correoNormalizado);
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        passwordResetTokenRepository.deleteByFechaExpiracionBefore(LocalDateTime.now());
        passwordResetTokenRepository.deleteByUsuarioIdUsuario(usuario.getIdUsuario());

        String codigoPlano = generarCodigoSeguro();
        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setTokenHash(hashToken(codigoPlano));
        token.setFechaSolicitud(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS));
        token.setFechaUso(null);

        PasswordResetToken guardado = passwordResetTokenRepository.save(token);

        try {
            emailService.sendEmail(
                    usuario.getCorreo(),
                    "Código para restablecer tu contraseña | AutoCheckList",
                    construirCorreoRestablecimiento(usuario, codigoPlano, construirUrlRestablecimiento(baseUrl)),
                    null);
        } catch (RuntimeException ex) {
            passwordResetTokenRepository.delete(guardado);
            throw new IllegalStateException("No fue posible enviar el correo de restablecimiento. Verifica la configuración de correo e inténtalo nuevamente.", ex);
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean codigoEsValido(String correo, String codigo) {
        return buscarCodigoActivo(correo, codigo).isPresent();
    }

    @Transactional(readOnly = true)
    public Long validarCodigo(String correo, String codigo) {
        validarFormatoCodigo(codigo);
        return buscarCodigoActivo(correo, codigo)
                .map(PasswordResetToken::getIdPasswordResetToken)
                .orElseThrow(() -> new IllegalArgumentException("El código es inválido o expiró. Solicita uno nuevo."));
    }

    @Transactional(readOnly = true)
    public String obtenerCorreoEnmascaradoPorCorreo(String correo) {
        return enmascararCorreo(correo);
    }

    @Transactional(readOnly = true)
    public boolean autorizacionVigente(Long tokenId, String correo) {
        return buscarTokenActivoPorId(tokenId)
                .map(PasswordResetToken::getUsuario)
                .map(Usuario::getCorreo)
                .map(this::normalizarCorreo)
                .filter(correoToken -> correoToken.equals(normalizarCorreo(correo)))
                .isPresent();
    }

    @Transactional(readOnly = true)
    public long obtenerSegundosRestantes(Long fechaExpiracionEpochMillis) {
        if (fechaExpiracionEpochMillis == null) {
            return 0;
        }

        Instant ahora = Instant.now();
        Instant expira = Instant.ofEpochMilli(fechaExpiracionEpochMillis);
        long segundos = Duration.between(ahora, expira).getSeconds();
        return Math.max(segundos, 0);
    }

    public int obtenerExpiracionMinutos() {
        return EXPIRACION_MINUTOS;
    }

    @Transactional
    public void restablecerContrasenaAutorizada(Long tokenId, String nuevaContrasena) {
        validarPoliticaContrasena(nuevaContrasena);

        PasswordResetToken token = buscarTokenActivoPorId(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("La validación del código expiró. Solicita uno nuevo."));

        Usuario usuario = token.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena.trim()));
        usuarioRepository.save(usuario);

        token.setFechaUso(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    public void validarPoliticaContrasena(String nuevaContrasena) {
        String valor = nuevaContrasena == null ? "" : nuevaContrasena.trim();
        if (!PASSWORD_POLICY.matcher(valor).matches()) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 64 caracteres e incluir al menos una letra y un número.");
        }
    }

    private void validarFormatoCodigo(String codigo) {
        String codigoNormalizado = normalizarCodigo(codigo);
        if (!CODIGO_PATTERN.matcher(codigoNormalizado).matches()) {
            throw new IllegalArgumentException("Ingresa el código de 6 dígitos enviado a tu correo.");
        }
    }

    private Optional<PasswordResetToken> buscarCodigoActivo(String correo, String codigo) {
        String correoNormalizado = normalizarCorreo(correo);
        String codigoNormalizado = normalizarCodigo(codigo);
        if (correoNormalizado.isBlank() || codigoNormalizado.isBlank()) {
            return Optional.empty();
        }

        String codigoHash = hashToken(codigoNormalizado);

        return usuarioRepository.findByCorreoIgnoreCase(correoNormalizado)
                .flatMap(usuario -> passwordResetTokenRepository
                        .findTopByUsuarioIdUsuarioAndFechaUsoIsNullOrderByFechaSolicitudDesc(usuario.getIdUsuario()))
                .filter(this::tokenEstaActivo)
                .filter(token -> token.getTokenHash().equals(codigoHash));
    }

    private Optional<PasswordResetToken> buscarTokenActivoPorId(Long tokenId) {
        if (tokenId == null) {
            return Optional.empty();
        }

        return passwordResetTokenRepository.findById(tokenId)
                .filter(this::tokenEstaActivo);
    }

    private boolean tokenEstaActivo(PasswordResetToken token) {
        return token != null
                && token.getFechaUso() == null
                && token.getFechaExpiracion() != null
                && token.getFechaExpiracion().isAfter(LocalDateTime.now())
                && token.getUsuario() != null
                && Boolean.TRUE.equals(token.getUsuario().getActivo());
    }

    private String generarCodigoSeguro() {
        return String.format(Locale.ROOT, "%0" + CODIGO_DIGITOS + "d", secureRandom.nextInt(1_000_000));
    }

    private String hashToken(String tokenPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo generar el hash del token de recuperación.", ex);
        }
    }

    private String construirUrlRestablecimiento(String baseUrl) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        return base + "/restablecer";
    }

    private String construirCorreoRestablecimiento(Usuario usuario, String codigo, String urlRestablecimiento) {
        String nombreBase = Optional.ofNullable(usuario.getNombre()).orElse("Cliente");
        String nombre = escapeHtml(nombreBase);
        String codigoSeguro = escapeHtml(normalizarCodigo(codigo));
        String url = escapeHtml(urlRestablecimiento == null ? "" : urlRestablecimiento);

        return "<div style=\"font-family:Arial,sans-serif;background:#f5f7fb;padding:32px;color:#10243e;\">"
                + "<div style=\"max-width:620px;margin:0 auto;background:#ffffff;border-radius:22px;overflow:hidden;box-shadow:0 18px 50px rgba(16,36,62,0.12);\">"
                + "<div style=\"padding:28px 32px;background:linear-gradient(135deg,#0a1d37,#1c3b5a);color:#ffffff;\">"
                + "<p style=\"margin:0 0 10px;font-size:12px;letter-spacing:1.2px;text-transform:uppercase;opacity:0.82;\">AutoCheckList</p>"
                + "<h1 style=\"margin:0;font-size:28px;line-height:1.15;\">Código de restablecimiento</h1>"
                + "</div>"
                + "<div style=\"padding:30px 32px;\">"
                + "<p style=\"margin:0 0 14px;font-size:16px;\">Hola, <strong>" + nombre + "</strong>.</p>"
                + "<p style=\"margin:0 0 18px;line-height:1.65;color:#43546a;\">Recibimos una solicitud para cambiar la contraseña de tu cuenta. Usa este código de verificación para completar el proceso.</p>"
                + "<div style=\"margin:28px 0;padding:18px 22px;border-radius:18px;background:#f8fbff;border:1px solid #d7e4f2;text-align:center;\">"
                + "<p style=\"margin:0 0 8px;font-size:12px;letter-spacing:1.5px;text-transform:uppercase;color:#5a6a7e;\">Código de verificación</p>"
                + "<div style=\"font-size:34px;font-weight:700;letter-spacing:8px;color:#0a1d37;\">" + codigoSeguro + "</div>"
                + "</div>"
                + "<p style=\"margin:0 0 12px;line-height:1.65;color:#43546a;\">El código estará disponible durante <strong>10 minutos</strong> y solo podrá usarse una vez.</p>"
                + "<p style=\"margin:0 0 16px;line-height:1.65;color:#43546a;\">Luego entra a la pantalla de restablecimiento, escribe tu correo, este código y tu nueva contraseña.</p>"
                + "<div style=\"margin:22px 0;text-align:center;\">"
                + "<a href=\"" + url + "\" style=\"display:inline-block;padding:14px 22px;border-radius:14px;background:#f5a623;color:#10243e;text-decoration:none;font-weight:700;\">Ir a restablecer contraseña</a>"
                + "</div>"
                + "<p style=\"margin:0 0 10px;line-height:1.65;color:#43546a;\">Si no solicitaste este cambio, puedes ignorar este mensaje. Tu contraseña actual seguirá siendo válida.</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String enmascararCorreo(String correo) {
        String valor = normalizarCorreo(correo);
        int arroba = valor.indexOf('@');
        if (arroba <= 1) {
            return valor;
        }

        String local = valor.substring(0, arroba);
        String dominio = valor.substring(arroba + 1);
        String visibleLocal = local.substring(0, Math.min(2, local.length()));
        String visibleDominio = dominio.length() <= 3 ? dominio : dominio.substring(0, 3);
        return visibleLocal + "***@" + visibleDominio + "***";
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim();
    }

    private String escapeHtml(String valor) {
        Object escape = HtmlUtils.htmlEscape(valor == null ? "" : valor);
        return escape == null ? "" : escape.toString();
    }
}