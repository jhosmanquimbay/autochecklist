package com.example.demostracion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demostracion.dto.FinanciamientoResultadoDTO;
import com.example.demostracion.dto.FinanciamientoSolicitudForm;
import com.example.demostracion.dto.ResumenFinanciamientoDTO;
import com.example.demostracion.dto.SeguimientoFinanciamientoForm;
import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.Novedad;
import com.example.demostracion.model.Pedido;
import com.example.demostracion.model.SolicitudFinanciamiento;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.ConductorRepository;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.NovedadRepository;
import com.example.demostracion.repository.PedidoRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;
import com.example.demostracion.service.FinanciamientoService;
import com.example.demostracion.service.InventarioVentaService;
import com.example.demostracion.service.MensajeService;

@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    private static final String UPLOAD_DIR = "uploads/novedades/";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CO"));

    private final InventarioRepository inventarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PedidoRepository pedidoRepository;
    private final NovedadRepository novedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;
    private final FinanciamientoService financiamientoService;
    private final InventarioVentaService inventarioVentaService;
    private final MensajeService mensajeService;

    public VendedorController(InventarioRepository inventarioRepository,
                              VehiculoRepository vehiculoRepository,
                              PedidoRepository pedidoRepository,
                              NovedadRepository novedadRepository,
                              UsuarioRepository usuarioRepository,
                              ConductorRepository conductorRepository,
                              FinanciamientoService financiamientoService,
                              InventarioVentaService inventarioVentaService,
                              MensajeService mensajeService) {
        this.inventarioRepository = inventarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.pedidoRepository = pedidoRepository;
        this.novedadRepository = novedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
        this.financiamientoService = financiamientoService;
        this.inventarioVentaService = inventarioVentaService;
        this.mensajeService = mensajeService;
    }

    @GetMapping("/panel")
    public String panel(Model model, Principal principal, Authentication auth) {
        Usuario usuario = obtenerUsuario(auth);
        Conductor vendedor = obtenerOCrearFichaVendedor(principal);

        List<Pedido> ventas = vendedor.getIdConductor() != null
                ? pedidoRepository.findByConductorIdConductor(vendedor.getIdConductor())
                : List.of();

        long ventasCerradas = ventas.stream()
                .filter(p -> esEstadoVenta(p.getEstado()))
                .count();

        long oportunidades = ventas.stream()
                .filter(p -> !esEstadoVenta(p.getEstado()))
                .count();

        long vehiculosDisponibles = inventarioRepository.findByActivoTrue().size();

        List<Novedad> misNovedades = usuario == null ? List.of() : novedadRepository.findAll().stream()
                .filter(n -> n.getUsuario() != null && n.getUsuario().getIdUsuario() != null)
                .filter(n -> n.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .sorted(Comparator.comparing(Novedad::getFechaReporte, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        ResumenFinanciamientoDTO resumenFinanciamientoVendedor = financiamientoService.construirResumenPorAsesor(vendedor.getUsername());

        model.addAttribute("vendedor", vendedor);
        model.addAttribute("vehiculosDisponibles", vehiculosDisponibles);
        model.addAttribute("ventasCerradas", ventasCerradas);
        model.addAttribute("oportunidades", oportunidades);
        model.addAttribute("novedadesPendientes", misNovedades.stream().filter(n -> !"resuelto".equals(normalizarEstado(n.getEstado()))).count());
        model.addAttribute("resumenFinanciamientoVendedor", resumenFinanciamientoVendedor);
        model.addAttribute("ultimasVentas", ventas.stream()
                .sorted(Comparator.comparing(Pedido::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList());

        agregarDatosCorreo(model, auth);
        return "vendedor/panel";
    }

    @GetMapping
    public String inicioVendedor() {
        return "redirect:/vendedor/panel";
    }

    @GetMapping("/vehiculos")
    public String vehiculosDisponibles(Model model,
                                       Authentication auth,
                                       @RequestParam(required = false, defaultValue = "") String marca,
                                       @RequestParam(required = false, defaultValue = "") String modelo,
                                       @RequestParam(required = false, defaultValue = "") String color) {
        List<Inventario> disponibles = inventarioRepository.findByActivoTrue().stream()
                .filter(i -> contiene(i.getMarca(), marca))
                .filter(i -> contiene(i.getModelo(), modelo))
                .filter(i -> contiene(i.getColor(), color))
                .sorted(Comparator.comparing(Inventario::getMarca, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Inventario::getModelo, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        model.addAttribute("inventarios", disponibles);
        model.addAttribute("marca", marca);
        model.addAttribute("modelo", modelo);
        model.addAttribute("color", color);

        agregarDatosCorreo(model, auth);
        return "vendedor/catalogo";
    }

    @GetMapping("/novedades")
    public String novedades(Model model, Principal principal, Authentication auth) {
        Usuario usuario = obtenerUsuario(auth);
        Novedad novedad = new Novedad();
        novedad.setEstado("pendiente");
        novedad.setPrioridad("media");
        novedad.setOrigenReporte("vendedor");
        novedad.setAplicaGarantia(Boolean.FALSE);

        List<Novedad> misNovedades = usuario == null ? List.of() : novedadRepository.findAll().stream()
                .filter(n -> n.getUsuario() != null && n.getUsuario().getIdUsuario() != null)
                .filter(n -> n.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .sorted(Comparator.comparing(Novedad::getFechaReporte, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("novedad", novedad);
        model.addAttribute("misNovedades", misNovedades);
        model.addAttribute("inventariosDisponibles", inventarioRepository.findByActivoTrue());
        model.addAttribute("vendedor", obtenerOCrearFichaVendedor(principal));

        agregarDatosCorreo(model, auth);
        return "vendedor/novedades";
    }

    @PostMapping("/novedades")
    public String guardarNovedad(@ModelAttribute Novedad novedad,
                                 @RequestParam("file") MultipartFile file,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) throws IOException {
        Usuario usuario = obtenerUsuario(auth);
        novedad.setUsuario(usuario);
        novedad.setEstado(normalizarEstado(novedad.getEstado()));
        novedad.setPrioridad(normalizarPrioridad(novedad.getPrioridad()));
        novedad.setOrigenReporte("vendedor");
        novedad.setFechaReporte(LocalDateTime.now());
        novedad.setFechaGestion(LocalDateTime.now());

        if (novedad.getAplicaGarantia() == null) {
            novedad.setAplicaGarantia(Boolean.FALSE);
        }

        completarDatosVehiculoNovedad(novedad);

        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            novedad.setEvidencia(fileName);
        }

        novedadRepository.save(novedad);
        redirectAttributes.addFlashAttribute("success", "Novedad reportada al gerente correctamente.");
        return "redirect:/vendedor/novedades";
    }

    @GetMapping("/ventas")
    public String ventas(Model model, Principal principal, Authentication auth) {
        Conductor vendedor = obtenerOCrearFichaVendedor(principal);

        List<Pedido> misVentas = vendedor.getIdConductor() != null
                ? pedidoRepository.findByConductorIdConductor(vendedor.getIdConductor()).stream()
                .sorted(Comparator.comparing(Pedido::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList()
                : List.of();

        model.addAttribute("misVentas", misVentas);
        model.addAttribute("inventariosDisponibles", inventarioRepository.findByActivoTrue());
        model.addAttribute("vendedor", vendedor);
        cargarModuloFinanciamiento(model, vendedor, null, null, null);

        agregarDatosCorreo(model, auth);
        return "vendedor/clientes";
    }

    @PostMapping("/ventas")
    public String registrarVenta(@RequestParam Long inventarioId,
                                 @RequestParam String clienteNombre,
                                 @RequestParam(required = false, defaultValue = "") String clienteTelefono,
                                 @RequestParam(required = false, defaultValue = "") String clienteCorreo,
                                 @RequestParam(required = false, defaultValue = "") String observacion,
                                 @RequestParam(defaultValue = "negociando") String estado,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (inventarioId == null) {
            throw new IllegalArgumentException("Inventario requerido");
        }
        long inventarioSeguro = inventarioId;
        Inventario inventario = inventarioRepository.findById(inventarioSeguro)
            .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));

        Vehiculo vehiculo = vehiculoRepository.findByChasis(inventario.getChasis())
            .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado para el chasis seleccionado"));

        Conductor vendedor = obtenerOCrearFichaVendedor(principal);

        Pedido pedido = new Pedido();
        pedido.setVehiculo(vehiculo);
        pedido.setConductor(vendedor);
        pedido.setEstado(normalizarEstadoPedido(estado));
        if (clienteCorreo != null && !clienteCorreo.isBlank()) {
            usuarioRepository.findByCorreo(clienteCorreo.trim().toLowerCase(Locale.ROOT))
                .ifPresent(pedido::setClienteUsuario);
        }
        pedido.setDescripcion(construirDescripcionCliente(clienteNombre, clienteTelefono, clienteCorreo, observacion));
        pedidoRepository.save(pedido);
        inventarioVentaService.sincronizarStockPorEstado(pedido, null);

        redirectAttributes.addFlashAttribute("success", "Venta/oportunidad registrada correctamente.");
        return "redirect:/vendedor/ventas";
    }

    @PostMapping("/ventas/financiamiento")
    public String simularFinanciamiento(@ModelAttribute("financiamientoForm") FinanciamientoSolicitudForm financiamientoForm,
                                        Model model,
                                        Principal principal,
                                        Authentication auth) {
        Conductor vendedor = obtenerOCrearFichaVendedor(principal);

        List<Pedido> misVentas = vendedor.getIdConductor() != null
                ? pedidoRepository.findByConductorIdConductor(vendedor.getIdConductor()).stream()
                .sorted(Comparator.comparing(Pedido::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList()
                : List.of();

        model.addAttribute("misVentas", misVentas);
        model.addAttribute("inventariosDisponibles", inventarioRepository.findByActivoTrue());
        model.addAttribute("vendedor", vendedor);

        if (financiamientoForm.getVehiculoId() == null) {
            cargarModuloFinanciamiento(model, vendedor, financiamientoForm, null, "Selecciona un vehículo para simular el crédito.");
            agregarDatosCorreo(model, auth);
            return "vendedor/clientes";
        }

        try {
            FinanciamientoResultadoDTO resultado = financiamientoService.registrarSimulacion(
                    financiamientoForm.getVehiculoId(),
                    financiamientoForm,
                    "VENDEDOR",
                    vendedor.getUsername(),
                    vendedor.getNombre(),
                    null);
            cargarModuloFinanciamiento(model, vendedor, financiamientoForm, resultado, null);
            model.addAttribute("mensajeFinanciamientoVendedor", "Simulación registrada para seguimiento comercial del vendedor.");
        } catch (IllegalArgumentException ex) {
            cargarModuloFinanciamiento(model, vendedor, financiamientoForm, null, ex.getMessage());
        }

        agregarDatosCorreo(model, auth);
        return "vendedor/clientes";
    }

    @PostMapping("/ventas/financiamiento/seguimiento")
    public String actualizarSeguimientoFinanciamiento(@ModelAttribute("seguimientoFinanciamientoForm") SeguimientoFinanciamientoForm seguimientoForm,
                                                      Principal principal,
                                                      RedirectAttributes redirectAttributes) {
        Conductor vendedor = obtenerOCrearFichaVendedor(principal);
        try {
            financiamientoService.actualizarSeguimiento(
                    seguimientoForm.getSolicitudId(),
                    seguimientoForm,
                    vendedor.getUsername(),
                    vendedor.getNombre());
            redirectAttributes.addFlashAttribute("mensajeFinanciamientoVendedor", "Seguimiento financiero actualizado para continuar el negocio.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorFinanciamientoVendedor", ex.getMessage());
            redirectAttributes.addFlashAttribute("seguimientoFinanciamientoForm", seguimientoForm);
        }
        return "redirect:/vendedor/ventas#financiamiento-seguimiento";
    }

    private void agregarDatosCorreo(Model model, Authentication auth) {
        if (auth == null) {
            return;
        }

        usuarioRepository.findByCorreo(auth.getName()).ifPresent(usuario -> {
            model.addAttribute("usuarioId", usuario.getIdUsuario());
            model.addAttribute("usuarioRol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");
            model.addAttribute("unreadCount", mensajeService.contarNoLeidos(usuario.getIdUsuario()));
        });
    }

    private Usuario obtenerUsuario(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
    }

    private Conductor obtenerOCrearFichaVendedor(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return new Conductor();
        }

        String username = principal.getName();
        return conductorRepository.findByUsername(username).orElseGet(() -> {
            Usuario usuario = usuarioRepository.findByCorreo(username).orElse(null);
            Conductor nuevo = new Conductor();
            nuevo.setUsername(username);
            nuevo.setNombre(usuario != null && usuario.getNombre() != null ? usuario.getNombre() : "Vendedor");
            nuevo.setLicencia("N/A");
            nuevo.setTelefono("SIN REGISTRO");
            return conductorRepository.save(nuevo);
        });
    }

    private String construirDescripcionCliente(String nombre, String telefono, String correo, String observacion) {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre != null && !nombre.isBlank() ? nombre.trim() : "Cliente sin nombre");

        if (telefono != null && !telefono.isBlank()) {
            sb.append(" | Tel: ").append(telefono.trim());
        }
        if (correo != null && !correo.isBlank()) {
            sb.append(" | Correo: ").append(correo.trim());
        }
        if (observacion != null && !observacion.isBlank()) {
            sb.append(" | Nota: ").append(observacion.trim());
        }

        return sb.toString();
    }

    private void cargarModuloFinanciamiento(Model model,
                                            Conductor vendedor,
                                            FinanciamientoSolicitudForm formulario,
                                            FinanciamientoResultadoDTO resultado,
                                            String error) {
        List<Vehiculo> vehiculosFinanciables = financiamientoService.listarVehiculosFinanciables();
        List<SolicitudFinanciamiento> simulaciones = financiamientoService.listarRecientesPorAsesor(vendedor.getUsername());
        ResumenFinanciamientoDTO resumen = financiamientoService.construirResumenPorAsesor(vendedor.getUsername());

        FinanciamientoSolicitudForm form = formulario;
        if (form == null) {
            form = new FinanciamientoSolicitudForm();
            if (!vehiculosFinanciables.isEmpty()) {
                Vehiculo vehiculoBase = vehiculosFinanciables.get(0);
                FinanciamientoResultadoDTO referencia = financiamientoService.calcularOfertaReferencial(vehiculoBase);
                form.setVehiculoId(vehiculoBase.getIdVehiculo());
                form.setCuotaInicial(referencia.getCuotaInicialMinima());
                form.setPlazoMeses(referencia.getPlazoMeses());
            }
        }

        if (!model.containsAttribute("financiamientoForm")) {
            model.addAttribute("financiamientoForm", form);
        }
        model.addAttribute("vehiculosFinanciables", vehiculosFinanciables);
        model.addAttribute("simulacionesFinanciamiento", simulaciones);
        model.addAttribute("resumenFinanciamientoVendedor", resumen);
        if (!model.containsAttribute("seguimientoFinanciamientoForm")) {
            model.addAttribute("seguimientoFinanciamientoForm", construirFormularioSeguimiento(simulaciones));
        }
        if (resultado != null) {
            model.addAttribute("resultadoFinanciamientoVendedor", resultado);
        }
        if (error != null && !error.isBlank()) {
            model.addAttribute("errorFinanciamientoVendedor", error);
        }
    }

    private SeguimientoFinanciamientoForm construirFormularioSeguimiento(List<SolicitudFinanciamiento> simulaciones) {
        SeguimientoFinanciamientoForm formulario = new SeguimientoFinanciamientoForm();
        if (simulaciones.isEmpty()) {
            formulario.setCrearNegocio(Boolean.FALSE);
            formulario.setEstadoDocumental("PENDIENTE");
            formulario.setEtapaProceso("RADICACION_PENDIENTE");
            return formulario;
        }

        SolicitudFinanciamiento base = simulaciones.get(0);
        formulario.setSolicitudId(base.getIdSolicitudFinanciamiento());
        formulario.setCrearNegocio(base.isNegocioCreado());
        formulario.setEstadoDocumental(base.getEstadoDocumental());
        formulario.setEtapaProceso(base.getEtapaProceso());
        formulario.setEntidadFinanciera(base.getEntidadFinanciera());
        formulario.setMontoDesembolsado(base.getMontoDesembolsado());
        formulario.setFechaDesembolsoProgramada(base.getFechaDesembolsoProgramada());
        return formulario;
    }

    private boolean contiene(String valor, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }
        if (valor == null) {
            return false;
        }
        return valor.toLowerCase(Locale.ROOT).contains(filtro.toLowerCase(Locale.ROOT));
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "pendiente";
        }

        String valor = estado.trim().toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace(' ', '_');

        return switch (valor) {
            case "en_revision", "revision" -> "en_revision";
            case "gestion_garantia", "garantia" -> "gestion_garantia";
            case "resuelto", "cerrado" -> "resuelto";
            default -> "pendiente";
        };
    }

    private String normalizarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            return "media";
        }

        String valor = prioridad.trim().toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");

        return switch (valor) {
            case "baja", "media", "alta", "critica" -> valor;
            default -> "media";
        };
    }

    private void completarDatosVehiculoNovedad(Novedad novedad) {
        if (novedad.getVehiculoChasis() == null || novedad.getVehiculoChasis().isBlank()) {
            novedad.setVehiculoReferencia("Sin vehiculo asociado");
            return;
        }

        inventarioRepository.findByChasis(novedad.getVehiculoChasis()).ifPresentOrElse(inventario -> {
            String marca = inventario.getMarca() != null ? inventario.getMarca() : "";
            String modelo = inventario.getModelo() != null ? inventario.getModelo() : "";
            String referencia = (marca + " " + modelo).trim();
            novedad.setVehiculoReferencia(referencia.isBlank() ? inventario.getChasis() : referencia);
        }, () -> novedad.setVehiculoReferencia(novedad.getVehiculoChasis()));
    }

    private String normalizarEstadoPedido(String estado) {
        if (estado == null || estado.isBlank()) {
            return "negociando";
        }

        String valor = estado.trim().toLowerCase(Locale.ROOT);
        return switch (valor) {
            case "pendiente", "contactado", "negociando", "vendido", "entregado", "perdido" -> valor;
            default -> "negociando";
        };
    }

    private boolean esEstadoVenta(String estado) {
        if (estado == null) {
            return false;
        }
        String valor = estado.trim().toLowerCase(Locale.ROOT);
        return "vendido".equals(valor) || "entregado".equals(valor);
    }

    @ModelAttribute("formatoFechaVendedor")
    public String formatoFechaVendedor() {
        return FORMATO_FECHA.format(LocalDateTime.now());
    }
}
