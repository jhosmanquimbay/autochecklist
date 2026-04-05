package com.example.demostracion.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.MercadoCompetenciaDTO;
import com.example.demostracion.dto.RadarPrecioDTO;
import com.example.demostracion.dto.RecallResumenDTO;
import com.example.demostracion.dto.ResumenValoracionDTO;
import com.example.demostracion.dto.ValoracionVehiculoResumenDTO;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.ValoracionVehiculo;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.service.GerenteValoracionService;

import java.util.List;

@Controller
@RequestMapping({"/gerente/valoracion", "/clima"})
public class ValoracionVehiculoController {

    private final UsuarioRepository usuarioRepository;
    private final GerenteValoracionService gerenteValoracionService;

    public ValoracionVehiculoController(UsuarioRepository usuarioRepository,
                                        GerenteValoracionService gerenteValoracionService) {
        this.usuarioRepository = usuarioRepository;
        this.gerenteValoracionService = gerenteValoracionService;
    }

    @GetMapping({"", "/"})
    public String listarValoraciones(@RequestParam(required = false) String marca,
                                     @RequestParam(required = false) String modelo,
                                     @RequestParam(required = false, defaultValue = "todos") String estadoPublicacion,
                                     Model model,
                                     Authentication auth) {
        agregarDatosUsuario(model, auth);

        List<ValoracionVehiculoResumenDTO> valoraciones = gerenteValoracionService.listarVehiculos(marca, modelo, estadoPublicacion);
        ResumenValoracionDTO resumen = gerenteValoracionService.construirResumen(valoraciones);

        model.addAttribute("valoraciones", valoraciones);
        model.addAttribute("resumenValoracion", resumen);
        model.addAttribute("marcaFiltro", marca == null ? "" : marca);
        model.addAttribute("modeloFiltro", modelo == null ? "" : modelo);
        model.addAttribute("estadoPublicacionFiltro", estadoPublicacion == null ? "todos" : estadoPublicacion);
        return "gerente/valoracion";
    }

    @GetMapping("/vehiculo/{id}")
    public String editarValoracion(@PathVariable Long id,
                                   Model model,
                                   Authentication auth) {
        agregarDatosUsuario(model, auth);
        cargarDetalle(model, gerenteValoracionService.prepararRegistro(id));
        return "gerente/valoracion-form";
    }

    @PostMapping("/vehiculo/{id}")
    public String guardarValoracion(@PathVariable Long id,
                                    @ModelAttribute("valoracion") ValoracionVehiculo valoracion,
                                    Model model,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            gerenteValoracionService.guardarRegistro(id, valoracion);
        } catch (IllegalArgumentException ex) {
            agregarDatosUsuario(model, auth);
            ValoracionVehiculo vista = gerenteValoracionService.prepararRegistro(id);
            vista.setSoatVencimiento(valoracion.getSoatVencimiento());
            vista.setTecnicomecanicaVencimiento(valoracion.getTecnicomecanicaVencimiento());
            vista.setTarjetaPropiedadOk(valoracion.isTarjetaPropiedadOk());
            vista.setImpuestosAlDia(valoracion.isImpuestosAlDia());
            vista.setPrendaActiva(valoracion.isPrendaActiva());
            vista.setPrecioObjetivoManual(valoracion.getPrecioObjetivoManual());
            vista.setObservaciones(valoracion.getObservaciones());
            cargarDetalle(model, vista);
            model.addAttribute("error", ex.getMessage());
            return "gerente/valoracion-form";
        }

        redirectAttributes.addFlashAttribute("success", "Valoración y validación guardadas correctamente.");
        return "redirect:/gerente/valoracion";
    }

    private void cargarDetalle(Model model, ValoracionVehiculo valoracion) {
        Vehiculo vehiculo = valoracion.getVehiculo();
        Inventario inventario = valoracion.getInventario();
        MercadoCompetenciaDTO mercadoCompetencia = gerenteValoracionService.consultarMercadoCompetencia(vehiculo.getIdVehiculo());
        RadarPrecioDTO radar = gerenteValoracionService.construirRadar(vehiculo.getIdVehiculo(), mercadoCompetencia);
        RecallResumenDTO alertasTecnicas = gerenteValoracionService.consultarAlertasTecnicas(vehiculo.getIdVehiculo());

        model.addAttribute("valoracion", valoracion);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("inventarioRelacionado", inventario);
        model.addAttribute("radarPrecio", radar);
        model.addAttribute("mercadoCompetencia", mercadoCompetencia);
        model.addAttribute("alertasTecnicas", alertasTecnicas);
        model.addAttribute("estadoDocumental", gerenteValoracionService.describirEstadoDocumental(valoracion));
        model.addAttribute("estadoDocumentalClase", gerenteValoracionService.claseEstadoDocumental(valoracion));
        model.addAttribute("listoParaPublicar", gerenteValoracionService.listoParaPublicar(vehiculo, valoracion));
    }

    private void agregarDatosUsuario(Model model, Authentication auth) {
        if (auth == null) {
            return;
        }
        usuarioRepository.findByCorreo(auth.getName()).ifPresent(u -> {
            model.addAttribute("usuarioId", u.getIdUsuario());
            model.addAttribute("usuarioNombre", u.getNombre());
            model.addAttribute("usuarioRol", u.getRol() != null ? u.getRol().getNombre() : "Gerente");
        });
    }
}