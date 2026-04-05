package com.example.demostracion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demostracion.model.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    
    boolean existsByChasis(String chasis);

    boolean existsByChasisIgnoreCase(String chasis);

    boolean existsByChasisIgnoreCaseAndIdInventarioNot(String chasis, Long idInventario);

        Optional<Inventario> findByChasis(String chasis);

    @Query("SELECT i FROM Inventario i " +
           "WHERE (:marca = '' OR LOWER(i.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) " +
           "AND (:modelo = '' OR LOWER(i.modelo) LIKE LOWER(CONCAT('%', :modelo, '%'))) " +
           "AND (:color = '' OR LOWER(i.color) LIKE LOWER(CONCAT('%', :color, '%'))) " +
           "AND (:estadoLogistico = '' OR LOWER(i.estadoLogistico) LIKE LOWER(CONCAT('%', :estadoLogistico, '%')))")
    List<Inventario> findByFiltros(
            @Param("marca") String marca,
            @Param("modelo") String modelo,
            @Param("color") String color,
            @Param("estadoLogistico") String estadoLogistico
    );
    List<Inventario> findByActivoFalse();
    
    List<Inventario> findByActivoTrue();
    
    @Query("SELECT i FROM Inventario i " +
           "WHERE (:marca = '' OR LOWER(i.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) " +
           "AND (:modelo = '' OR LOWER(i.modelo) LIKE LOWER(CONCAT('%', :modelo, '%'))) " +
           "AND (:color = '' OR LOWER(i.color) LIKE LOWER(CONCAT('%', :color, '%'))) " +
           "AND (:estadoLogistico = '' OR LOWER(i.estadoLogistico) LIKE LOWER(CONCAT('%', :estadoLogistico, '%'))) " +
           "AND i.activo = :activo")
    List<Inventario> findByFiltrosAndActivo(
            @Param("marca") String marca,
            @Param("modelo") String modelo,
            @Param("color") String color,
            @Param("estadoLogistico") String estadoLogistico,
            @Param("activo") boolean activo
    );

}
