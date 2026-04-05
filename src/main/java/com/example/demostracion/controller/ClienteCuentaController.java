package com.example.demostracion.controller;

import java.security.Principal;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.ClientePerfilForm;
import com.example.demostracion.dto.ClienteRegistroForm;
import com.example.demostracion.service.ClienteCuentaService;
import com.example.demostracion.service.OficinaClienteService;
import com.example.demostracion.service.UsuarioDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping
public class ClienteCuentaController {

    private final ClienteCuentaService clienteCuentaService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final OficinaClienteService oficinaClienteService;

    public ClienteCuentaController(ClienteCuentaService clienteCuentaService,
                                   UsuarioDetailsService usuarioDetailsService,
                                   OficinaClienteService oficinaClienteService) {
        this.clienteCuentaService = clienteCuentaService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.oficinaClienteService = oficinaClienteService;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registroForm")) {
            model.addAttribute("registroForm", new ClienteRegistroForm());
        }
        return "cliente/registro";
    }

    @PostMapping("/registro")
    public String registrarCliente(@Valid @ModelAttribute("registroForm") ClienteRegistroForm registroForm,
                                   BindingResult bindingResult,
                                   HttpServletRequest request) {
        validarDuplicados(registroForm.getCorreo(), registroForm.getCedula(), null, bindingResult);

        if (bindingResult.hasErrors()) {
            return "cliente/registro";
        }

        clienteCuentaService.registrarCliente(registroForm);
        autenticarUsuario(registroForm.getCorreo(), request);
        return "redirect:/?registro=ok";
    }

    @GetMapping("/cliente/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public String verPerfil(Model model, Principal principal) {
        if (!model.containsAttribute("perfilForm")) {
            model.addAttribute("perfilForm", clienteCuentaService.construirPerfil(principal.getName()));
        }
        model.addAttribute("usuarioActual", clienteCuentaService.obtenerUsuarioPorCorreo(principal.getName()));
        return "cliente/perfil";
    }

    @PostMapping("/cliente/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public String actualizarPerfil(@Valid @ModelAttribute("perfilForm") ClientePerfilForm perfilForm,
                                   BindingResult bindingResult,
                                   Principal principal,
                                   HttpServletRequest request,
                                   Model model) {
        Long usuarioActualId = clienteCuentaService.obtenerUsuarioPorCorreo(principal.getName()).getIdUsuario();
        validarDuplicados(perfilForm.getCorreo(), perfilForm.getCedula(), usuarioActualId, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuarioActual", clienteCuentaService.obtenerUsuarioPorCorreo(principal.getName()));
            return "cliente/perfil";
        }

        var actualizado = clienteCuentaService.actualizarPerfil(principal.getName(), perfilForm);
        autenticarUsuario(actualizado.getCorreo(), request);
        return "redirect:/cliente/perfil?actualizado";
    }

    @GetMapping("/cliente/compras")
    @PreAuthorize("hasRole('CLIENTE')")
    public String verCompras(Model model, Principal principal) {
        model.addAttribute("usuarioActual", clienteCuentaService.obtenerUsuarioPorCorreo(principal.getName()));
        model.addAttribute("compras", clienteCuentaService.obtenerCompras(principal.getName()));
        return "cliente/compras";
    }

    @GetMapping("/cliente/favoritos")
    @PreAuthorize("hasRole('CLIENTE')")
    public String verFavoritos(Model model, Principal principal) {
        Set<Long> favoritosIds = clienteCuentaService.obtenerIdsFavoritos(principal.getName());
        model.addAttribute("usuarioActual", clienteCuentaService.obtenerUsuarioPorCorreo(principal.getName()));
        model.addAttribute("favoritosIds", favoritosIds);
        model.addAttribute("favoritos", oficinaClienteService.obtenerVehiculosPorIds(favoritosIds));
        return "cliente/favoritos";
    }

    @PostMapping("/cliente/favoritos/{vehiculoId}/toggle")
    @PreAuthorize("hasRole('CLIENTE')")
    public String alternarFavorito(@PathVariable Long vehiculoId,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        boolean agregado = clienteCuentaService.alternarFavorito(principal.getName(), vehiculoId);
        redirectAttributes.addFlashAttribute("favoritoMensaje", agregado
                ? "Vehículo agregado a favoritos."
                : "Vehículo eliminado de favoritos.");

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/cliente/favoritos";
    }

    private void validarDuplicados(String correo, String cedula, Long usuarioActualId, BindingResult bindingResult) {
        if (clienteCuentaService.correoPerteneceAOtroUsuario(correo, usuarioActualId)) {
            bindingResult.rejectValue("correo", "correo.duplicado", "Ya existe una cuenta registrada con ese correo.");
        }
        if (clienteCuentaService.cedulaPerteneceAOtroUsuario(cedula, usuarioActualId)) {
            bindingResult.rejectValue("cedula", "cedula.duplicada", "Ya existe una cuenta registrada con esa cédula.");
        }
    }

    private void autenticarUsuario(String correo, HttpServletRequest request) {
        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(correo);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}