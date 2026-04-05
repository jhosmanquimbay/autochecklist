package com.example.demostracion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.ContabilidadVenta;

public interface ContabilidadVentaRepository extends JpaRepository<ContabilidadVenta, Long> {

    Optional<ContabilidadVenta> findByPedidoIdPedido(Long idPedido);
}