package com.example.demostracion.controller;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.FinanciamientoResultadoDTO;
import com.example.demostracion.dto.FinanciamientoSolicitudForm;
import com.example.demostracion.dto.HomeBannerForm;
import com.example.demostracion.dto.SolicitudInteresForm;
import com.example.demostracion.dto.VehiculoClienteDTO;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.service.ClienteCuentaService;
import com.example.demostracion.service.FinanciamientoService;
import com.example.demostracion.service.HomeBannerService;
import com.example.demostracion.service.OficinaClienteService;

import jakarta.validation.Valid;

@Controller
public class HomeController {

    private final OficinaClienteService oficinaClienteService;
    private final ClienteCuentaService clienteCuentaService;
    private final FinanciamientoService financiamientoService;
    private final HomeBannerService homeBannerService;

    public HomeController(OficinaClienteService oficinaClienteService,
                          ClienteCuentaService clienteCuentaService,
                          FinanciamientoService financiamientoService,
                          HomeBannerService homeBannerService) {
        this.oficinaClienteService = oficinaClienteService;
        this.clienteCuentaService = clienteCuentaService;
        this.financiamientoService = financiamientoService;
        this.homeBannerService = homeBannerService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String marca,
                        Authentication authentication,
                        Model model) {
        List<VehiculoClienteDTO> vehiculos = oficinaClienteService.obtenerVehiculos(marca);
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("destacados", oficinaClienteService.obtenerDestacados(vehiculos));
        model.addAttribute("marcas", oficinaClienteService.obtenerMarcasDisponibles());
        model.addAttribute("marcaSeleccionada", marca);

        List<HomeBannerForm> bannersActivos = homeBannerService.listarActivos();
        HomeBannerForm heroBannerInicial = seleccionarBannerInicial(bannersActivos, "hero");
        List<HomeBannerForm> catalogBannersIniciales = bannersActivos.stream()
            .filter(banner -> coincidePlacement(banner, "catalog"))
            .limit(3)
            .toList();

        model.addAttribute("heroBannerInicial", heroBannerInicial);
        model.addAttribute("heroBannerInicialTieneTexto", bannerTieneTexto(heroBannerInicial));
        model.addAttribute("catalogBannersIniciales", catalogBannersIniciales);

        agregarContextoUsuario(authentication, model);

        return "index";
    }

    @GetMapping("/oficina/vehiculos/{id}")
    public String detalleVehiculo(@PathVariable Long id,
                                  Authentication authentication,
                                  Model model) {
        cargarDetalleVehiculo(id, authentication, model);
        return "oficina/vehiculo-detalle";
    }

    @PostMapping("/oficina/vehiculos/{id}/interes")
    public String registrarInteres(@PathVariable Long id,
                                   @Valid @ModelAttribute("interesForm") SolicitudInteresForm interesForm,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        interesForm.setVehiculoId(id);

        boolean autenticado = authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName());

        if (!autenticado) {
            validarDatosContacto(interesForm, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            cargarDetalleVehiculo(id, authentication, model);
            model.addAttribute("abrirModalInteres", true);
            return "oficina/vehiculo-detalle";
        }

        String correoAutenticado = correoAutenticado(authentication);
        clienteCuentaService.registrarInteresVehiculo(id, interesForm, correoAutenticado);
        redirectAttributes.addFlashAttribute("mensajeInteres", "Tu información fue registrada correctamente. Un asesor se pondrá en contacto contigo pronto.");
        return "redirect:/oficina/vehiculos/" + id;
    }

    @PostMapping("/oficina/vehiculos/{id}/financiamiento")
    public String registrarFinanciamiento(@PathVariable Long id,
                                          @ModelAttribute("financiamientoForm") FinanciamientoSolicitudForm financiamientoForm,
                                          Authentication authentication,
                                          Model model) {
        financiamientoForm.setVehiculoId(id);

        boolean autenticado = authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName());

