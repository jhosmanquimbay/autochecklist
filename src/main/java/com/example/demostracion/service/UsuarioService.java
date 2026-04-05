package com.example.demostracion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.ConductorRepository;
import com.example.demostracion.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ConductorRepository conductorRepository) {
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
        if (usuario.getIdUsuario() != null) {
            usuarioRepository.findById(usuario.getIdUsuario()).ifPresent(actual -> {
                if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
                    usuario.setContrasena(actual.getContrasena());
                }
                if (usuario.getActivo() == null) {
                    usuario.setActivo(actual.getActivo());
                }
            });
        }

        Usuario nuevo = usuarioRepository.save(usuario);

        // Si el rol del usuario es CONDUCTOR o VENDEDOR (con o sin prefijo)
        String rol = nuevo.getRol() != null && nuevo.getRol().getNombre() != null
                ? nuevo.getRol().getNombre()
                : "";
        if (rol.equalsIgnoreCase("CONDUCTOR") || rol.equalsIgnoreCase("ROLE_CONDUCTOR")
            || rol.equalsIgnoreCase("VENDEDOR") || rol.equalsIgnoreCase("ROLE_VENDEDOR")) {

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
