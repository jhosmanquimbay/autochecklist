package com.example.demostracion.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import com.example.demostracion.service.VehiculoPublicacionService;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final VehiculoPublicacionService vehiculoPublicacionService;

    public VehiculoController(VehiculoRepository vehiculoRepository,
                              InventarioRepository inventarioRepository,
                              VehiculoPublicacionService vehiculoPublicacionService) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.vehiculoPublicacionService = vehiculoPublicacionService;
    }

    @GetMapping
    public String listarVehiculos(@RequestParam(required = false, defaultValue = "todos") String estadoPublicacion,
                                  Model model) {
        List<Vehiculo> vehiculos = vehiculoPublicacionService.listarVehiculos(estadoPublicacion);
        Map<String, Integer> stockPorChasis = inventarioRepository.findAll().stream()
                .filter(inventario -> inventario.getChasis() != null && !inventario.getChasis().isBlank())
                .collect(Collectors.toMap(
                        inventario -> inventario.getChasis().trim().toLowerCase(Locale.ROOT),
                inventario -> inventario.isActivo() ? obtenerCantidadInventario(inventario.getCantidadDisponible()) : 0,
                        Integer::sum));

        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("stockPorChasis", stockPorChasis);
        model.addAttribute("estadoPublicacionFiltro", vehiculoPublicacionService.normalizarFiltro(estadoPublicacion));
        model.addAttribute("conteoBorradores", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_BORRADOR));
        model.addAttribute("conteoPendientes", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_PENDIENTE));
        model.addAttribute("conteoPublicados", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_PUBLICADO));
        model.addAttribute("conteoDevueltos", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_DEVUELTO));
        model.addAttribute("conteoDespublicados", vehiculoPublicacionService.contarPorEstado(Vehiculo.ESTADO_PUBLICACION_DESPUBLICADO));
        return "vehiculos/index"; // ✅ CORREGIDO
    }

    // soporta ambos caminos porque los templates usaban "/vehiculos/crear" pero el método original
    // estaba en "/nuevo"; dejamos los dos para evitar 404 si alguno cambia.
    @GetMapping({"/nuevo","/crear"})
    public String nuevoVehiculo(@RequestParam(required = false) Long inventarioId, Model model) {
        prepararFormularioVehiculoAdmin(model, vehiculoPublicacionService.prepararNuevoVehiculoAdmin(inventarioId));
        return "vehiculos/form";
    }

    @PostMapping("/guardar")
    public String guardarVehiculo(
            @ModelAttribute Vehiculo vehiculo,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            @RequestParam(value = "imagenFile2", required = false) MultipartFile imagenFile2,
            @RequestParam(value = "imagenFile3", required = false) MultipartFile imagenFile3,
            @RequestParam(value = "imagenFile4", required = false) MultipartFile imagenFile4,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            vehiculoPublicacionService.crearDesdeAdmin(vehiculo, imagenFile, imagenFile2, imagenFile3, imagenFile4);
        } catch (IllegalArgumentException e) {
            prepararFormularioVehiculoAdmin(model, vehiculo);
            model.addAttribute("error", e.getMessage());
            return "vehiculos/form";
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible procesar las imagenes del vehiculo", e);
        }

        redirectAttributes.addFlashAttribute("success", "Vehículo registrado y publicado correctamente.");

        return "redirect:/vehiculos"; // ✅ CORRECTO
    }

    @GetMapping("/editar/{id}")
    public String editarVehiculo(@PathVariable Long id, Model model) {
        Vehiculo vehiculo = vehiculoPublicacionService.prepararVehiculoExistenteParaFormulario(
                vehiculoPublicacionService.obtenerVehiculo(id));
        prepararFormularioVehiculoAdmin(model, vehiculo);
        return "vehiculos/form"; // para editar
    }

    @GetMapping("/carga-masiva")
    public String cargaMasivaVehiculos() {
        return "vehiculos/carga-masiva";
    }

    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> getImagenVehiculo(@PathVariable Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        if (vehiculo.getImagen() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // or IMAGE_PNG depending on your images
                    .body(vehiculo.getImagen());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarVehiculo(@PathVariable Long id,
                                     @ModelAttribute Vehiculo vehiculo,
                                     @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                     @RequestParam(value = "imagenFile2", required = false) MultipartFile imagenFile2,
                                     @RequestParam(value = "imagenFile3", required = false) MultipartFile imagenFile3,
                                     @RequestParam(value = "imagenFile4", required = false) MultipartFile imagenFile4,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            vehiculoPublicacionService.actualizarDesdeAdmin(id, vehiculo, imagenFile, imagenFile2, imagenFile3, imagenFile4);
        } catch (IllegalArgumentException e) {
            vehiculo.setIdVehiculo(id);
            prepararFormularioVehiculoAdmin(model, vehiculo);
            model.addAttribute("error", e.getMessage());
            return "vehiculos/form";
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible procesar las imagenes del vehiculo", e);
        }
        redirectAttributes.addFlashAttribute("success", "Vehículo actualizado correctamente.");
        return "redirect:/vehiculos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vehiculoPublicacionService.despublicar(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo retirado de la publicación.");
        return "redirect:/vehiculos";
    }

    @PostMapping("/{id}/publicar")
    public String publicarVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vehiculoPublicacionService.publicar(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo publicado correctamente.");
        return "redirect:/vehiculos";
    }

    @PostMapping("/{id}/devolver")
    public String devolverVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vehiculoPublicacionService.devolverAGerencia(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo devuelto a gerencia para ajustes.");
        return "redirect:/vehiculos";
    }

    @PostMapping("/{id}/despublicar")
    public String despublicarVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vehiculoPublicacionService.despublicar(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo despublicado correctamente.");
        return "redirect:/vehiculos";
    }

    private Integer obtenerCantidadInventario(Integer cantidadDisponible) {
        return cantidadDisponible == null || cantidadDisponible < 1 ? 1 : cantidadDisponible;
    }

    private void prepararFormularioVehiculoAdmin(Model model, Vehiculo vehiculo) {
        model.addAttribute("vehiculo", vehiculoPublicacionService.prepararVehiculoExistenteParaFormulario(vehiculo));
        model.addAttribute("inventarios", vehiculoPublicacionService.listarInventariosSeleccionables());
        model.addAttribute("modoAdmin", true);
    }
}