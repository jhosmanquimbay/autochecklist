package com.example.demostracion.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demostracion.dto.FinanciamientoResultadoDTO;
import com.example.demostracion.dto.FinanciamientoSolicitudForm;
import com.example.demostracion.model.Vehiculo;

class FinanciamientoServiceTest {

    private FinanciamientoService financiamientoService;

    @BeforeEach
    void setUp() {
        financiamientoService = new FinanciamientoService(null, null, null);
    }

    @Test
    void deberiaPreaprobarCuandoLaCapacidadDePagoEsSana() {
        Vehiculo vehiculo = vehiculo(95_000_000d, 2023);
        FinanciamientoSolicitudForm form = new FinanciamientoSolicitudForm();
        form.setNombreCompleto("Laura Díaz");
        form.setCorreo("laura@correo.com");
        form.setTelefono("3001234567");
        form.setCuotaInicial(35_000_000d);
        form.setPlazoMeses(60);
        form.setIngresoMensual(9_500_000d);
        form.setOtrasObligaciones(600_000d);

        FinanciamientoResultadoDTO resultado = financiamientoService.simular(vehiculo, form, "CLIENTE_WEB");

        assertThat(resultado.isAprobadoPreliminar()).isTrue();
        assertThat(resultado.getEstadoAnalisis()).isEqualTo("PREAPROBADO");
        assertThat(resultado.getCuotaMensualTotal()).isGreaterThan(0.0);
    }

    @Test
    void deberiaMarcarNoViableSiLaCuotaInicialEsInsuficiente() {
        Vehiculo vehiculo = vehiculo(80_000_000d, 2016);
        FinanciamientoSolicitudForm form = new FinanciamientoSolicitudForm();
        form.setNombreCompleto("Carlos Pérez");
        form.setCorreo("carlos@correo.com");
        form.setTelefono("3001112233");
        form.setCuotaInicial(8_000_000d);
        form.setPlazoMeses(36);
        form.setIngresoMensual(6_000_000d);
        form.setOtrasObligaciones(300_000d);

        FinanciamientoResultadoDTO resultado = financiamientoService.simular(vehiculo, form, "CLIENTE_WEB");

        assertThat(resultado.isAprobadoPreliminar()).isFalse();
        assertThat(resultado.getEstadoAnalisis()).isEqualTo("NO_VIABLE");
        assertThat(resultado.getMensajeDecision()).contains("cuota inicial");
    }

    @Test
    void deberiaConstruirOfertaReferencialConPlazosDisponibles() {
        Vehiculo vehiculo = vehiculo(72_000_000d, 2021);

        FinanciamientoResultadoDTO resultado = financiamientoService.calcularOfertaReferencial(vehiculo);

        assertThat(resultado.getPlazosDisponibles()).contains(12, 24, 36, 48, 60);
        assertThat(resultado.getCuotaMensualTotal()).isGreaterThan(0.0);
        assertThat(resultado.getEstadoAnalisis()).isEqualTo("REFERENCIAL");
    }

    private Vehiculo vehiculo(double precio, int anio) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(1L);
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setPrecio(precio);
        vehiculo.setAnio(anio);
        return vehiculo;
    }
}