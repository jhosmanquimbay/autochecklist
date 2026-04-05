package com.example.demostracion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demostracion.model.SolicitudPrueba;
import com.example.demostracion.model.Cliente;
import com.example.demostracion.model.Vehiculo;
import java.util.List;

@Repository
public interface SolicitudPruebaRepository extends JpaRepository<SolicitudPrueba, Long> {
    List<SolicitudPrueba> findByEstado(String estado);
    List<SolicitudPrueba> findByCliente(Cliente cliente);
    List<SolicitudPrueba> findByVehiculo(Vehiculo vehiculo);
    List<SolicitudPrueba> findByEstadoAndClienteOrderByFechaSolicitadaDesc(String estado, Cliente cliente);
}
