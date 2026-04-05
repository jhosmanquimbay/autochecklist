package com.example.demostracion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    private final String UPLOAD_DIR = "uploads/vehiculos/";
    private static List<VehiculoEjemplo> vehiculosData = new ArrayList<>();

    public CatalogoController() {
        // Crear directorio si no existe
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Inicializar datos de ejemplo si está vacío
        if (vehiculosData.isEmpty()) {
            inicializarDatos();
        }
    }

    // ========================================
    // VISTA CATÁLOGO (USUARIO) - SOLO LECTURA
    // ========================================
    
    @GetMapping
    @PreAuthorize("permitAll()")
    public String mostrarCatalogo(
            @RequestParam(required = false) String categoria,
            Model model) {
        
        List<VehiculoEjemplo> vehiculos = new ArrayList<>(vehiculosData);
        
        // Filtrar por categoría si se especifica y no es "Todos"
        if (categoria != null && !categoria.isEmpty() && 
            !categoria.equals("Todos") && !categoria.equals("")) {
            vehiculos = vehiculos.stream()
                    .filter(v -> categoria.equals(v.getCategoria()))
                    .toList();
        }
        
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("vistaUsuario", true);
        
        return "catalogo/usuario";
    }

    // Ruta específica para "Todos" en vista usuario
    @GetMapping("/todos")
    @PreAuthorize("permitAll()")
    public String mostrarTodos(Model model) {
        model.addAttribute("vehiculos", new ArrayList<>(vehiculosData));
        model.addAttribute("categoriaSeleccionada", "Todos");
        model.addAttribute("vistaUsuario", true);
        return "catalogo/usuario";
    }

    @GetMapping("/detalles/{id}")
    @PreAuthorize("permitAll()")
    public String verDetalles(@PathVariable Long id, Model model) {
        VehiculoEjemplo vehiculo = vehiculosData.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (vehiculo == null) {
            return "redirect:/catalogo";
        }
        
        model.addAttribute("vehiculo", vehiculo);
        return "catalogo/detalles";
    }

    // ========================================
    // VISTA GESTIÓN (GERENTE) - ADMINISTRACIÓN
    // ========================================
    
    @GetMapping("/gestion")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String mostrarGestion(
            @RequestParam(required = false) String categoria,
            Model model) {
        
        List<VehiculoEjemplo> vehiculos = new ArrayList<>(vehiculosData);
        
        // Filtrar por categoría si se especifica y no es "Todos"
        if (categoria != null && !categoria.isEmpty() && 
            !categoria.equals("Todos") && !categoria.equals("")) {
            vehiculos = vehiculos.stream()
                    .filter(v -> categoria.equals(v.getCategoria()))
                    .toList();
        }
        
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("vistaGerente", true);
        
        return "catalogo/gestion";
    }

    // Ruta específica para "Todos" en vista gestión
    @GetMapping("/gestion/todos")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String mostrarTodosGestion(Model model) {
        model.addAttribute("vehiculos", new ArrayList<>(vehiculosData));
        model.addAttribute("categoriaSeleccionada", "Todos");
        model.addAttribute("vistaGerente", true);
        return "catalogo/gestion";
    }

    @GetMapping("/agregar")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String mostrarFormulario(Model model) {
        model.addAttribute("vehiculo", new VehiculoEjemplo());
        model.addAttribute("titulo", "Agregar Vehículo");
        model.addAttribute("esEdicion", false);
        return "catalogo/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String guardarVehiculo(
            @ModelAttribute VehiculoEjemplo vehiculo,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Procesar imagen si se subió una
            if (imagenFile != null && !imagenFile.isEmpty()) {
                String nombreImagen = guardarImagen(imagenFile);
                vehiculo.setImagenUrl("/uploads/vehiculos/" + nombreImagen);
            }
            
            // Asignar ID único
            vehiculo.setId(System.currentTimeMillis());
            
            // Agregar a la lista
            vehiculosData.add(vehiculo);
            
            redirectAttributes.addFlashAttribute("success", "Vehículo agregado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el vehículo: " + e.getMessage());
        }
        
        return "redirect:/catalogo/gestion";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String editarVehiculo(@PathVariable Long id, Model model) {
        VehiculoEjemplo vehiculo = vehiculosData.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (vehiculo == null) {
            return "redirect:/catalogo/gestion";
        }
        
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("titulo", "Editar Vehículo");
        model.addAttribute("esEdicion", true);
        return "catalogo/formulario";
    }

    @PostMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String actualizarVehiculo(
            @PathVariable Long id,
            @ModelAttribute VehiculoEjemplo vehiculo,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Buscar vehículo existente
            VehiculoEjemplo vehiculoExistente = vehiculosData.stream()
                    .filter(v -> v.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (vehiculoExistente != null) {
                // Procesar nueva imagen si se subió
                if (imagenFile != null && !imagenFile.isEmpty()) {
                    String nombreImagen = guardarImagen(imagenFile);
                    vehiculo.setImagenUrl("/uploads/vehiculos/" + nombreImagen);
                } else {
                    // Mantener imagen existente
                    vehiculo.setImagenUrl(vehiculoExistente.getImagenUrl());
                }
                
                vehiculo.setId(id);
                
                // Reemplazar en la lista
                int index = vehiculosData.indexOf(vehiculoExistente);
                vehiculosData.set(index, vehiculo);
                
                redirectAttributes.addFlashAttribute("success", "Vehículo actualizado exitosamente");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el vehículo: " + e.getMessage());
        }
        
        return "redirect:/catalogo/gestion";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public String eliminarVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehiculosData.removeIf(v -> v.getId().equals(id));
            redirectAttributes.addFlashAttribute("success", "Vehículo eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el vehículo");
        }
        
        return "redirect:/catalogo/gestion";
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================
    
    private void inicializarDatos() {
        vehiculosData.add(new VehiculoEjemplo(1L, "Mercedes", "AMG GT", "Sedán", 
            "Vehículos de lujo con alto rendimiento y tecnología avanzada.", 85000.0,
            "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=400&h=250&fit=crop"));
        
        vehiculosData.add(new VehiculoEjemplo(2L, "Toyota", "Land Cruiser", "4x4",
            "Vehículos todo terreno para aventuras y trabajo pesado.", 65000.0,
            "https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=400&h=250&fit=crop"));
        
        vehiculosData.add(new VehiculoEjemplo(3L, "Chevrolet", "Express", "Pasajeros",
            "Vehículos espaciosos para transporte de pasajeros y carga.", 45000.0,
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=250&fit=crop"));
        
        vehiculosData.add(new VehiculoEjemplo(4L, "Ford", "Explorer", "4x4",
            "Vehículos robustos con capacidad off road superior.", 55000.0,
            "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=400&h=250&fit=crop"));
    }

    private String guardarImagen(MultipartFile archivo) throws IOException {
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = nombreOriginal != null ? 
            nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) : ".jpg";
        
        String nombreUnico = UUID.randomUUID().toString() + extension;
        Path rutaArchivo = Paths.get(UPLOAD_DIR + nombreUnico);
        
        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        
        return nombreUnico;
    }

    // ========================================
    // CLASE MODELO
    // ========================================
    
    public static class VehiculoEjemplo {
        private Long id;
        private String marca;
        private String modelo;
        private String categoria;
        private String descripcion;
        private Double precio;
        private String imagenUrl;
        
        public VehiculoEjemplo() {}
        
        public VehiculoEjemplo(Long id, String marca, String modelo, String categoria, String descripcion, Double precio, String imagenUrl) {
            this.id = id;
            this.marca = marca;
            this.modelo = modelo;
            this.categoria = categoria;
            this.descripcion = descripcion;
            this.precio = precio;
            this.imagenUrl = imagenUrl;
        }
        
        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
        
        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
        
        public String getTitulo() { return marca + " " + modelo; }
    }
}