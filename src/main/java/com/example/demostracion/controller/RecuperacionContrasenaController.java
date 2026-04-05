package com.example.demostracion.controller;

import java.time.Instant;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.RecuperarContrasenaForm;
import com.example.demostracion.dto.RestablecerContrasenaForm;
import com.example.demostracion.dto.ValidarCodigoRecuperacionForm;
import com.example.demostracion.service.RecuperacionContrasenaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class RecuperacionContrasenaController {

    private static final String SESSION_CORREO_PENDIENTE = "passwordResetPendingEmail";
    private static final String SESSION_EXPIRACION_PENDIENTE = "passwordResetPendingExpiry";
    private static final String SESSION_TOKEN_AUTORIZADO = "passwordResetAuthorizedTokenId";
    private static final String SESSION_CORREO_AUTORIZADO = "passwordResetAuthorizedEmail";
    private static final String MENSAJE_CORREO_NO_ENCONTRADO = "No encontramos una cuenta activa con ese correo. Verifica el correo registrado del cliente.";

    private final RecuperacionContrasenaService recuperacionContrasenaService;

    public RecuperacionContrasenaController(RecuperacionContrasenaService recuperacionContrasenaService) {
        this.recuperacionContrasenaService = recuperacionContrasenaService;
    }

    @GetMapping("/recuperar")
    public String mostrarFormularioRecuperacion(Model model) {
        if (!model.containsAttribute("recuperarForm")) {
            model.addAttribute("recuperarForm", new RecuperarContrasenaForm());
        }
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitarRestablecimiento(@Valid @ModelAttribute("recuperarForm") RecuperarContrasenaForm recuperarForm,
                                            BindingResult bindingResult,
                                            HttpServletRequest request,
                                            HttpSession session,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "recuperar";
        }

        try {
            boolean codigoEnviado = recuperacionContrasenaService
                    .solicitarRestablecimiento(recuperarForm.getCorreo(), construirBaseUrl(request));
            if (!codigoEnviado) {
                model.addAttribute("errorEnvio", MENSAJE_CORREO_NO_ENCONTRADO);
                return "recuperar";
            }
        } catch (IllegalStateException ex) {
            model.addAttribute("errorEnvio", ex.getMessage());
            return "recuperar";
        }

        String correoNormalizado = normalizarCorreo(recuperarForm.getCorreo());
        limpiarAutorizacion(session);
        session.setAttribute(SESSION_CORREO_PENDIENTE, correoNormalizado);
        session.setAttribute(SESSION_EXPIRACION_PENDIENTE, calcularExpiracionEnSesion());

        ValidarCodigoRecuperacionForm form = new ValidarCodigoRecuperacionForm();
        form.setCorreo(correoNormalizado);
        redirectAttributes.addFlashAttribute("codigoForm", form);
        redirectAttributes.addFlashAttribute("codigoEnviado", Boolean.TRUE);
        redirectAttributes.addFlashAttribute(
                "correoDestino",
                recuperacionContrasenaService.obtenerCorreoEnmascaradoPorCorreo(correoNormalizado));
        return "redirect:/restablecer";
    }

    @GetMapping("/restablecer")
    public String mostrarFormularioRestablecimiento(Model model, HttpSession session) {
        if (tieneAutorizacionVigente(session)) {
            prepararPasoContrasena(model, session);
            return "restablecer";
        }

        String correoPendiente = obtenerCorreoPendiente(session);
        if (correoPendiente.isBlank()) {
            return "redirect:/recuperar";
        }

        prepararPasoCodigo(model, session, correoPendiente);
        return "restablecer";
    }

    @PostMapping("/restablecer/validar-codigo")
    public String validarCodigo(@Valid @ModelAttribute("codigoForm") ValidarCodigoRecuperacionForm codigoForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        String correoNormalizado = normalizarCorreo(codigoForm.getCorreo());
        codigoForm.setCorreo(correoNormalizado);
        session.setAttribute(SESSION_CORREO_PENDIENTE, correoNormalizado);

        if (bindingResult.hasErrors()) {
            prepararPasoCodigo(model, session, correoNormalizado);
            return "restablecer";
        }

        try {
            Long tokenId = recuperacionContrasenaService.validarCodigo(correoNormalizado, codigoForm.getCodigo());
            session.setAttribute(SESSION_TOKEN_AUTORIZADO, tokenId);
            session.setAttribute(SESSION_CORREO_AUTORIZADO, correoNormalizado);
            redirectAttributes.addFlashAttribute("codigoValidado", Boolean.TRUE);
            return "redirect:/restablecer";
        } catch (IllegalArgumentException ex) {
            String mensaje = ex.getMessage();
            if (mensaje == null || mensaje.isBlank()) {
                mensaje = "El código es inválido o expiró. Solicita uno nuevo.";
            }
            bindingResult.rejectValue("codigo", "codigo.invalido", mensaje);
            prepararPasoCodigo(model, session, correoNormalizado);
            return "restablecer";
        }
    }

    @PostMapping("/restablecer/reenviar-codigo")
    public String reenviarCodigo(@RequestParam("correo") String correo,
                                 HttpServletRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String correoNormalizado = normalizarCorreo(correo);
        if (correoNormalizado.isBlank()) {
            redirectAttributes.addFlashAttribute("errorEnvio", "Ingresa el correo para volver a solicitar el código.");
            return "redirect:/recuperar";
        }

        session.setAttribute(SESSION_CORREO_PENDIENTE, correoNormalizado);

        try {
            boolean codigoEnviado = recuperacionContrasenaService
                    .solicitarRestablecimiento(correoNormalizado, construirBaseUrl(request));
            if (!codigoEnviado) {
                limpiarProceso(session);
                redirectAttributes.addFlashAttribute("errorEnvio", MENSAJE_CORREO_NO_ENCONTRADO);
                redirectAttributes.addFlashAttribute("recuperarForm", crearRecuperarForm(correoNormalizado));
                return "redirect:/recuperar";
            }
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorCodigo", ex.getMessage());
            redirectAttributes.addFlashAttribute("codigoForm", crearCodigoForm(correoNormalizado));
            return "redirect:/restablecer";
        }

        limpiarAutorizacion(session);
        session.setAttribute(SESSION_CORREO_PENDIENTE, correoNormalizado);
        session.setAttribute(SESSION_EXPIRACION_PENDIENTE, calcularExpiracionEnSesion());
        redirectAttributes.addFlashAttribute("codigoReenviado", Boolean.TRUE);
        redirectAttributes.addFlashAttribute("codigoForm", crearCodigoForm(correoNormalizado));
        redirectAttributes.addFlashAttribute(
                "correoDestino",
                recuperacionContrasenaService.obtenerCorreoEnmascaradoPorCorreo(correoNormalizado));
        return "redirect:/restablecer";
    }

    @PostMapping("/restablecer")
    public String restablecerContrasena(@Valid @ModelAttribute("restablecerForm") RestablecerContrasenaForm restablecerForm,
                                        BindingResult bindingResult,
                                        Model model,
                                        RedirectAttributes redirectAttributes,
                                        HttpSession session) {
        if (!tieneAutorizacionVigente(session)) {
            limpiarAutorizacion(session);
            return "redirect:/recuperar";
        }

        validarConfirmacion(restablecerForm, bindingResult);

        if (bindingResult.hasErrors()) {
            prepararPasoContrasena(model, session);
            return "restablecer";
        }

        try {
            Long tokenId = (Long) session.getAttribute(SESSION_TOKEN_AUTORIZADO);
            recuperacionContrasenaService.restablecerContrasenaAutorizada(tokenId, restablecerForm.getNuevaContrasena());
        } catch (IllegalArgumentException ex) {
            String mensaje = ex.getMessage();
            if (mensaje == null || mensaje.isBlank()) {
                mensaje = "La validación del código expiró. Solicita uno nuevo.";
            }
            limpiarAutorizacion(session);
            redirectAttributes.addFlashAttribute("errorCodigo", mensaje);
            return "redirect:/restablecer";
        }

        limpiarProceso(session);
        redirectAttributes.addAttribute("passwordReset", "ok");
        return "redirect:/login";
    }

    private void validarConfirmacion(RestablecerContrasenaForm form, BindingResult bindingResult) {
        String nueva = form.getNuevaContrasena() == null ? "" : form.getNuevaContrasena().trim();
        String confirmacion = form.getConfirmacionContrasena() == null ? "" : form.getConfirmacionContrasena().trim();
        if (!nueva.equals(confirmacion)) {
            bindingResult.rejectValue("confirmacionContrasena", "confirmacionContrasena.noCoincide", "La confirmación no coincide con la nueva contraseña.");
        }
    }

    private void prepararPasoCodigo(Model model, HttpSession session, String correo) {
        if (!model.containsAttribute("codigoForm")) {
            model.addAttribute("codigoForm", crearCodigoForm(correo));
        }

        model.addAttribute("pasoActual", "codigo");
        model.addAttribute("correoDestino", recuperacionContrasenaService.obtenerCorreoEnmascaradoPorCorreo(correo));
        model.addAttribute("duracionCodigoMinutos", recuperacionContrasenaService.obtenerExpiracionMinutos());
        long segundosRestantes = recuperacionContrasenaService.obtenerSegundosRestantes((Long) session.getAttribute(SESSION_EXPIRACION_PENDIENTE));
        model.addAttribute("segundosRestantes", segundosRestantes);
        model.addAttribute("codigoExpirado", segundosRestantes <= 0);
        if (!model.containsAttribute("restablecerForm")) {
            model.addAttribute("restablecerForm", new RestablecerContrasenaForm());
        }
    }

    private void prepararPasoContrasena(Model model, HttpSession session) {
        String correo = (String) session.getAttribute(SESSION_CORREO_AUTORIZADO);
        model.addAttribute("pasoActual", "contrasena");
        model.addAttribute("correoDestino", recuperacionContrasenaService.obtenerCorreoEnmascaradoPorCorreo(correo));
        if (!model.containsAttribute("restablecerForm")) {
            model.addAttribute("restablecerForm", new RestablecerContrasenaForm());
        }
    }

    private ValidarCodigoRecuperacionForm crearCodigoForm(String correo) {
        ValidarCodigoRecuperacionForm form = new ValidarCodigoRecuperacionForm();
        form.setCorreo(correo);
        return form;
    }

    private RecuperarContrasenaForm crearRecuperarForm(String correo) {
        RecuperarContrasenaForm form = new RecuperarContrasenaForm();
        form.setCorreo(correo);
        return form;
    }

    private boolean tieneAutorizacionVigente(HttpSession session) {
        Long tokenId = (Long) session.getAttribute(SESSION_TOKEN_AUTORIZADO);
        String correo = (String) session.getAttribute(SESSION_CORREO_AUTORIZADO);
        return tokenId != null
                && correo != null
                && recuperacionContrasenaService.autorizacionVigente(tokenId, correo);
    }

    private String obtenerCorreoPendiente(HttpSession session) {
        Object correo = session.getAttribute(SESSION_CORREO_PENDIENTE);
        return correo == null ? "" : correo.toString();
    }

    private Long calcularExpiracionEnSesion() {
        return Instant.now().plusSeconds(recuperacionContrasenaService.obtenerExpiracionMinutos() * 60L).toEpochMilli();
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private void limpiarAutorizacion(HttpSession session) {
        session.removeAttribute(SESSION_TOKEN_AUTORIZADO);
        session.removeAttribute(SESSION_CORREO_AUTORIZADO);
    }

    private void limpiarProceso(HttpSession session) {
        limpiarAutorizacion(session);
        session.removeAttribute(SESSION_CORREO_PENDIENTE);
        session.removeAttribute(SESSION_EXPIRACION_PENDIENTE);
    }

    private String construirBaseUrl(HttpServletRequest request) {
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(request.getScheme()).append("://").append(request.getServerName());

        int port = request.getServerPort();
        boolean puertoPorDefecto = ("http".equalsIgnoreCase(request.getScheme()) && port == 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && port == 443);

        if (!puertoPorDefecto) {
            baseUrl.append(':').append(port);
        }

        if (request.getContextPath() != null && !request.getContextPath().isBlank()) {
            baseUrl.append(request.getContextPath());
        }

        return baseUrl.toString();
    }
}