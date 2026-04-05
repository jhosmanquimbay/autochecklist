package com.example.demostracion.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.EmailRequest;
import com.example.demostracion.dto.EmailResponse;
import com.example.demostracion.dto.EmailWebRequest;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.service.EmailService;
import com.example.demostracion.service.MensajeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailWebController {
    
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final MensajeService mensajeService;
    
    /**
     * Página principal de correos
     */
    @GetMapping
    public String emailHome(Model model) {
        model.addAttribute("emailRequest", new EmailWebRequest());
        return "email/index";
    }
    
    /**
     * Formulario para envío simple
     */
    @GetMapping("/enviar")
    public String mostrarFormularioEnvio(Model model, Authentication authentication) {
        agregarContextoUsuario(model, authentication);
        
        model.addAttribute("emailRequest", new EmailWebRequest());
        return "email/enviar";
    }
    
    /**
     * Procesar envío simple
     */
    @PostMapping("/enviar")
    public String procesarEnvio(@ModelAttribute EmailWebRequest emailWebRequest,
                               BindingResult bindingResult,
                               @RequestParam(required = false) String destinatariosTexto,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               Authentication authentication) {
        
        try {
            // Procesar lista de destinatarios desde textarea
            if (destinatariosTexto != null && !destinatariosTexto.trim().isEmpty()) {
                List<String> destinatarios = Arrays.stream(destinatariosTexto.split("[,\\n\\r]+"))
                        .map(String::trim)
                        .filter(email -> !email.isEmpty())
                        .toList();
                emailWebRequest.setDestinatarios(destinatarios);
            }
            
            // Validaciones manuales
            if (emailWebRequest.getDestinatarios() == null || emailWebRequest.getDestinatarios().isEmpty()) {
                agregarContextoUsuario(model, authentication);
                model.addAttribute("error", "Por favor ingresa al menos un destinatario");
                model.addAttribute("emailRequest", emailWebRequest);
                return "email/enviar";
            }
            
            if (emailWebRequest.getAsunto() == null || emailWebRequest.getAsunto().trim().isEmpty()) {
                agregarContextoUsuario(model, authentication);
                model.addAttribute("error", "Por favor ingresa un asunto");
                model.addAttribute("emailRequest", emailWebRequest);
                return "email/enviar";
            }
            
            if (emailWebRequest.getMensaje() == null || emailWebRequest.getMensaje().trim().isEmpty()) {
                agregarContextoUsuario(model, authentication);
                model.addAttribute("error", "Por favor ingresa un mensaje");
                model.addAttribute("emailRequest", emailWebRequest);
                return "email/enviar";
            }
            
            // Convertir a EmailRequest para el servicio
            EmailRequest emailRequest = emailWebRequest.toEmailRequest();
            EmailResponse response = emailService.enviarCorreosMasivos(emailRequest);
            
            if (response.isExitoso()) {
                redirectAttributes.addFlashAttribute("success", 
                    "✅ Correos enviados exitosamente: " + response.getTotalEnviados());
            } else {
                redirectAttributes.addFlashAttribute("warning", 
                    "⚠️ Envío parcial: " + response.getTotalEnviados() + " enviados, " + 
                    response.getTotalFallidos() + " fallidos");
                redirectAttributes.addFlashAttribute("errores", response.getErrores());
            }
            
        } catch (Exception e) {
            log.error("Error en envío web", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/email/enviar";
    }

    private void agregarContextoUsuario(Model model, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return;
        }

        usuarioRepository.findByCorreo(authentication.getName()).ifPresent(usuario -> {
            model.addAttribute("usuarioId", usuario.getIdUsuario());
            model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "");
            model.addAttribute("usuarioRolNormalizado", normalizarRol(usuario));
            model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
        });
    }

    private String normalizarRol(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            return "";
        }

        String rol = usuario.getRol().getNombre().trim().toUpperCase(Locale.ROOT);
        return rol.startsWith("ROLE_") ? rol.substring(5) : rol;
    }
    
    /**
     * Formulario para envío con archivos adjuntos
     */
    @GetMapping("/enviar-adjuntos")
    public String mostrarFormularioAdjuntos(Model model) {
        model.addAttribute("emailRequest", new EmailWebRequest());
        return "email/enviar-adjuntos";
    }
    
    /**
     * Procesar envío con archivos adjuntos
     */
    @PostMapping("/enviar-adjuntos")
    public String procesarEnvioConAdjuntos(@ModelAttribute EmailWebRequest emailWebRequest,
                                          BindingResult bindingResult,
                                          @RequestParam(required = false) String destinatariosTexto,
                                          @RequestParam(required = false) List<MultipartFile> archivos,
                                          RedirectAttributes redirectAttributes,
                                          Model model) {
        
        try {
            // Procesar lista de destinatarios
            if (destinatariosTexto != null && !destinatariosTexto.trim().isEmpty()) {
                List<String> destinatarios = Arrays.stream(destinatariosTexto.split("[,\\n\\r]+"))
                        .map(String::trim)
                        .filter(email -> !email.isEmpty())
                        .toList();
                emailWebRequest.setDestinatarios(destinatarios);
            }
            
            // Validaciones manuales
            if (emailWebRequest.getDestinatarios() == null || emailWebRequest.getDestinatarios().isEmpty()) {
                model.addAttribute("error", "Por favor ingresa al menos un destinatario");
                model.addAttribute("emailRequest", emailWebRequest);
                return "email/enviar-adjuntos";
            }
            
            if (emailWebRequest.getAsunto() == null || emailWebRequest.getAsunto().trim().isEmpty()) {
                model.addAttribute("error", "Por favor ingresa un asunto");
                model.addAttribute("emailRequest", emailWebRequest);
                return "email/enviar-adjuntos";
            }
            
            if (emailWebRequest.getMensaje() == null || emailWebRequest.getMensaje().trim().isEmpty()) {
                model.addAttribute("error", "Por favor ingresa un mensaje");
                model.addAttribute("emailRequest", emailWebRequest);
                return "email/enviar-adjuntos";
            }
            
            // Convertir a EmailRequest para el servicio
            EmailRequest emailRequest = emailWebRequest.toEmailRequest();
            EmailResponse response = emailService.enviarCorreosConAdjuntos(emailRequest, archivos);
            
            if (response.isExitoso()) {
                redirectAttributes.addFlashAttribute("success", 
                    "✅ Correos con adjuntos enviados exitosamente: " + response.getTotalEnviados());
            } else {
                redirectAttributes.addFlashAttribute("warning", 
                    "⚠️ Envío parcial: " + response.getTotalEnviados() + " enviados, " + 
                    response.getTotalFallidos() + " fallidos");
                redirectAttributes.addFlashAttribute("errores", response.getErrores());
            }
            
        } catch (Exception e) {
            log.error("Error en envío con adjuntos", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/email/enviar-adjuntos";
    }
    
    /**
     * Envío asíncrono
     */
    @PostMapping("/enviar-async")
    public String procesarEnvioAsync(@ModelAttribute EmailWebRequest emailWebRequest,
                                    BindingResult bindingResult,
                                    @RequestParam(required = false) String destinatariosTexto,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        
        try {
            // Procesar lista de destinatarios
            if (destinatariosTexto != null && !destinatariosTexto.trim().isEmpty()) {
                List<String> destinatarios = Arrays.stream(destinatariosTexto.split("[,\\n\\r]+"))
                        .map(String::trim)
                        .filter(email -> !email.isEmpty())
                        .toList();
                emailWebRequest.setDestinatarios(destinatarios);
            }
            
            // Validaciones manuales
            if (emailWebRequest.getDestinatarios() == null || emailWebRequest.getDestinatarios().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor ingresa al menos un destinatario");
                return "redirect:/email/enviar";
            }
            
            if (emailWebRequest.getAsunto() == null || emailWebRequest.getAsunto().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor ingresa un asunto");
                return "redirect:/email/enviar";
            }
            
            if (emailWebRequest.getMensaje() == null || emailWebRequest.getMensaje().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor ingresa un mensaje");
                return "redirect:/email/enviar";
            }
            
            // Convertir a EmailRequest para el servicio
            EmailRequest emailRequest = emailWebRequest.toEmailRequest();
            emailService.enviarCorreosMasivosAsync(emailRequest)
                .thenAccept(response -> {
                    log.info("Envío asíncrono completado: {} enviados, {} fallidos", 
                            response.getTotalEnviados(), response.getTotalFallidos());
                });
            
            redirectAttributes.addFlashAttribute("info", 
                "🚀 Envío asíncrono iniciado para " + emailWebRequest.getDestinatarios().size() + 
                " destinatarios. Los correos se están procesando en segundo plano.");
            
        } catch (Exception e) {
            log.error("Error en envío asíncrono", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }
        
        return "redirect:/email/enviar";
    }
    
    /**
     * Validar configuración SMTP
     */
    @GetMapping("/validar")
    @ResponseBody
    public String validarConfiguracion() {
        boolean esValida = emailService.validarConfiguracion();
        return esValida ? "✅ Configuración válida" : "❌ Configuración inválida";
    }
}