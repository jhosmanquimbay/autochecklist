package com.example.demostracion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demostracion.dto.FinanciamientoSolicitudForm;
import com.example.demostracion.dto.ResumenFinanciamientoDTO;
import com.example.demostracion.dto.SeguimientoFinanciamientoForm;
import com.example.demostracion.model.SolicitudFinanciamiento;
import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.SolicitudFinanciamientoRepository;
import com.example.demostracion.repository.UsuarioRepository;
import com.example.demostracion.repository.VehiculoRepository;

@SuppressWarnings({"null", "unused"})
class FinanciamientoSeguimientoServiceTest {

    private FinanciamientoService financiamientoService;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private SolicitudFinanciamientoRepository solicitudFinanciamientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        financiamientoService = new FinanciamientoService(vehiculoRepository, solicitudFinanciamientoRepository, usuarioRepository);
    }

    @Test
    void deberiaInicializarSeguimientoCuandoSeRegistraUnaSimulacion() {
        Vehiculo vehiculo = vehiculo(1L, 92_000_000d, 2024);
        FinanciamientoSolicitudForm form = formularioBase();

        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(solicitudFinanciamientoRepository.save(org.mockito.ArgumentMatchers.<SolicitudFinanciamiento>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        financiamientoService.registrarSimulacion(1L, form, "VENDEDOR", "asesor@dealer.com", "Laura Asesora", null);

        ArgumentCaptor<SolicitudFinanciamiento> captor = ArgumentCaptor.forClass(SolicitudFinanciamiento.class);
        verify(solicitudFinanciamientoRepository).save(captor.capture());

        SolicitudFinanciamiento guardada = captor.getValue();
        assertThat(guardada.getEstadoDocumental()).isEqualTo("PENDIENTE");
        assertThat(guardada.getEtapaProceso()).isEqualTo("RADICACION_PENDIENTE");
        assertThat(guardada.isNegocioCreado()).isFalse();
        assertThat(guardada.getFechaUltimaGestion()).isNotNull();
        assertThat(guardada.getObservacionesSeguimiento()).contains("Simulación creada desde vendedor");
    }

    @Test
    void deberiaActualizarSeguimientoHastaDesembolso() {
        SolicitudFinanciamiento solicitud = solicitudBase();
        solicitud.setIdSolicitudFinanciamiento(7L);
        solicitud.setMontoFinanciar(54_000_000d);
        solicitud.setEstadoDocumental("PENDIENTE");
        solicitud.setEtapaProceso("RADICACION_PENDIENTE");
        solicitud.setFechaUltimaGestion(LocalDateTime.now().minusDays(1));

        SeguimientoFinanciamientoForm form = new SeguimientoFinanciamientoForm();
        form.setSolicitudId(7L);
        form.setCrearNegocio(Boolean.TRUE);
        form.setEstadoDocumental("COMPLETO");
        form.setEtapaProceso("DESEMBOLSADO");
        form.setEntidadFinanciera("Banco Aliado");
        form.setFechaDesembolsoProgramada(LocalDate.of(2026, 4, 10));
        form.setMontoDesembolsado(52_500_000d);
        form.setObservacionesGestion("Entidad confirmó giro para cierre de mes.");

        when(solicitudFinanciamientoRepository.findById(7L)).thenReturn(Optional.of(solicitud));
        when(solicitudFinanciamientoRepository.save(org.mockito.ArgumentMatchers.<SolicitudFinanciamiento>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitudFinanciamiento actualizada = financiamientoService.actualizarSeguimiento(7L, form, "gerencia@dealer.com", "Gerencia");

        assertThat(actualizada.isNegocioCreado()).isTrue();
        assertThat(actualizada.getFechaNegocioCreado()).isNotNull();
        assertThat(actualizada.getEstadoDocumental()).isEqualTo("COMPLETO");
        assertThat(actualizada.getEtapaProceso()).isEqualTo("DESEMBOLSADO");
        assertThat(actualizada.getEntidadFinanciera()).isEqualTo("Banco Aliado");
        assertThat(actualizada.getFechaDesembolsoProgramada()).isEqualTo(LocalDate.of(2026, 4, 10));
        assertThat(actualizada.getFechaDesembolsoReal()).isEqualTo(LocalDate.now());
        assertThat(actualizada.getMontoDesembolsado()).isEqualTo(52_500_000d);
        assertThat(actualizada.getObservacionesSeguimiento()).contains("Banco Aliado").contains("Entidad confirmó giro");
    }

    @Test
    void deberiaConstruirResumenConNegociosYDesembolsos() {
        SolicitudFinanciamiento negocioAprobado = solicitudBase();
        negocioAprobado.setEstadoAnalisis("PREAPROBADO");
        negocioAprobado.setCanalOrigen("VENDEDOR");
        negocioAprobado.setNegocioCreado(true);
        negocioAprobado.setEstadoDocumental("COMPLETO");
        negocioAprobado.setEtapaProceso("APROBADO_ENTIDAD");
        negocioAprobado.setMontoFinanciar(40_000_000d);
        negocioAprobado.setCuotaMensualTotal(1_050_000d);

        SolicitudFinanciamiento pendienteDocumentos = solicitudBase();
        pendienteDocumentos.setEstadoAnalisis("EN_ESTUDIO");
        pendienteDocumentos.setCanalOrigen("CLIENTE_WEB");
        pendienteDocumentos.setNegocioCreado(false);
        pendienteDocumentos.setEstadoDocumental("INCOMPLETO");
        pendienteDocumentos.setEtapaProceso("RADICACION_PENDIENTE");
        pendienteDocumentos.setMontoFinanciar(28_000_000d);
        pendienteDocumentos.setCuotaMensualTotal(820_000d);

        SolicitudFinanciamiento desembolsada = solicitudBase();
        desembolsada.setEstadoAnalisis("PREAPROBADO");
        desembolsada.setCanalOrigen("CLIENTE_WEB");
        desembolsada.setNegocioCreado(true);
        desembolsada.setEstadoDocumental("COMPLETO");
        desembolsada.setEtapaProceso("DESEMBOLSADO");
        desembolsada.setMontoFinanciar(52_000_000d);
        desembolsada.setMontoDesembolsado(49_500_000d);
        desembolsada.setCuotaMensualTotal(1_240_000d);

        when(solicitudFinanciamientoRepository.findAll()).thenReturn(List.of(negocioAprobado, pendienteDocumentos, desembolsada));

        ResumenFinanciamientoDTO resumen = financiamientoService.construirResumenGeneral();

        assertThat(resumen.getNegociosOriginados()).isEqualTo(2);
        assertThat(resumen.getPendientesDocumentos()).isEqualTo(1);
        assertThat(resumen.getDocumentacionCompleta()).isEqualTo(2);
        assertThat(resumen.getListasDesembolso()).isEqualTo(1);
        assertThat(resumen.getDesembolsadas()).isEqualTo(1);
        assertThat(resumen.getMontoDesembolsado()).isEqualTo(49_500_000d);
        assertThat(resumen.getMontoPendienteDesembolso()).isEqualTo(40_000_000d);
        assertThat(resumen.getPorcentajeConversionNegocio()).isGreaterThan(60.0);
    }

    private FinanciamientoSolicitudForm formularioBase() {
        FinanciamientoSolicitudForm form = new FinanciamientoSolicitudForm();
        form.setNombreCompleto("Mariana López");
        form.setCorreo("mariana@correo.com");
        form.setTelefono("3001234567");
        form.setCuotaInicial(28_000_000d);
        form.setPlazoMeses(60);
        form.setIngresoMensual(8_400_000d);
        form.setOtrasObligaciones(450_000d);
        form.setObservaciones("Cliente con contrato indefinido.");
        return form;
    }

    private SolicitudFinanciamiento solicitudBase() {
        SolicitudFinanciamiento solicitud = new SolicitudFinanciamiento();
        solicitud.setFechaSimulacion(LocalDateTime.now().minusDays(2));
        solicitud.setFechaUltimaGestion(LocalDateTime.now().minusDays(1));
        solicitud.setSolicitanteNombre("Cliente Demo");
        solicitud.setSolicitanteCorreo("cliente@correo.com");
        solicitud.setSolicitanteTelefono("3001234567");
        solicitud.setMontoFinanciar(30_000_000d);
        solicitud.setCuotaMensualTotal(900_000d);
        solicitud.setEstadoAnalisis("PREAPROBADO");
        solicitud.setEstadoDocumental("PENDIENTE");
        solicitud.setEtapaProceso("RADICACION_PENDIENTE");
        solicitud.setCanalOrigen("VENDEDOR");
        solicitud.setMontoDesembolsado(0.0);
        return solicitud;
    }

    private Vehiculo vehiculo(Long id, double precio, int anio) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(id);
        vehiculo.setMarca("Mazda");
        vehiculo.setModelo("CX-5");
        vehiculo.setPrecio(precio);
        vehiculo.setAnio(anio);
        vehiculo.setActivo(true);
        return vehiculo;
    }
}