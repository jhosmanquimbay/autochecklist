package com.example.demostracion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.repository.ConductorRepository;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;

    public ConductorService(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    public List<Conductor> listarTodos() {
        return conductorRepository.findAll();
    }

    public Optional<Conductor> buscarPorId(Long id) {
        return conductorRepository.findById(id);
    }

    public Conductor guardar(Conductor conductor) {
        return conductorRepository.save(conductor);
    }

    public void eliminar(Long id) {
        conductorRepository.deleteById(id);
    }

    // 👉 Nuevo: buscar por username (para login del conductor)
    public Optional<Conductor> buscarPorUsername(String username) {
        return conductorRepository.findByUsername(username);
    }
}
