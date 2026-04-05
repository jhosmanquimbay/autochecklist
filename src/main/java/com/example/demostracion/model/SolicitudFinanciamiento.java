package com.example.demostracion.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitud_financiamiento")
public class SolicitudFinanciamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud_financiamiento")
    private Long idSolicitudFinanciamiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "cliente_usuario_id")
    private Usuario clienteUsuario;

    @Column(name = "canal_origen", nullable = false, length = 30)
    private String canalOrigen;

    @Column(name = "solicitante_nombre", nullable = false, length = 150)
    private String solicitanteNombre;

    @Column(name = "solicitante_correo", nullable = false, length = 150)
    private String solicitanteCorreo;

    @Column(name = "solicitante_telefono", nullable = false, length = 30)
    private String solicitanteTelefono;

    @Column(name = "asesor_correo", length = 150)
    private String asesorCorreo;

    @Column(name = "asesor_nombre", length = 120)
    private String asesorNombre;

    @Column(name = "precio_vehiculo_snapshot", nullable = false)
    private Double precioVehiculoSnapshot;

    @Column(name = "cuota_inicial", nullable = false)
    private Double cuotaInicial;

    @Column(name = "porcentaje_cuota_inicial", nullable = false)
    private Double porcentajeCuotaInicial;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "ingreso_mensual", nullable = false)
    private Double ingresoMensual;

    @Column(name = "otras_obligaciones", nullable = false)
    private Double otrasObligaciones;

    @Column(name = "monto_financiar", nullable = false)
    private Double montoFinanciar;

    @Column(name = "monto_maximo_sugerido", nullable = false)
    private Double montoMaximoSugerido;

    @Column(name = "tasa_efectiva_anual", nullable = false)
    private Double tasaEfectivaAnual;

    @Column(name = "tasa_mes_vencida", nullable = false)
    private Double tasaMesVencida;

    @Column(name = "cuota_capital_interes", nullable = false)
    private Double cuotaCapitalInteres;

    @Column(name = "seguros_mensuales", nullable = false)
    private Double segurosMensuales;

    @Column(name = "cargos_mensuales", nullable = false)
    private Double cargosMensuales;

    @Column(name = "cuota_mensual_total", nullable = false)
    private Double cuotaMensualTotal;

    @Column(name = "capacidad_pago_disponible", nullable = false)
    private Double capacidadPagoDisponible;

    @Column(name = "relacion_cuota_ingreso", nullable = false)
    private Double relacionCuotaIngreso;

    @Column(name = "relacion_endeudamiento_total", nullable = false)
    private Double relacionEndeudamientoTotal;

    @Column(name = "aprobado_preliminar", nullable = false)
    private boolean aprobadoPreliminar;

    @Column(name = "estado_analisis", nullable = false, length = 30)
    private String estadoAnalisis;

    @Column(name = "estado_documental", nullable = false, length = 30)
    private String estadoDocumental;

    @Column(name = "etapa_proceso", nullable = false, length = 40)
    private String etapaProceso;

    @Column(name = "negocio_creado", nullable = false)
    private boolean negocioCreado;

    @Column(name = "fecha_negocio_creado")
    private LocalDateTime fechaNegocioCreado;

    @Column(name = "entidad_financiera", length = 120)
    private String entidadFinanciera;

    @Column(name = "observaciones_seguimiento", columnDefinition = "TEXT")
    private String observacionesSeguimiento;

    @Column(name = "fecha_ultima_gestion", nullable = false)
    private LocalDateTime fechaUltimaGestion = LocalDateTime.now();

    @Column(name = "fecha_desembolso_programada")
    private LocalDate fechaDesembolsoProgramada;

    @Column(name = "fecha_desembolso_real")
    private LocalDate fechaDesembolsoReal;

    @Column(name = "monto_desembolsado")
    private Double montoDesembolsado;

    @Column(name = "mensaje_decision", columnDefinition = "TEXT")
    private String mensajeDecision;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "fecha_simulacion", nullable = false)
    private LocalDateTime fechaSimulacion = LocalDateTime.now();

    public Long getIdSolicitudFinanciamiento() {
        return idSolicitudFinanciamiento;
    }

    public void setIdSolicitudFinanciamiento(Long idSolicitudFinanciamiento) {
        this.idSolicitudFinanciamiento = idSolicitudFinanciamiento;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Usuario getClienteUsuario() {
        return clienteUsuario;
    }

    public void setClienteUsuario(Usuario clienteUsuario) {
        this.clienteUsuario = clienteUsuario;
    }

    public String getCanalOrigen() {
        return canalOrigen;
    }

    public void setCanalOrigen(String canalOrigen) {
        this.canalOrigen = canalOrigen;
    }

    public String getSolicitanteNombre() {
        return solicitanteNombre;
    }

    public void setSolicitanteNombre(String solicitanteNombre) {
        this.solicitanteNombre = solicitanteNombre;
    }

    public String getSolicitanteCorreo() {
        return solicitanteCorreo;
    }

    public void setSolicitanteCorreo(String solicitanteCorreo) {
        this.solicitanteCorreo = solicitanteCorreo;
    }

    public String getSolicitanteTelefono() {
        return solicitanteTelefono;
    }

    public void setSolicitanteTelefono(String solicitanteTelefono) {
        this.solicitanteTelefono = solicitanteTelefono;
    }

    public String getAsesorCorreo() {
        return asesorCorreo;
    }

    public void setAsesorCorreo(String asesorCorreo) {
        this.asesorCorreo = asesorCorreo;
    }

    public String getAsesorNombre() {
        return asesorNombre;
    }

    public void setAsesorNombre(String asesorNombre) {
        this.asesorNombre = asesorNombre;
    }

    public Double getPrecioVehiculoSnapshot() {
        return precioVehiculoSnapshot;
    }

    public void setPrecioVehiculoSnapshot(Double precioVehiculoSnapshot) {
        this.precioVehiculoSnapshot = precioVehiculoSnapshot;
    }

    public Double getCuotaInicial() {
        return cuotaInicial;
    }

    public void setCuotaInicial(Double cuotaInicial) {
        this.cuotaInicial = cuotaInicial;
    }

    public Double getPorcentajeCuotaInicial() {
        return porcentajeCuotaInicial;
    }

    public void setPorcentajeCuotaInicial(Double porcentajeCuotaInicial) {
        this.porcentajeCuotaInicial = porcentajeCuotaInicial;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public Double getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(Double ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    public Double getOtrasObligaciones() {
        return otrasObligaciones;
    }

    public void setOtrasObligaciones(Double otrasObligaciones) {
        this.otrasObligaciones = otrasObligaciones;
    }

    public Double getMontoFinanciar() {
        return montoFinanciar;
    }

    public void setMontoFinanciar(Double montoFinanciar) {
        this.montoFinanciar = montoFinanciar;
    }

    public Double getMontoMaximoSugerido() {
        return montoMaximoSugerido;
    }

    public void setMontoMaximoSugerido(Double montoMaximoSugerido) {
        this.montoMaximoSugerido = montoMaximoSugerido;
    }

    public Double getTasaEfectivaAnual() {
        return tasaEfectivaAnual;
    }

    public void setTasaEfectivaAnual(Double tasaEfectivaAnual) {
        this.tasaEfectivaAnual = tasaEfectivaAnual;
    }

    public Double getTasaMesVencida() {
        return tasaMesVencida;
    }

    public void setTasaMesVencida(Double tasaMesVencida) {
        this.tasaMesVencida = tasaMesVencida;
    }

    public Double getCuotaCapitalInteres() {
        return cuotaCapitalInteres;
    }

    public void setCuotaCapitalInteres(Double cuotaCapitalInteres) {
        this.cuotaCapitalInteres = cuotaCapitalInteres;
    }

    public Double getSegurosMensuales() {
        return segurosMensuales;
    }

    public void setSegurosMensuales(Double segurosMensuales) {
        this.segurosMensuales = segurosMensuales;
    }

    public Double getCargosMensuales() {
        return cargosMensuales;
    }

    public void setCargosMensuales(Double cargosMensuales) {
        this.cargosMensuales = cargosMensuales;
    }

    public Double getCuotaMensualTotal() {
        return cuotaMensualTotal;
    }

    public void setCuotaMensualTotal(Double cuotaMensualTotal) {
        this.cuotaMensualTotal = cuotaMensualTotal;
    }

    public Double getCapacidadPagoDisponible() {
        return capacidadPagoDisponible;
    }

    public void setCapacidadPagoDisponible(Double capacidadPagoDisponible) {
        this.capacidadPagoDisponible = capacidadPagoDisponible;
    }

    public Double getRelacionCuotaIngreso() {
        return relacionCuotaIngreso;
    }

    public void setRelacionCuotaIngreso(Double relacionCuotaIngreso) {
        this.relacionCuotaIngreso = relacionCuotaIngreso;
    }

    public Double getRelacionEndeudamientoTotal() {
        return relacionEndeudamientoTotal;
    }

    public void setRelacionEndeudamientoTotal(Double relacionEndeudamientoTotal) {
        this.relacionEndeudamientoTotal = relacionEndeudamientoTotal;
    }

    public boolean isAprobadoPreliminar() {
        return aprobadoPreliminar;
    }

    public void setAprobadoPreliminar(boolean aprobadoPreliminar) {
        this.aprobadoPreliminar = aprobadoPreliminar;
    }

    public String getEstadoAnalisis() {
        return estadoAnalisis;
    }

    public void setEstadoAnalisis(String estadoAnalisis) {
        this.estadoAnalisis = estadoAnalisis;
    }

    public String getEstadoDocumental() {
        return estadoDocumental;
    }

    public void setEstadoDocumental(String estadoDocumental) {
        this.estadoDocumental = estadoDocumental;
    }

    public String getEtapaProceso() {
        return etapaProceso;
    }

    public void setEtapaProceso(String etapaProceso) {
        this.etapaProceso = etapaProceso;
    }

    public boolean isNegocioCreado() {
        return negocioCreado;
    }

    public void setNegocioCreado(boolean negocioCreado) {
        this.negocioCreado = negocioCreado;
    }

    public LocalDateTime getFechaNegocioCreado() {
        return fechaNegocioCreado;
    }

    public void setFechaNegocioCreado(LocalDateTime fechaNegocioCreado) {
        this.fechaNegocioCreado = fechaNegocioCreado;
    }

    public String getEntidadFinanciera() {
        return entidadFinanciera;
    }

    public void setEntidadFinanciera(String entidadFinanciera) {
        this.entidadFinanciera = entidadFinanciera;
    }

    public String getObservacionesSeguimiento() {
        return observacionesSeguimiento;
    }

    public void setObservacionesSeguimiento(String observacionesSeguimiento) {
        this.observacionesSeguimiento = observacionesSeguimiento;
    }

    public LocalDateTime getFechaUltimaGestion() {
        return fechaUltimaGestion;
    }

    public void setFechaUltimaGestion(LocalDateTime fechaUltimaGestion) {
        this.fechaUltimaGestion = fechaUltimaGestion;
    }

    public LocalDate getFechaDesembolsoProgramada() {
        return fechaDesembolsoProgramada;
    }

    public void setFechaDesembolsoProgramada(LocalDate fechaDesembolsoProgramada) {
        this.fechaDesembolsoProgramada = fechaDesembolsoProgramada;
    }

    public LocalDate getFechaDesembolsoReal() {
        return fechaDesembolsoReal;
    }

    public void setFechaDesembolsoReal(LocalDate fechaDesembolsoReal) {
        this.fechaDesembolsoReal = fechaDesembolsoReal;
    }

    public Double getMontoDesembolsado() {
        return montoDesembolsado;
    }

    public void setMontoDesembolsado(Double montoDesembolsado) {
        this.montoDesembolsado = montoDesembolsado;
    }

    public String getMensajeDecision() {
        return mensajeDecision;
    }

    public void setMensajeDecision(String mensajeDecision) {
        this.mensajeDecision = mensajeDecision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaSimulacion() {
        return fechaSimulacion;
    }

    public void setFechaSimulacion(LocalDateTime fechaSimulacion) {
        this.fechaSimulacion = fechaSimulacion;
    }

    public String getEstadoAnalisisEtiqueta() {
        return switch (estadoAnalisis == null ? "" : estadoAnalisis.trim().toUpperCase()) {
            case "PREAPROBADO" -> "Preaprobado";
            case "EN_ESTUDIO" -> "En estudio";
            case "NO_VIABLE" -> "No viable";
            default -> "Referencial";
        };
    }

    public String getEstadoDocumentalEtiqueta() {
        return switch (estadoDocumental == null ? "" : estadoDocumental.trim().toUpperCase()) {
            case "PENDIENTE" -> "Pendiente";
            case "INCOMPLETO" -> "Incompleto";
            case "VALIDANDO" -> "Validando";
            case "COMPLETO" -> "Completo";
            case "OBSERVADO" -> "Observado";
            default -> "Sin definir";
        };
    }

    public String getEtapaProcesoEtiqueta() {
        return switch (etapaProceso == null ? "" : etapaProceso.trim().toUpperCase()) {
            case "RADICACION_PENDIENTE" -> "Radicación pendiente";
            case "EN_ANALISIS_ENTIDAD" -> "En análisis";
            case "APROBADO_ENTIDAD" -> "Aprobado por entidad";
            case "DESEMBOLSO_PROGRAMADO" -> "Desembolso programado";
            case "DESEMBOLSADO" -> "Desembolsado";
            case "DESCARTADO" -> "Descartado";
            default -> "Sin etapa";
        };
    }

    public String getNegocioCreadoEtiqueta() {
        return negocioCreado ? "Negocio creado" : "Sin negocio";
    }
}