        cargarDetalleVehiculo(id, authentication, model);

        if (!autenticado) {
            validarDatosContactoFinanciamiento(financiamientoForm, model);
            if (Boolean.TRUE.equals(model.getAttribute("errorFormularioFinanciamiento"))) {
                model.addAttribute("errorFinanciamiento", model.getAttribute("errorFinanciamiento"));
                return "oficina/vehiculo-detalle";
            }
        }

        try {
            String correoAutenticado = correoAutenticado(authentication);
            FinanciamientoResultadoDTO resultado = financiamientoService.registrarSimulacion(
                    id,
                    financiamientoForm,
                    "CLIENTE_WEB",
                    null,
                    null,
                    correoAutenticado);
            model.addAttribute("resultadoFinanciamiento", resultado);
            model.addAttribute("mensajeFinanciamiento", "Simulación registrada. Un asesor podrá continuar el estudio con tus documentos.");
            model.addAttribute("financiamientoActivo", true);
            return "oficina/vehiculo-detalle";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorFinanciamiento", ex.getMessage());
            model.addAttribute("financiamientoActivo", true);
            return "oficina/vehiculo-detalle";
        }
    }

    @GetMapping("/login-expired")
    public String loginExpired() {
        return "login-expired";
    }

    private void agregarContextoUsuario(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
            Usuario usuarioActual = clienteCuentaService.obtenerUsuarioPorCorreo(authentication.getName());
            boolean esClientePortal = clienteCuentaService.esCliente(usuarioActual);
            Set<Long> favoritosIds = esClientePortal
                    ? clienteCuentaService.obtenerIdsFavoritos(authentication.getName())
                    : Set.of();

            model.addAttribute("usuarioActual", usuarioActual);
            model.addAttribute("esClientePortal", esClientePortal);
            model.addAttribute("favoritosIds", favoritosIds);
            return;
        }

        model.addAttribute("usuarioActual", null);
        model.addAttribute("esClientePortal", false);
        model.addAttribute("favoritosIds", Set.of());
    }

    private SolicitudInteresForm construirFormularioInteres(Authentication authentication, Long idVehiculo) {
        SolicitudInteresForm form = new SolicitudInteresForm();
        form.setVehiculoId(idVehiculo);

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
            Usuario usuarioActual = clienteCuentaService.obtenerUsuarioPorCorreo(authentication.getName());
            form.setNombreCompleto(usuarioActual.getNombre());
            form.setCorreo(usuarioActual.getCorreo());
            form.setTelefono(usuarioActual.getTelefono());
        }

        return form;
    }

    private FinanciamientoSolicitudForm construirFormularioFinanciamiento(Authentication authentication,
                                                                          Long idVehiculo,
                                                                          FinanciamientoResultadoDTO referencia) {
        FinanciamientoSolicitudForm form = new FinanciamientoSolicitudForm();
        form.setVehiculoId(idVehiculo);
        form.setCanalOrigen("CLIENTE_WEB");
        if (referencia != null) {
            form.setCuotaInicial(referencia.getCuotaInicialMinima());
            form.setPlazoMeses(referencia.getPlazoMeses());
        }

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
            Usuario usuarioActual = clienteCuentaService.obtenerUsuarioPorCorreo(authentication.getName());
            form.setNombreCompleto(usuarioActual.getNombre());
            form.setCorreo(usuarioActual.getCorreo());
            form.setTelefono(usuarioActual.getTelefono());
        }

        return form;
    }

    private void cargarDetalleVehiculo(Long id, Authentication authentication, Model model) {
        VehiculoClienteDTO vehiculo = oficinaClienteService.obtenerVehiculoDetalle(id);
        FinanciamientoResultadoDTO referenciaFinanciamiento = financiamientoService.calcularOfertaReferencial(id);

        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("relacionados", oficinaClienteService.obtenerRelacionados(id, vehiculo.getMarca()));
        model.addAttribute("financiamientoReferencia", referenciaFinanciamiento);
        if (!model.containsAttribute("interesForm")) {
            model.addAttribute("interesForm", construirFormularioInteres(authentication, id));
        }
        if (!model.containsAttribute("financiamientoForm")) {
            model.addAttribute("financiamientoForm", construirFormularioFinanciamiento(authentication, id, referenciaFinanciamiento));
        }
        agregarContextoUsuario(authentication, model);
    }

    private void validarDatosContacto(SolicitudInteresForm form, BindingResult bindingResult) {
        if (form.getNombreCompleto() == null || form.getNombreCompleto().isBlank()) {
            bindingResult.rejectValue("nombreCompleto", "nombreCompleto.requerido", "Ingresa tu nombre completo.");
        }
        if (form.getCorreo() == null || form.getCorreo().isBlank()) {
            bindingResult.rejectValue("correo", "correo.requerido", "Ingresa tu correo.");
        }
        if (form.getTelefono() == null || form.getTelefono().isBlank()) {
            bindingResult.rejectValue("telefono", "telefono.requerido", "Ingresa tu teléfono.");
        }
    }

    private HomeBannerForm seleccionarBannerInicial(List<HomeBannerForm> banners, String placement) {
        return banners.stream()
                .filter(HomeBannerForm::isActive)
                .filter(banner -> coincidePlacement(banner, placement))
                .findFirst()
                .orElseGet(() -> banners.stream().filter(HomeBannerForm::isActive).findFirst().orElse(null));
    }

    private boolean coincidePlacement(HomeBannerForm banner, String esperado) {
        if (banner == null || banner.getPlacement() == null || esperado == null) {
            return false;
        }

        String placement = banner.getPlacement().trim().toLowerCase();
        String objetivo = esperado.trim().toLowerCase();
        if ("catalog".equals(objetivo) && "showcase".equals(placement)) {
            return true;
        }

        return placement.equals(objetivo);
    }

    private boolean bannerTieneTexto(HomeBannerForm banner) {
        if (banner == null) {
            return false;
        }

        return tieneTexto(banner.getEyebrow())
                || tieneTexto(banner.getTitle())
                || tieneTexto(banner.getDescription())
                || (tieneTexto(banner.getCtaText()) && tieneTexto(banner.getCtaUrl()))
                || tieneTexto(banner.getMetricValue())
                || tieneTexto(banner.getMetricLabel());
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private void validarDatosContactoFinanciamiento(FinanciamientoSolicitudForm form, Model model) {
        if (form.getNombreCompleto() == null || form.getNombreCompleto().isBlank()) {
            model.addAttribute("errorFinanciamiento", "Ingresa el nombre del solicitante.");
            model.addAttribute("errorFormularioFinanciamiento", true);
            return;
        }
        if (form.getCorreo() == null || form.getCorreo().isBlank()) {
            model.addAttribute("errorFinanciamiento", "Ingresa el correo del solicitante.");
            model.addAttribute("errorFormularioFinanciamiento", true);
            return;
        }
        if (form.getTelefono() == null || form.getTelefono().isBlank()) {
            model.addAttribute("errorFinanciamiento", "Ingresa el teléfono del solicitante.");
            model.addAttribute("errorFormularioFinanciamiento", true);
            return;
        }
        if (form.getCuotaInicial() == null || form.getCuotaInicial() <= 0.0) {
            model.addAttribute("errorFinanciamiento", "Ingresa una cuota inicial válida.");
            model.addAttribute("errorFormularioFinanciamiento", true);
            return;
        }
        if (form.getIngresoMensual() == null || form.getIngresoMensual() <= 0.0) {
            model.addAttribute("errorFinanciamiento", "Ingresa el ingreso mensual del solicitante.");
            model.addAttribute("errorFormularioFinanciamiento", true);
            return;
        }
        model.addAttribute("errorFormularioFinanciamiento", false);
    }

    private String correoAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }
}
