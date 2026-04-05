package com.example.demostracion;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demostracion.repository.UsuarioRepository;

@SpringBootTest
@Disabled("Prueba de depuracion local; no debe romper la suite automatizada")
public class DebugUserPasswordTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void failWithAdminPassword() {
        usuarioRepository.findByCorreo("admin@test.com").ifPresentOrElse(u -> {
            throw new RuntimeException("ADMIN PASSWORD (from DB): " + u.getContrasena());
        }, () -> {
            throw new RuntimeException("Admin user not found");
        });
    }
}

