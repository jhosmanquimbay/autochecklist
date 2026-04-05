package com.example.demostracion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demostracion.model.Inventario;
import com.example.demostracion.repository.InventarioRepository;

@Service
public class InventarioService {
    
    @Autowired
    private InventarioRepository inventarioRepository;

    public List<Inventario> listar() {
        return inventarioRepository.findAll();
    }

    public Inventario guardar(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    public void eliminar(Long id) {
        inventarioRepository.deleteById(id);
    }
}
