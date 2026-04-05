package com.example.demostracion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByConductorIdConductor(Long idConductor);

    List<Pedido> findByClienteUsuarioIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);
}
