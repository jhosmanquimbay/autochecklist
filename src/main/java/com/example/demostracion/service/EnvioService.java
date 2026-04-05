package com.example.demostracion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.ConductorRepository;
import com.example.demostracion.repository.UsuarioRepository;

@Service
public class EnvioService {

    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;

    public EnvioService(UsuarioRepository usuarioRepository, ConductorRepository conductorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario guardar(Usuario usuario) {
        Usuario nuevo = usuarioRepository.save(usuario);

        // 👉 Si el rol del usuario es CONDUCTOR (con o sin prefijo)
        String rol = nuevo.getRol().getNombre();
        if (rol.equalsIgnoreCase("CONDUCTOR") || rol.equalsIgnoreCase("ROLE_CONDUCTOR")) {

            String username = nuevo.getCorreo(); // ⚡ usamos el correo como identificador/login

            // Validar si ya existe un conductor con ese username
            boolean existe = conductorRepository.findByUsername(username).isPresent();
            if (!existe) {
                Conductor conductor = new Conductor();
                conductor.setUsername(username);         // Enlazado al login (correo)
                conductor.setNombre(nuevo.getNombre());  // Nombre visible
                conductor.setLicencia("PENDIENTE");      // valor por defecto
                conductor.setTelefono("SIN REGISTRO");   // valor por defecto
                conductorRepository.save(conductor);
            }
        }

        return nuevo;
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}
