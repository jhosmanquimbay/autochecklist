package com.example.demostracion.repository;

import com.example.demostracion.model.Novedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NovedadRepository extends JpaRepository<Novedad, Long> {
}
