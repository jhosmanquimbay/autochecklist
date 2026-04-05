package com.example.demostracion.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.demostracion.dto.MercadoCompetenciaDTO;
import com.example.demostracion.dto.RadarPrecioDTO;
import com.example.demostracion.dto.RecallItemDTO;
import com.example.demostracion.dto.RecallResumenDTO;
import com.example.demostracion.dto.ResumenValoracionDTO;
import com.example.demostracion.dto.ValoracionVehiculoResumenDTO;
import com.example.demostracion.model.ContabilidadVenta;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.model.ValoracionVehiculo;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.ContabilidadVentaRepository;
import com.example.demostracion.repository.InventarioRepository;
import com.example.demostracion.repository.ValoracionVehiculoRepository;
import com.example.demostracion.repository.VehiculoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GerenteValoracionService {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final String RECALL_URL =
            "https://api.nhtsa.gov/recalls/recallsByVehicle?make={make}&model={model}&modelYear={year}";

    private final VehiculoRepository vehiculoRepository;
    private final InventarioRepository inventarioRepository;
    private final ValoracionVehiculoRepository valoracionVehiculoRepository;
    private final ContabilidadVentaRepository contabilidadVentaRepository;
    private final MercadoCompetenciaService mercadoCompetenciaService;
    private final RestTemplate restTemplate;

    public GerenteValoracionService(VehiculoRepository vehiculoRepository,
                                    InventarioRepository inventarioRepository,
                                    ValoracionVehiculoRepository valoracionVehiculoRepository,
                                    ContabilidadVentaRepository contabilidadVentaRepository,
                                    MercadoCompetenciaService mercadoCompetenciaService) {
        this.vehiculoRepository = vehiculoRepository;
        this.inventarioRepository = inventarioRepository;
        this.valoracionVehiculoRepository = valoracionVehiculoRepository;
        this.contabilidadVentaRepository = contabilidadVentaRepository;
        this.mercadoCompetenciaService = mercadoCompetenciaService;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public List<ValoracionVehiculoResumenDTO> listarVehiculos(String marca, String modelo, String estadoPublicacion) {
        List<Vehiculo> vehiculos = vehiculoRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(Vehiculo::isActivo)
                .toList();

        List<ContabilidadVenta> historicos = contabilidadVentaRepository.findAll();
        Map<Long, ValoracionVehiculo> valoraciones = valoracionVehiculoRepository.findAll().stream()
                .filter(v -> v.getVehiculo() != null && v.getVehiculo().getIdVehiculo() != null)
                .collect(Collectors.toMap(v -> v.getVehiculo().getIdVehiculo(), v -> v, (a, b) -> a, LinkedHashMap::new));

        String marcaFiltro = textoFiltro(marca);
        String modeloFiltro = textoFiltro(modelo);
        String estadoFiltro = estadoPublicacion == null ? "todos" : estadoPublicacion.trim().toLowerCase(LOCALE_ES);

        return vehiculos.stream()
                .filter(vehiculo -> marcaFiltro.isBlank() || contiene(vehiculo.getMarca(), marcaFiltro))
                .filter(vehiculo -> modeloFiltro.isBlank() || contiene(vehiculo.getModelo(), modeloFiltro))
                .filter(vehiculo -> "todos".equals(estadoFiltro) || estadoFiltro.equalsIgnoreCase(vehiculo.getEstadoPublicacion()))
                .map(vehiculo -> construirResumen(vehiculo, valoraciones.get(vehiculo.getIdVehiculo()), vehiculos, historicos))
                .sorted(Comparator.comparingInt(this::prioridadEstadoDocumental)
                        .thenComparing(ValoracionVehiculoResumenDTO::isListoParaPublicar)
                        .thenComparing(ValoracionVehiculoResumenDTO::getVehiculo, Comparator.nullsLast(String::compareToIgnoreCase))
                        .reversed())
                .toList();
    }

    public ResumenValoracionDTO construirResumen(List<ValoracionVehiculoResumenDTO> filas) {
        ResumenValoracionDTO resumen = new ResumenValoracionDTO();
        resumen.setTotalVehiculos(filas.size());
        resumen.setListosParaPublicar((int) filas.stream().filter(ValoracionVehiculoResumenDTO::isListoParaPublicar).count());
        resumen.setRiesgoDocumental((int) filas.stream().filter(fila -> "Riesgo alto".equalsIgnoreCase(fila.getEstadoDocumental())).count());
        resumen.setSubirPrecio((int) filas.stream().filter(fila -> "Subir".equalsIgnoreCase(fila.getAccionPrecio())).count());
        resumen.setBajarPrecio((int) filas.stream().filter(fila -> "Bajar".equalsIgnoreCase(fila.getAccionPrecio())).count());
        resumen.setSinPrecio((int) filas.stream().filter(fila -> fila.getPrecioActual() == null || fila.getPrecioActual() <= 0.0).count());
        return resumen;
    }

    public ValoracionVehiculo prepararRegistro(Long vehiculoId) {
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);
        ValoracionVehiculo valoracion = valoracionVehiculoRepository.findByVehiculoIdVehiculo(vehiculoId)
                .orElseGet(ValoracionVehiculo::new);

        valoracion.setVehiculo(vehiculo);
        valoracion.setInventario(resolverInventario(vehiculo));
        return valoracion;
    }

    @Transactional
    public ValoracionVehiculo guardarRegistro(Long vehiculoId, ValoracionVehiculo formulario) {
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);
        ValoracionVehiculo registro = valoracionVehiculoRepository.findByVehiculoIdVehiculo(vehiculoId)
                .orElseGet(ValoracionVehiculo::new);

        validarFechas(formulario.getSoatVencimiento(), formulario.getTecnicomecanicaVencimiento());

        registro.setVehiculo(vehiculo);
        registro.setInventario(resolverInventario(vehiculo));
        registro.setSoatVencimiento(formulario.getSoatVencimiento());
        registro.setTecnicomecanicaVencimiento(formulario.getTecnicomecanicaVencimiento());
        registro.setTarjetaPropiedadOk(formulario.isTarjetaPropiedadOk());
        registro.setImpuestosAlDia(formulario.isImpuestosAlDia());
        registro.setPrendaActiva(formulario.isPrendaActiva());
        registro.setPrecioObjetivoManual(validarMonto(formulario.getPrecioObjetivoManual()));
        registro.setObservaciones(normalizarTexto(formulario.getObservaciones()));
        registro.setFechaActualizacion(LocalDateTime.now());

        return valoracionVehiculoRepository.save(registro);
    }

    public RadarPrecioDTO construirRadar(Long vehiculoId) {
        return construirRadar(vehiculoId, consultarMercadoCompetencia(vehiculoId));
    }

    public RadarPrecioDTO construirRadar(Long vehiculoId, MercadoCompetenciaDTO mercadoCompetencia) {
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);
        RadarPrecioDTO radar = construirRadar(vehiculo, vehiculoRepository.findAll(), contabilidadVentaRepository.findAll());
        incorporarMercadoExterno(radar, vehiculo, mercadoCompetencia);
        return radar;
    }

    public MercadoCompetenciaDTO consultarMercadoCompetencia(Long vehiculoId) {
        return mercadoCompetenciaService.consultarMercado(obtenerVehiculo(vehiculoId));
    }

    public RecallResumenDTO consultarAlertasTecnicas(Long vehiculoId) {
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);

        if (vehiculo.getMarca() == null || vehiculo.getModelo() == null || vehiculo.getAnio() == null) {
            return new RecallResumenDTO(false, "Faltan marca, modelo o año para consultar alertas técnicas.", 0, "Sin datos", List.of());
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    RECALL_URL,
                    Map.class,
                    vehiculo.getMarca().trim(),
                    vehiculo.getModelo().trim(),
                    vehiculo.getAnio());

            if (response == null || !(response.get("results") instanceof List<?> rawResults)) {
                return new RecallResumenDTO(false, "La API externa no devolvió campañas utilizables.", 0, "Sin datos", List.of());
            }

            List<RecallItemDTO> items = rawResults.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(this::mapearRecall)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(RecallItemDTO::campania, item -> item, (a, b) -> a, LinkedHashMap::new))
                    .values().stream()
                    .limit(4)
                    .toList();

            int totalCampanias = ((Number) response.getOrDefault("Count", items.size())).intValue();
            String riesgo = calcularNivelRiesgoRecall(totalCampanias, items);
            String mensaje = totalCampanias == 0
                    ? "Sin campañas críticas reportadas para esta combinación de marca, modelo y año."
                    : "Se detectaron campañas técnicas que conviene revisar antes de publicar o entregar.";

            return new RecallResumenDTO(true, mensaje, totalCampanias, riesgo, items);
        } catch (RestClientException ex) {
            log.error("Error consultando recalls para vehículo {}", vehiculoId, ex);
            return new RecallResumenDTO(false, "No fue posible consultar la API de alertas técnicas en este momento.", 0, "Sin datos", List.of());
        }
    }

    public String describirEstadoDocumental(ValoracionVehiculo valoracion) {
        return evaluarEstadoDocumental(valoracion).estado();
    }

    public String claseEstadoDocumental(ValoracionVehiculo valoracion) {
        return evaluarEstadoDocumental(valoracion).claseCss();
    }

    public boolean listoParaPublicar(Vehiculo vehiculo, ValoracionVehiculo valoracion) {
        EstadoDocumental estado = evaluarEstadoDocumental(valoracion);
        return "Listo".equalsIgnoreCase(estado.estado()) && vehiculo.getPrecio() != null && vehiculo.getPrecio() > 0.0;
    }

    private ValoracionVehiculoResumenDTO construirResumen(Vehiculo vehiculo,
                                                          ValoracionVehiculo valoracion,
                                                          List<Vehiculo> todosVehiculos,
                                                          List<ContabilidadVenta> historicos) {
        RadarPrecioDTO radar = construirRadar(vehiculo, todosVehiculos, historicos);
        ValoracionVehiculoResumenDTO fila = new ValoracionVehiculoResumenDTO();
        fila.setVehiculoId(vehiculo.getIdVehiculo());
        fila.setInventarioId(valoracion != null && valoracion.getInventario() != null ? valoracion.getInventario().getIdInventario() : null);
        fila.setChasis(vehiculo.getChasis());
        fila.setVehiculo(nombreVehiculo(vehiculo));
        fila.setEstadoPublicacion(vehiculo.getEstadoPublicacion());
        fila.setEstadoPublicacionEtiqueta(vehiculo.getEstadoPublicacionEtiqueta());
        fila.setPrecioActual(vehiculo.getPrecio());
        fila.setPrecioObjetivoManual(valoracion != null ? valoracion.getPrecioObjetivoManual() : null);
        fila.setPrecioSugerido(radar.getPrecioSugerido());
        fila.setAccionPrecio(radar.getAccionSugerida());
        fila.setTotalComparables(radar.getComparablesActivos() + radar.getComparablesHistoricos());
        fila.setSoatVencimiento(valoracion != null ? valoracion.getSoatVencimiento() : null);
        fila.setTecnicomecanicaVencimiento(valoracion != null ? valoracion.getTecnicomecanicaVencimiento() : null);
        fila.setEstadoDocumental(describirEstadoDocumental(valoracion));
        fila.setEstadoDocumentalClase(claseEstadoDocumental(valoracion));
        fila.setListoParaPublicar(listoParaPublicar(vehiculo, valoracion));
        return fila;
    }

    private RadarPrecioDTO construirRadar(Vehiculo objetivo, List<Vehiculo> vehiculos, List<ContabilidadVenta> historicos) {
        List<Vehiculo> comparablesActivos = vehiculos.stream()
                .filter(Vehiculo::isActivo)
                .filter(v -> !Objects.equals(v.getIdVehiculo(), objetivo.getIdVehiculo()))
                .filter(v -> v.getPrecio() != null && v.getPrecio() > 0.0)
                .filter(v -> esComparable(objetivo, v))
                .toList();

        if (comparablesActivos.isEmpty()) {
            comparablesActivos = vehiculos.stream()
                    .filter(Vehiculo::isActivo)
                    .filter(v -> !Objects.equals(v.getIdVehiculo(), objetivo.getIdVehiculo()))
                    .filter(v -> v.getPrecio() != null && v.getPrecio() > 0.0)
                    .filter(v -> coincideMarcaModelo(objetivo, v))
                    .toList();
        }

        List<ContabilidadVenta> comparablesHistoricos = historicos.stream()
                .filter(h -> h.getVehiculo() != null && h.getPrecioVentaFinal() != null && h.getPrecioVentaFinal() > 0.0)
                .filter(h -> esComparable(objetivo, h.getVehiculo()))
                .toList();

        if (comparablesHistoricos.isEmpty()) {
            comparablesHistoricos = historicos.stream()
                    .filter(h -> h.getVehiculo() != null && h.getPrecioVentaFinal() != null && h.getPrecioVentaFinal() > 0.0)
                    .filter(h -> coincideMarcaModelo(objetivo, h.getVehiculo()))
                    .toList();
        }

        double promedioActivo = comparablesActivos.stream().mapToDouble(v -> valor(v.getPrecio())).average().orElse(0.0);
        double promedioHistorico = comparablesHistoricos.stream().mapToDouble(v -> valor(v.getPrecioVentaFinal())).average().orElse(0.0);

        double precioSugerido;
        if (promedioActivo > 0.0 && promedioHistorico > 0.0) {
            precioSugerido = promedioHistorico * 0.55 + promedioActivo * 0.45;
        } else if (promedioHistorico > 0.0) {
            precioSugerido = promedioHistorico;
        } else if (promedioActivo > 0.0) {
            precioSugerido = promedioActivo;
        } else {
            precioSugerido = valor(objetivo.getPrecio());
        }

        RadarPrecioDTO radar = new RadarPrecioDTO();
        radar.setComparablesActivos(comparablesActivos.size());
        radar.setComparablesHistoricos(comparablesHistoricos.size());
        radar.setPrecioPromedioActivo(promedioActivo);
        radar.setPrecioPromedioHistorico(promedioHistorico);
        radar.setComparablesMercado(0);
        radar.setPrecioPromedioMercado(0.0);
        radar.setMercadoIntegrado(false);
        radar.setFuentesMercado("Sin fuente externa");
        radar.setOrigenAnalisis("Interno");
        actualizarIndicadoresRadar(radar, objetivo, precioSugerido, comparablesActivos.size() + comparablesHistoricos.size());
        return radar;
    }

    private void incorporarMercadoExterno(RadarPrecioDTO radar, Vehiculo objetivo, MercadoCompetenciaDTO mercadoCompetencia) {
        if (radar == null || mercadoCompetencia == null) {
            return;
        }

        radar.setComparablesMercado(mercadoCompetencia.getTotalMuestras());
        radar.setPrecioPromedioMercado(mercadoCompetencia.getPrecioPromedio());
        radar.setFuentesMercado(Optional.ofNullable(mercadoCompetencia.getFuentes()).filter(valor -> !valor.isBlank()).orElse("Sin fuente externa"));

        if (!mercadoCompetencia.isDisponible() || mercadoCompetencia.getPrecioPromedio() <= 0.0 || mercadoCompetencia.getTotalMuestras() < 2) {
            radar.setMercadoIntegrado(false);
            radar.setOrigenAnalisis("Interno");
            return;
        }

        double pesoMercado = mercadoCompetencia.getTotalMuestras() >= 4 ? 0.60 : 0.45;
        double precioBase = radar.getPrecioSugerido();
        double precioSugerido = precioBase > 0.0
                ? mercadoCompetencia.getPrecioPromedio() * pesoMercado + precioBase * (1.0 - pesoMercado)
                : mercadoCompetencia.getPrecioPromedio();

        int totalComparables = radar.getComparablesActivos()
                + radar.getComparablesHistoricos()
                + mercadoCompetencia.getTotalMuestras();

        radar.setMercadoIntegrado(true);
        radar.setOrigenAnalisis("Mixto");
        actualizarIndicadoresRadar(radar, objetivo, precioSugerido, totalComparables);
    }

    private void actualizarIndicadoresRadar(RadarPrecioDTO radar, Vehiculo objetivo, double precioSugerido, int totalComparables) {
        String confianza = calcularConfianza(totalComparables);
        double amplitud = switch (confianza) {
            case "Alta" -> 0.045;
            case "Media" -> 0.075;
            default -> 0.10;
        };

        double rangoMinimo = precioSugerido <= 0.0 ? 0.0 : precioSugerido * (1.0 - amplitud);
        double rangoMaximo = precioSugerido <= 0.0 ? 0.0 : precioSugerido * (1.0 + amplitud);
        double precioActual = valor(objetivo.getPrecio());
        double desviacion = precioSugerido <= 0.0 ? 0.0 : ((precioActual - precioSugerido) * 100.0) / precioSugerido;

        String accion;
        if (precioActual <= 0.0) {
            accion = "Completar precio";
        } else if (precioActual < rangoMinimo) {
            accion = "Subir";
        } else if (precioActual > rangoMaximo) {
            accion = "Bajar";
        } else {
            accion = "Mantener";
        }

        radar.setPrecioSugerido(precioSugerido);
        radar.setRangoMinimo(rangoMinimo);
        radar.setRangoMaximo(rangoMaximo);
        radar.setConfianza(confianza);
        radar.setAccionSugerida(accion);
        radar.setDesviacionPorcentual(desviacion);
    }

    private boolean esComparable(Vehiculo objetivo, Vehiculo candidato) {
        if (!coincideMarcaModelo(objetivo, candidato)) {
            return false;
        }
        if (objetivo.getAnio() == null || candidato.getAnio() == null) {
            return true;
        }
        return Math.abs(objetivo.getAnio() - candidato.getAnio()) <= 1;
    }

    private boolean coincideMarcaModelo(Vehiculo objetivo, Vehiculo candidato) {
        return textoSeguro(objetivo.getMarca()).equals(textoSeguro(candidato.getMarca()))
                && textoSeguro(objetivo.getModelo()).equals(textoSeguro(candidato.getModelo()));
    }

    private String calcularConfianza(int totalComparables) {
        if (totalComparables >= 6) {
            return "Alta";
        }
        if (totalComparables >= 3) {
            return "Media";
        }
        return "Baja";
    }

    private RecallItemDTO mapearRecall(Map<?, ?> raw) {
        String campania = textoPlano(raw.get("NHTSACampaignNumber"));
        if (campania.isBlank()) {
            return null;
        }
        return new RecallItemDTO(
                campania,
                textoPlano(raw.get("Component")),
                textoPlano(raw.get("ReportReceivedDate")),
                resumirTexto(textoPlano(raw.get("Consequence")), 160),
                resumirTexto(textoPlano(raw.get("Remedy")), 160));
    }

    private String calcularNivelRiesgoRecall(int totalCampanias, List<RecallItemDTO> items) {
        String texto = items.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.componente(), item.consecuencia()))
                .collect(Collectors.joining(" "))
                .toLowerCase(LOCALE_ES);

        if (texto.contains("air bag") || texto.contains("brake") || texto.contains("steering") || texto.contains("death")) {
            return "Alto";
        }
        if (totalCampanias > 0) {
            return "Medio";
        }
        return "Bajo";
    }

    private EstadoDocumental evaluarEstadoDocumental(ValoracionVehiculo valoracion) {
        if (valoracion == null) {
            return new EstadoDocumental("Sin validar", "bg-neutral-soft");
        }

        LocalDate hoy = LocalDate.now();
        boolean soatVencido = valoracion.getSoatVencimiento() == null || valoracion.getSoatVencimiento().isBefore(hoy);
        boolean tecnoVencida = valoracion.getTecnicomecanicaVencimiento() == null || valoracion.getTecnicomecanicaVencimiento().isBefore(hoy);
        boolean porVencer = estaPorVencer(valoracion.getSoatVencimiento(), hoy) || estaPorVencer(valoracion.getTecnicomecanicaVencimiento(), hoy);

        if (valoracion.isPrendaActiva() || soatVencido || tecnoVencida || !valoracion.isTarjetaPropiedadOk() || !valoracion.isImpuestosAlDia()) {
            return new EstadoDocumental("Riesgo alto", "bg-danger");
        }
        if (porVencer) {
            return new EstadoDocumental("Por vencer", "bg-warning-soft");
        }
        return new EstadoDocumental("Listo", "bg-success");
    }

    private boolean estaPorVencer(LocalDate fecha, LocalDate hoy) {
        return fecha != null && !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(30));
    }

    private int prioridadEstadoDocumental(ValoracionVehiculoResumenDTO fila) {
        return switch (fila.getEstadoDocumental()) {
            case "Riesgo alto" -> 3;
            case "Por vencer" -> 2;
            case "Sin validar" -> 1;
            default -> 0;
        };
    }

    private Vehiculo obtenerVehiculo(Long vehiculoId) {
        return vehiculoRepository.findById(Objects.requireNonNull(vehiculoId, "El vehículo es obligatorio."))
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));
    }

    private Inventario resolverInventario(Vehiculo vehiculo) {
        if (vehiculo == null || vehiculo.getChasis() == null || vehiculo.getChasis().isBlank()) {
            return null;
        }
        return inventarioRepository.findByChasis(vehiculo.getChasis()).orElse(null);
    }

    private void validarFechas(LocalDate soat, LocalDate tecnicomecanica) {
        LocalDate limiteInferior = LocalDate.of(2000, 1, 1);
        if (soat != null && soat.isBefore(limiteInferior)) {
            throw new IllegalArgumentException("La fecha de SOAT no es válida.");
        }
        if (tecnicomecanica != null && tecnicomecanica.isBefore(limiteInferior)) {
            throw new IllegalArgumentException("La fecha de técnico-mecánica no es válida.");
        }
    }

    private Double validarMonto(Double valor) {
        if (valor == null) {
            return null;
        }
        if (valor < 0.0) {
            throw new IllegalArgumentException("El precio objetivo manual no puede ser negativo.");
        }
        return valor;
    }

    private String nombreVehiculo(Vehiculo vehiculo) {
        return List.of(vehiculo.getMarca(), vehiculo.getModelo(), vehiculo.getAnio() != null ? String.valueOf(vehiculo.getAnio()) : null)
                .stream()
                .filter(Objects::nonNull)
                .filter(valor -> !valor.isBlank())
                .collect(Collectors.joining(" "));
    }

    private boolean contiene(String valor, String filtro) {
        return textoSeguro(valor).contains(textoSeguro(filtro));
    }

    private String textoFiltro(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(LOCALE_ES);
    }

    private String textoSeguro(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(LOCALE_ES);
    }

    private double valor(Double numero) {
        return numero == null ? 0.0 : numero;
    }

    private String textoPlano(Object valor) {
        return valor == null ? "" : String.valueOf(valor).trim();
    }

    private String resumirTexto(String valor, int limite) {
        if (valor == null || valor.isBlank()) {
            return "Sin detalle";
        }
        String limpio = valor.replaceAll("\\s+", " ").trim();
        if (limpio.length() <= limite) {
            return limpio;
        }
        return limpio.substring(0, limite - 3) + "...";
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private record EstadoDocumental(String estado, String claseCss) {
    }
}