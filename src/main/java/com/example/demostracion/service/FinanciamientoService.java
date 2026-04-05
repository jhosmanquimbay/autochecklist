package com.example.demostracion.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demostracion.dto.FinanciamientoResultadoDTO;
import com.example.demostracion.dto.FinanciamientoSolicitudForm;
import com.example.demostracion.dto.ResumenFinanciamientoDTO;
import com.example.demostracion.dto.SeguimientoFinanciamientoForm;
import com.example.demostracion.model.SolicitudFinanciamiento;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.SolicitudFinanciamientoRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@Service
public class FinanciamientoService {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_BITACORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_ES);
    private static final double INGRESO_MINIMO_REFERENCIA = 1_500_000.0;
    private static final double CARGO_ADMINISTRATIVO_MENSUAL = 29_000.0;
    private static final Set<String> ESTADOS_DOCUMENTALES_VALIDOS = Set.of("PENDIENTE", "INCOMPLETO", "VALIDANDO", "COMPLETO", "OBSERVADO");
    private static final Set<String> ETAPAS_PROCESO_VALIDAS = Set.of("RADICACION_PENDIENTE", "EN_ANALISIS_ENTIDAD", "APROBADO_ENTIDAD", "DESEMBOLSO_PROGRAMADO", "DESEMBOLSADO", "DESCARTADO");
    private static final Set<String> ETAPAS_CON_NEGOCIO = Set.of("APROBADO_ENTIDAD", "DESEMBOLSO_PROGRAMADO", "DESEMBOLSADO");
    private static final Set<String> ETAPAS_PENDIENTES_DESEMBOLSO = Set.of("APROBADO_ENTIDAD", "DESEMBOLSO_PROGRAMADO");

    private final VehiculoRepository vehiculoRepository;
    private final SolicitudFinanciamientoRepository solicitudFinanciamientoRepository;
    private final UsuarioRepository usuarioRepository;

    public FinanciamientoService(VehiculoRepository vehiculoRepository,
                                 SolicitudFinanciamientoRepository solicitudFinanciamientoRepository,
                                 UsuarioRepository usuarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.solicitudFinanciamientoRepository = solicitudFinanciamientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Vehiculo> listarVehiculosFinanciables() {
        return vehiculoRepository.findAllByOrderByFechaCreacionDesc().stream()
                .filter(Vehiculo::isActivo)
                .filter(vehiculo -> valor(vehiculo.getPrecio()) > 0.0)
                .sorted(Comparator.comparing(Vehiculo::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public FinanciamientoResultadoDTO calcularOfertaReferencial(Long vehiculoId) {
        return calcularOfertaReferencial(obtenerVehiculo(vehiculoId));
    }

    public FinanciamientoResultadoDTO calcularOfertaReferencial(Vehiculo vehiculo) {
        double precio = valor(vehiculo.getPrecio());
        double porcentajeMinimo = cuotaInicialMinimaPorcentaje(calcularEdadVehiculo(vehiculo));
        int plazoSugerido = plazosDisponibles(vehiculo).stream().max(Integer::compareTo).orElse(48);

        FinanciamientoSolicitudForm form = new FinanciamientoSolicitudForm();
        form.setVehiculoId(vehiculo.getIdVehiculo());
        form.setCuotaInicial(precio * Math.max(porcentajeMinimo, 30.0) / 100.0);
        form.setPlazoMeses(plazoSugerido);
        form.setIngresoMensual(precio);
        form.setOtrasObligaciones(0.0);
        form.setCanalOrigen("REFERENCIAL");

        FinanciamientoResultadoDTO resultado = construirResultado(vehiculo, form, "REFERENCIAL");
        resultado.setEstadoAnalisis("REFERENCIAL");
        resultado.setAprobadoPreliminar(false);
        resultado.setMensajeDecision("Cuota estimada con una entrada sugerida y el plazo más amplio permitido para este vehículo.");
        resultado.setDocumentosRequeridos(List.of(
                "Cédula del solicitante",
                "Soporte de ingresos de los últimos 3 meses",
                "Soporte de la cuota inicial"));
        return resultado;
    }

    public FinanciamientoResultadoDTO simular(Vehiculo vehiculo,
                                              FinanciamientoSolicitudForm form,
                                              String canalOrigen) {
        validarFormulario(form, true);
        return construirResultado(vehiculo, form, normalizarCanal(canalOrigen));
    }

    @Transactional
    public FinanciamientoResultadoDTO registrarSimulacion(Long vehiculoId,
                                                          FinanciamientoSolicitudForm form,
                                                          String canalOrigen,
                                                          String asesorCorreo,
                                                          String asesorNombre,
                                                          String correoUsuarioAutenticado) {
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);
        validarFormulario(form, true);

        FinanciamientoResultadoDTO resultado = construirResultado(vehiculo, form, normalizarCanal(canalOrigen));
        SolicitudFinanciamiento solicitud = new SolicitudFinanciamiento();
    LocalDateTime ahora = LocalDateTime.now();
        solicitud.setVehiculo(vehiculo);
        solicitud.setClienteUsuario(resolverUsuario(correoUsuarioAutenticado));
        solicitud.setCanalOrigen(resultado.getCanalOrigen());
        solicitud.setSolicitanteNombre(normalizarTexto(form.getNombreCompleto()));
        solicitud.setSolicitanteCorreo(normalizarCorreo(form.getCorreo()));
        solicitud.setSolicitanteTelefono(normalizarTexto(form.getTelefono()));
        solicitud.setAsesorCorreo(normalizarTexto(asesorCorreo));
        solicitud.setAsesorNombre(normalizarTexto(asesorNombre));
        solicitud.setPrecioVehiculoSnapshot(resultado.getPrecioVehiculo());
        solicitud.setCuotaInicial(resultado.getCuotaInicial());
        solicitud.setPorcentajeCuotaInicial(resultado.getPorcentajeCuotaInicial());
        solicitud.setPlazoMeses(resultado.getPlazoMeses());
        solicitud.setIngresoMensual(valor(form.getIngresoMensual()));
        solicitud.setOtrasObligaciones(valor(form.getOtrasObligaciones()));
        solicitud.setMontoFinanciar(resultado.getMontoFinanciar());
        solicitud.setMontoMaximoSugerido(resultado.getMontoMaximoSugerido());
        solicitud.setTasaEfectivaAnual(resultado.getTasaEfectivaAnual());
        solicitud.setTasaMesVencida(resultado.getTasaMesVencida());
        solicitud.setCuotaCapitalInteres(resultado.getCuotaCapitalInteres());
        solicitud.setSegurosMensuales(resultado.getSegurosMensuales());
        solicitud.setCargosMensuales(resultado.getCargosMensuales());
        solicitud.setCuotaMensualTotal(resultado.getCuotaMensualTotal());
        solicitud.setCapacidadPagoDisponible(resultado.getCapacidadPagoDisponible());
        solicitud.setRelacionCuotaIngreso(resultado.getRelacionCuotaIngreso());
        solicitud.setRelacionEndeudamientoTotal(resultado.getRelacionEndeudamientoTotal());
        solicitud.setAprobadoPreliminar(resultado.isAprobadoPreliminar());
        solicitud.setEstadoAnalisis(resultado.getEstadoAnalisis());
        solicitud.setEstadoDocumental(definirEstadoDocumentalInicial(resultado.getEstadoAnalisis()));
        solicitud.setEtapaProceso(definirEtapaInicial(resultado.getEstadoAnalisis()));
        solicitud.setNegocioCreado(false);
        solicitud.setMontoDesembolsado(0.0);
        solicitud.setFechaUltimaGestion(ahora);
        solicitud.setObservacionesSeguimiento("" + FORMATO_BITACORA.format(ahora) + " - Simulación creada desde " + resultado.getCanalOrigen().replace('_', ' ').toLowerCase(LOCALE_ES) + ".");
        solicitud.setMensajeDecision(resultado.getMensajeDecision());
        solicitud.setObservaciones(normalizarTexto(form.getObservaciones()));
        solicitudFinanciamientoRepository.save(solicitud);
        return resultado;
    }

    public ResumenFinanciamientoDTO construirResumenGeneral() {
        return construirResumen(solicitudFinanciamientoRepository.findAll());
    }

    public ResumenFinanciamientoDTO construirResumenPorAsesor(String asesorCorreo) {
        if (asesorCorreo == null || asesorCorreo.isBlank()) {
            return new ResumenFinanciamientoDTO();
        }
        return construirResumen(solicitudFinanciamientoRepository.findByAsesorCorreoIgnoreCase(asesorCorreo));
    }

    public List<SolicitudFinanciamiento> listarRecientes() {
        return solicitudFinanciamientoRepository.findTop20ByOrderByFechaUltimaGestionDescFechaSimulacionDesc();
    }

    public List<SolicitudFinanciamiento> listarRecientesPorAsesor(String asesorCorreo) {
        if (asesorCorreo == null || asesorCorreo.isBlank()) {
            return List.of();
        }
        return solicitudFinanciamientoRepository.findTop20ByAsesorCorreoIgnoreCaseOrderByFechaUltimaGestionDescFechaSimulacionDesc(asesorCorreo);
    }

    public List<SolicitudFinanciamiento> listarSeguimientoOperativo() {
        return solicitudFinanciamientoRepository.findAll().stream()
                .filter(solicitud -> solicitud.isNegocioCreado()
                        || ETAPAS_PENDIENTES_DESEMBOLSO.contains(normalizarEtapaProceso(solicitud.getEtapaProceso(), solicitud.getEtapaProceso()))
                        || "DESEMBOLSADO".equals(normalizarEtapaProceso(solicitud.getEtapaProceso(), solicitud.getEtapaProceso()))
                        || "EN_ANALISIS_ENTIDAD".equals(normalizarEtapaProceso(solicitud.getEtapaProceso(), solicitud.getEtapaProceso())))
                .sorted(comparadorSeguimiento())
                .limit(20)
                .toList();
    }

    @Transactional
    public SolicitudFinanciamiento actualizarSeguimiento(Long solicitudId,
                                                         SeguimientoFinanciamientoForm form,
                                                         String gestorCorreo,
                                                         String gestorNombre) {
        validarSeguimiento(form);
        Long solicitudSegura = Objects.requireNonNull(solicitudId != null ? solicitudId : form.getSolicitudId(), "Selecciona la solicitud a gestionar.");
        SolicitudFinanciamiento solicitud = solicitudFinanciamientoRepository.findById(solicitudSegura)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud de financiamiento ya no existe."));

        String estadoDocumental = normalizarEstadoDocumental(form.getEstadoDocumental(), solicitud.getEstadoDocumental());
        String etapaProceso = normalizarEtapaProceso(form.getEtapaProceso(), solicitud.getEtapaProceso());
        boolean negocioCreado = solicitud.isNegocioCreado()
                || Boolean.TRUE.equals(form.getCrearNegocio())
                || ETAPAS_CON_NEGOCIO.contains(etapaProceso);

        if (ETAPAS_CON_NEGOCIO.contains(etapaProceso) && !"COMPLETO".equals(estadoDocumental)) {
            estadoDocumental = "COMPLETO";
        }

        if ("DESEMBOLSO_PROGRAMADO".equals(etapaProceso)
                && form.getFechaDesembolsoProgramada() == null
                && solicitud.getFechaDesembolsoProgramada() == null) {
            throw new IllegalArgumentException("Define la fecha programada de desembolso para continuar el seguimiento.");
        }

        if (form.getFechaDesembolsoProgramada() != null) {
            solicitud.setFechaDesembolsoProgramada(form.getFechaDesembolsoProgramada());
        }

        if ("DESEMBOLSADO".equals(etapaProceso)) {
            double montoDesembolsado = valor(form.getMontoDesembolsado());
            if (montoDesembolsado <= 0.0) {
                montoDesembolsado = valor(solicitud.getMontoFinanciar());
            }
            solicitud.setMontoDesembolsado(montoDesembolsado);
            solicitud.setFechaDesembolsoReal(LocalDate.now());
            if (solicitud.getFechaDesembolsoProgramada() == null) {
                solicitud.setFechaDesembolsoProgramada(LocalDate.now());
            }
        } else if (form.getMontoDesembolsado() != null) {
            solicitud.setMontoDesembolsado(Math.max(valor(form.getMontoDesembolsado()), 0.0));
        }

        if ("DESCARTADO".equals(etapaProceso) && !solicitud.isNegocioCreado()) {
            solicitud.setMontoDesembolsado(0.0);
            solicitud.setFechaDesembolsoProgramada(null);
            solicitud.setFechaDesembolsoReal(null);
        }

        if (negocioCreado && solicitud.getFechaNegocioCreado() == null) {
            solicitud.setFechaNegocioCreado(LocalDateTime.now());
        }

        String entidadFinanciera = normalizarTexto(form.getEntidadFinanciera());
        if (!entidadFinanciera.isBlank()) {
            solicitud.setEntidadFinanciera(entidadFinanciera);
        }

        solicitud.setNegocioCreado(negocioCreado);
        solicitud.setEstadoDocumental(estadoDocumental);
        solicitud.setEtapaProceso(etapaProceso);
        solicitud.setFechaUltimaGestion(LocalDateTime.now());
        solicitud.setObservacionesSeguimiento(anexarBitacora(
                solicitud.getObservacionesSeguimiento(),
                construirBitacora(gestorCorreo, gestorNombre, form, estadoDocumental, etapaProceso, negocioCreado)));
        return solicitudFinanciamientoRepository.save(solicitud);
    }

    private ResumenFinanciamientoDTO construirResumen(List<SolicitudFinanciamiento> solicitudes) {
        ResumenFinanciamientoDTO resumen = new ResumenFinanciamientoDTO();
        resumen.setTotalSolicitudes(solicitudes.size());
        resumen.setPreaprobadas(solicitudes.stream().filter(s -> "PREAPROBADO".equalsIgnoreCase(s.getEstadoAnalisis())).count());
        resumen.setEnEstudio(solicitudes.stream().filter(s -> "EN_ESTUDIO".equalsIgnoreCase(s.getEstadoAnalisis())).count());
        resumen.setNoViables(solicitudes.stream().filter(s -> "NO_VIABLE".equalsIgnoreCase(s.getEstadoAnalisis())).count());
        resumen.setSolicitudesWeb(solicitudes.stream().filter(s -> "CLIENTE_WEB".equalsIgnoreCase(s.getCanalOrigen())).count());
        resumen.setSolicitudesVendedor(solicitudes.stream().filter(s -> "VENDEDOR".equalsIgnoreCase(s.getCanalOrigen())).count());
        resumen.setSolicitudesGerencia(solicitudes.stream().filter(s -> "GERENTE".equalsIgnoreCase(s.getCanalOrigen())).count());
        resumen.setNegociosOriginados(solicitudes.stream().filter(SolicitudFinanciamiento::isNegocioCreado).count());
        resumen.setPendientesDocumentos(solicitudes.stream().filter(this::esPendienteDocumental).count());
        resumen.setDocumentacionCompleta(solicitudes.stream().filter(s -> "COMPLETO".equals(normalizarEstadoDocumental(s.getEstadoDocumental(), s.getEstadoDocumental()))).count());
        resumen.setListasDesembolso(solicitudes.stream().filter(s -> ETAPAS_PENDIENTES_DESEMBOLSO.contains(normalizarEtapaProceso(s.getEtapaProceso(), s.getEtapaProceso()))).count());
        resumen.setDesembolsadas(solicitudes.stream().filter(s -> "DESEMBOLSADO".equals(normalizarEtapaProceso(s.getEtapaProceso(), s.getEtapaProceso()))).count());
        resumen.setMontoPotencialFinanciado(solicitudes.stream().mapToDouble(s -> valor(s.getMontoFinanciar())).sum());
        resumen.setMontoDesembolsado(solicitudes.stream()
            .filter(s -> "DESEMBOLSADO".equals(normalizarEtapaProceso(s.getEtapaProceso(), s.getEtapaProceso())))
            .mapToDouble(s -> valor(s.getMontoDesembolsado()))
            .sum());
        resumen.setMontoPendienteDesembolso(solicitudes.stream()
            .filter(s -> ETAPAS_PENDIENTES_DESEMBOLSO.contains(normalizarEtapaProceso(s.getEtapaProceso(), s.getEtapaProceso())))
            .mapToDouble(s -> valor(s.getMontoFinanciar()))
            .sum());
        resumen.setTicketPromedioFinanciado(solicitudes.isEmpty() ? 0.0 : solicitudes.stream().mapToDouble(s -> valor(s.getMontoFinanciar())).average().orElse(0.0));
        resumen.setCuotaPromedio(solicitudes.isEmpty() ? 0.0 : solicitudes.stream().mapToDouble(s -> valor(s.getCuotaMensualTotal())).average().orElse(0.0));
        resumen.setPorcentajeAprobacion(solicitudes.isEmpty() ? 0.0 : (resumen.getPreaprobadas() * 100.0) / solicitudes.size());
        resumen.setPorcentajeConversionNegocio(solicitudes.isEmpty() ? 0.0 : (resumen.getNegociosOriginados() * 100.0) / solicitudes.size());
        return resumen;
    }

    private FinanciamientoResultadoDTO construirResultado(Vehiculo vehiculo,
                                                          FinanciamientoSolicitudForm form,
                                                          String canalOrigen) {
        int edadVehiculo = calcularEdadVehiculo(vehiculo);
        List<Integer> plazosDisponibles = plazosDisponibles(vehiculo);
        double precioVehiculo = valor(vehiculo.getPrecio());
        double porcentajeCuotaInicialMinima = cuotaInicialMinimaPorcentaje(edadVehiculo);
        double cuotaInicialMinima = precioVehiculo * porcentajeCuotaInicialMinima / 100.0;
        int plazoMeses = resolverPlazo(form.getPlazoMeses(), plazosDisponibles);
        double cuotaInicial = Math.max(valor(form.getCuotaInicial()), 0.0);
        double porcentajeCuotaInicial = precioVehiculo > 0.0 ? (cuotaInicial * 100.0) / precioVehiculo : 0.0;
        double montoFinanciar = Math.max(precioVehiculo - cuotaInicial, 0.0);
        double tasaEfectivaAnual = tasaEfectivaAnual(edadVehiculo, porcentajeCuotaInicial, plazoMeses);
        double tasaMesVencida = Math.pow(1.0 + (tasaEfectivaAnual / 100.0), 1.0 / 12.0) - 1.0;
        double cuotaCapitalInteres = calcularCuotaMensual(montoFinanciar, tasaMesVencida, plazoMeses);
        double segurosMensuales = (montoFinanciar * 0.00085) + (precioVehiculo * 0.00025);
        double cargosMensuales = CARGO_ADMINISTRATIVO_MENSUAL;
        double cuotaMensualTotal = cuotaCapitalInteres + segurosMensuales + cargosMensuales;
        double ingresoMensual = Math.max(valor(form.getIngresoMensual()), 0.0);
        double otrasObligaciones = Math.max(valor(form.getOtrasObligaciones()), 0.0);
        double capacidadPagoDisponible = Math.max((ingresoMensual * 0.35) - otrasObligaciones, 0.0);
        double relacionCuotaIngreso = ingresoMensual > 0.0 ? cuotaMensualTotal / ingresoMensual : 1.0;
        double relacionEndeudamientoTotal = ingresoMensual > 0.0 ? (cuotaMensualTotal + otrasObligaciones) / ingresoMensual : 1.0;
        double montoMaximoSugerido = calcularMontoMaximoSugerido(capacidadPagoDisponible, tasaMesVencida, plazoMeses);
        double ingresoMinimoSugerido = cuotaMensualTotal / 0.35;

        DecisionFinanciamiento decision = evaluarDecision(cuotaInicial,
                cuotaInicialMinima,
                ingresoMensual,
                capacidadPagoDisponible,
                cuotaMensualTotal,
                relacionCuotaIngreso,
                relacionEndeudamientoTotal,
                montoMaximoSugerido);

        FinanciamientoResultadoDTO resultado = new FinanciamientoResultadoDTO();
        resultado.setVehiculoId(vehiculo.getIdVehiculo());
        resultado.setNombreVehiculo((vehiculo.getMarca() == null ? "Vehículo" : vehiculo.getMarca())
                + " "
                + (vehiculo.getModelo() == null ? "sin referencia" : vehiculo.getModelo()));
        resultado.setPrecioVehiculo(precioVehiculo);
        resultado.setPlazosDisponibles(plazosDisponibles);
        resultado.setPorcentajeCuotaInicialMinima(porcentajeCuotaInicialMinima);
        resultado.setCuotaInicialMinima(cuotaInicialMinima);
        resultado.setCuotaInicial(cuotaInicial);
        resultado.setPorcentajeCuotaInicial(porcentajeCuotaInicial);
        resultado.setPlazoMeses(plazoMeses);
        resultado.setMontoFinanciar(montoFinanciar);
        resultado.setMontoMaximoSugerido(montoMaximoSugerido);
        resultado.setTasaEfectivaAnual(tasaEfectivaAnual);
        resultado.setTasaMesVencida(tasaMesVencida * 100.0);
        resultado.setCuotaCapitalInteres(cuotaCapitalInteres);
        resultado.setSegurosMensuales(segurosMensuales);
        resultado.setCargosMensuales(cargosMensuales);
        resultado.setCuotaMensualTotal(cuotaMensualTotal);
        resultado.setIngresoMinimoSugerido(ingresoMinimoSugerido);
        resultado.setCapacidadPagoDisponible(capacidadPagoDisponible);
        resultado.setRelacionCuotaIngreso(relacionCuotaIngreso * 100.0);
        resultado.setRelacionEndeudamientoTotal(relacionEndeudamientoTotal * 100.0);
        resultado.setEstadoAnalisis(decision.estado());
        resultado.setAprobadoPreliminar(decision.preaprobado());
        resultado.setMensajeDecision(decision.mensaje());
        resultado.setCanalOrigen(canalOrigen);
        resultado.setDocumentosRequeridos(documentosRequeridos(decision.estado()));
        return resultado;
    }

    private DecisionFinanciamiento evaluarDecision(double cuotaInicial,
                                                   double cuotaInicialMinima,
                                                   double ingresoMensual,
                                                   double capacidadPagoDisponible,
                                                   double cuotaMensualTotal,
                                                   double relacionCuotaIngreso,
                                                   double relacionEndeudamientoTotal,
                                                   double montoMaximoSugerido) {
        if (cuotaInicial < cuotaInicialMinima) {
            return new DecisionFinanciamiento(
                    "NO_VIABLE",
                    false,
                    "La cuota inicial está por debajo del mínimo requerido para este vehículo. Debe aportar al menos "
                            + formatoMoneda(cuotaInicialMinima) + ".");
        }

        if (ingresoMensual < INGRESO_MINIMO_REFERENCIA) {
            return new DecisionFinanciamiento(
                    "NO_VIABLE",
                    false,
                    "El ingreso reportado está por debajo del umbral mínimo de análisis preliminar del concesionario.");
        }

        if (capacidadPagoDisponible <= 0.0) {
            return new DecisionFinanciamiento(
                    "NO_VIABLE",
                    false,
                    "Las obligaciones actuales ya consumen la capacidad de pago disponible para asumir una nueva cuota.");
        }

        if (cuotaMensualTotal <= capacidadPagoDisponible && relacionCuotaIngreso <= 30.0 / 100.0 && relacionEndeudamientoTotal <= 45.0 / 100.0) {
            return new DecisionFinanciamiento(
                    "PREAPROBADO",
                    true,
                    "El perfil cumple con la política preliminar: cuota sostenible, relación de endeudamiento sana y entrada suficiente.");
        }

        if (cuotaMensualTotal <= ingresoMensual * 0.40 && relacionEndeudamientoTotal <= 55.0 / 100.0) {
            return new DecisionFinanciamiento(
                    "EN_ESTUDIO",
                    false,
                    "La operación es viable, pero requiere revisión documental y posible ajuste de entrada o codeudor. Con este ingreso el monto sugerido es "
                            + formatoMoneda(montoMaximoSugerido) + ".");
        }

        return new DecisionFinanciamiento(
                "NO_VIABLE",
                false,
                "La cuota proyectada supera la capacidad de pago estimada. El monto sugerido por política no debería exceder "
                        + formatoMoneda(montoMaximoSugerido) + ".");
    }

    private List<String> documentosRequeridos(String estadoAnalisis) {
        List<String> base = List.of(
                "Cédula ampliada al 150%",
                "Soporte de ingresos de los últimos 3 meses",
                "Certificación laboral o extractos",
                "Soporte de la cuota inicial",
                "Recibo de servicio público o prueba de residencia");

        if ("PREAPROBADO".equalsIgnoreCase(estadoAnalisis)) {
            return base;
        }
        if ("EN_ESTUDIO".equalsIgnoreCase(estadoAnalisis)) {
            return List.of(
                    "Cédula ampliada al 150%",
                    "Soporte de ingresos de los últimos 3 meses",
                    "Extractos bancarios recientes",
                    "Soporte de la cuota inicial",
                    "Documentación de codeudor si aplica");
        }
        return List.of(
                "Revisar aumento de cuota inicial",
                "Revisar consolidación de obligaciones",
                "Validar ingresos adicionales o codeudor");
    }

    private int calcularEdadVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getAnio() == null) {
            return 5;
        }
        return Math.max(LocalDate.now().getYear() - vehiculo.getAnio(), 0);
    }

    private List<Integer> plazosDisponibles(Vehiculo vehiculo) {
        int edad = calcularEdadVehiculo(vehiculo);
        int plazoMaximo = edad <= 2 ? 72 : (edad <= 5 ? 60 : (edad <= 8 ? 48 : 36));
        return List.of(12, 24, 36, 48, 60, 72).stream()
                .filter(plazo -> plazo <= plazoMaximo)
                .toList();
    }

    private int resolverPlazo(Integer plazoSolicitado, List<Integer> plazosDisponibles) {
        if (plazoSolicitado != null && plazosDisponibles.contains(plazoSolicitado)) {
            return plazoSolicitado;
        }
        return plazosDisponibles.stream().max(Integer::compareTo).orElse(48);
    }

    private double cuotaInicialMinimaPorcentaje(int edadVehiculo) {
        if (edadVehiculo <= 2) {
            return 20.0;
        }
        if (edadVehiculo <= 5) {
            return 25.0;
        }
        if (edadVehiculo <= 8) {
            return 30.0;
        }
        return 35.0;
    }

    private double tasaEfectivaAnual(int edadVehiculo, double porcentajeCuotaInicial, int plazoMeses) {
        double base = edadVehiculo <= 2 ? 16.8 : (edadVehiculo <= 5 ? 18.4 : (edadVehiculo <= 8 ? 20.9 : 24.2));

        if (porcentajeCuotaInicial >= 40.0) {
            base -= 1.0;
        } else if (porcentajeCuotaInicial >= 30.0) {
            base -= 0.4;
        } else if (porcentajeCuotaInicial < 25.0) {
            base += 0.9;
        }

        if (plazoMeses >= 60) {
            base += 0.7;
        } else if (plazoMeses <= 24) {
            base -= 0.5;
        }

        return Math.max(base, 14.5);
    }

    private double calcularCuotaMensual(double monto, double tasaMensual, int meses) {
        if (monto <= 0.0 || meses <= 0) {
            return 0.0;
        }
        if (tasaMensual <= 0.0) {
            return monto / meses;
        }
        double factor = Math.pow(1.0 + tasaMensual, meses);
        return monto * (tasaMensual * factor) / (factor - 1.0);
    }

    private double calcularMontoMaximoSugerido(double capacidadPagoDisponible, double tasaMensual, int meses) {
        if (capacidadPagoDisponible <= 0.0 || meses <= 0) {
            return 0.0;
        }
        double cuotaSinCargos = Math.max(capacidadPagoDisponible - CARGO_ADMINISTRATIVO_MENSUAL, 0.0);
        if (cuotaSinCargos <= 0.0) {
            return 0.0;
        }
        if (tasaMensual <= 0.0) {
            return cuotaSinCargos * meses;
        }
        return cuotaSinCargos * (1.0 - Math.pow(1.0 + tasaMensual, -meses)) / tasaMensual;
    }

    private Vehiculo obtenerVehiculo(Long vehiculoId) {
        return vehiculoRepository.findById(Objects.requireNonNull(vehiculoId, "El vehículo es obligatorio."))
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado."));
    }

    private Comparator<SolicitudFinanciamiento> comparadorSeguimiento() {
        return Comparator.comparing(SolicitudFinanciamiento::getFechaUltimaGestion, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(SolicitudFinanciamiento::getFechaSimulacion, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private boolean esPendienteDocumental(SolicitudFinanciamiento solicitud) {
        String estadoDocumental = normalizarEstadoDocumental(solicitud.getEstadoDocumental(), solicitud.getEstadoDocumental());
        return "PENDIENTE".equals(estadoDocumental)
                || "INCOMPLETO".equals(estadoDocumental)
                || "OBSERVADO".equals(estadoDocumental);
    }

    private void validarSeguimiento(SeguimientoFinanciamientoForm form) {
        if (form == null || form.getSolicitudId() == null) {
            throw new IllegalArgumentException("Selecciona la solicitud que vas a gestionar.");
        }
    }

    private String definirEstadoDocumentalInicial(String estadoAnalisis) {
        return switch (estadoAnalisis == null ? "" : estadoAnalisis.trim().toUpperCase(LOCALE_ES)) {
            case "PREAPROBADO" -> "PENDIENTE";
            case "EN_ESTUDIO" -> "INCOMPLETO";
            default -> "OBSERVADO";
        };
    }

    private String definirEtapaInicial(String estadoAnalisis) {
        return switch (estadoAnalisis == null ? "" : estadoAnalisis.trim().toUpperCase(LOCALE_ES)) {
            case "PREAPROBADO", "EN_ESTUDIO" -> "RADICACION_PENDIENTE";
            default -> "DESCARTADO";
        };
    }

    private String normalizarEstadoDocumental(String estadoDocumental, String valorPorDefecto) {
        String candidato = (estadoDocumental == null || estadoDocumental.isBlank()) ? valorPorDefecto : estadoDocumental;
        String normalizado = candidato == null ? "PENDIENTE" : candidato.trim().toUpperCase(LOCALE_ES);
        return ESTADOS_DOCUMENTALES_VALIDOS.contains(normalizado) ? normalizado : "PENDIENTE";
    }

    private String normalizarEtapaProceso(String etapaProceso, String valorPorDefecto) {
        String candidato = (etapaProceso == null || etapaProceso.isBlank()) ? valorPorDefecto : etapaProceso;
        String normalizado = candidato == null ? "RADICACION_PENDIENTE" : candidato.trim().toUpperCase(LOCALE_ES);
        return ETAPAS_PROCESO_VALIDAS.contains(normalizado) ? normalizado : "RADICACION_PENDIENTE";
    }

    private String construirBitacora(String gestorCorreo,
                                     String gestorNombre,
                                     SeguimientoFinanciamientoForm form,
                                     String estadoDocumental,
                                     String etapaProceso,
                                     boolean negocioCreado) {
        LocalDateTime ahora = LocalDateTime.now();
        StringBuilder bitacora = new StringBuilder();
        bitacora.append(FORMATO_BITACORA.format(ahora))
                .append(" - ")
                .append(nombreGestor(gestorCorreo, gestorNombre))
                .append(": etapa ")
                .append(etapaProceso.replace('_', ' ').toLowerCase(LOCALE_ES))
                .append(", documentación ")
                .append(estadoDocumental.toLowerCase(LOCALE_ES));

        if (negocioCreado) {
            bitacora.append(", negocio creado");
        }

        if (form.getEntidadFinanciera() != null && !form.getEntidadFinanciera().isBlank()) {
            bitacora.append(", entidad ").append(normalizarTexto(form.getEntidadFinanciera()));
        }

        if (form.getFechaDesembolsoProgramada() != null) {
            bitacora.append(", desembolso programado ").append(form.getFechaDesembolsoProgramada());
        }

        if (form.getMontoDesembolsado() != null && valor(form.getMontoDesembolsado()) > 0.0) {
            bitacora.append(", monto ").append(formatoMoneda(valor(form.getMontoDesembolsado())));
        }

        String observacionesGestion = normalizarTexto(form.getObservacionesGestion());
        if (!observacionesGestion.isBlank()) {
            bitacora.append(". ").append(observacionesGestion);
        }

        return bitacora.toString();
    }

    private String nombreGestor(String gestorCorreo, String gestorNombre) {
        String nombre = normalizarTexto(gestorNombre);
        if (!nombre.isBlank()) {
            return nombre;
        }
        String correo = normalizarTexto(gestorCorreo);
        return correo.isBlank() ? "Equipo financiero" : correo;
    }

    private String anexarBitacora(String actual, String nuevaEntrada) {
        String valorActual = normalizarTexto(actual);
        String entrada = normalizarTexto(nuevaEntrada);
        if (entrada.isBlank()) {
            return valorActual;
        }
        if (valorActual.isBlank()) {
            return entrada;
        }
        return valorActual + "\n" + entrada;
    }

    private Usuario resolverUsuario(String correoUsuarioAutenticado) {
        if (correoUsuarioAutenticado == null || correoUsuarioAutenticado.isBlank() || usuarioRepository == null) {
            return null;
        }
        return usuarioRepository.findByCorreo(correoUsuarioAutenticado.trim()).orElse(null);
    }

    private void validarFormulario(FinanciamientoSolicitudForm form, boolean requiereContacto) {
        if (form == null) {
            throw new IllegalArgumentException("La solicitud de financiamiento es obligatoria.");
        }
        if (requiereContacto && normalizarTexto(form.getNombreCompleto()).isBlank()) {
            throw new IllegalArgumentException("Ingresa el nombre del solicitante.");
        }
        if (requiereContacto && normalizarCorreo(form.getCorreo()).isBlank()) {
            throw new IllegalArgumentException("Ingresa el correo del solicitante.");
        }
        if (requiereContacto && normalizarTexto(form.getTelefono()).isBlank()) {
            throw new IllegalArgumentException("Ingresa el teléfono del solicitante.");
        }
        if (valor(form.getCuotaInicial()) <= 0.0) {
            throw new IllegalArgumentException("La cuota inicial debe ser mayor a cero.");
        }
        if (valor(form.getIngresoMensual()) <= 0.0) {
            throw new IllegalArgumentException("El ingreso mensual debe ser mayor a cero.");
        }
    }

    private String normalizarCanal(String canalOrigen) {
        if (canalOrigen == null || canalOrigen.isBlank()) {
            return "CLIENTE_WEB";
        }
        String valor = canalOrigen.trim().toUpperCase(LOCALE_ES);
        return switch (valor) {
            case "VENDEDOR", "GERENTE", "REFERENCIAL" -> valor;
            default -> "CLIENTE_WEB";
        };
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarCorreo(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(LOCALE_ES);
    }

    private String formatoMoneda(double valor) {
        return java.text.NumberFormat.getCurrencyInstance(LOCALE_ES).format(valor);
    }

    private double valor(Number numero) {
        return numero == null ? 0.0 : numero.doubleValue();
    }

    private record DecisionFinanciamiento(String estado, boolean preaprobado, String mensaje) {
    }
}