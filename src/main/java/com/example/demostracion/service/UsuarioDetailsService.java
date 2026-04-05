package com.example.demostracion.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        // Asegurarse de que el usuario tiene rol asignado
        String rolNombre;
        if (usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            rolNombre = "USER";
            System.out.println("[UsuarioDetailsService] Usuario sin rol definido, asignando ROLE_USER por defecto: " + correo);
        } else {
            rolNombre = usuario.getRol().getNombre().trim().toUpperCase();
            if (rolNombre.startsWith("ROLE_")) {
                rolNombre = rolNombre.substring("ROLE_".length());
            }
        }

        System.out.println("[UsuarioDetailsService] Autenticando " + correo + " con rol " + rolNombre + " y password '" + usuario.getContrasena() + "'");

        //  OJO: aquí usamos .roles() porque en tu BD tienes "ADMIN", "GERENTE", "CONDUCTOR" sin ROLE_
        return User.builder()
                .username(usuario.getCorreo())                  // login con correo
                .password(usuario.getContrasena())
                .roles(rolNombre)                               // Spring agrega el prefijo ROLE_
                .build();
    }
}
    