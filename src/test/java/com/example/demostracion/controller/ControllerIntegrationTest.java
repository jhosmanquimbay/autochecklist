package com.example.demostracion.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para Controladores
 * 
 * ISO 25010: Mantenibilidad - Capacidad de prueba
 * ✅ Pruebas de integración para endpoints
 * 
 * @author Sistema
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Controladores - Integration Tests")
@SuppressWarnings("null")
public class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / debería retornar status 200")
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /login debería retornar página de login")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /recuperar debería retornar página de recuperación")
    void testRecuperarPage() throws Exception {
        mockMvc.perform(get("/recuperar"))
            .andExpect(status().isOk())
            .andExpect(view().name("recuperar"));
    }

    @Test
    @DisplayName("GET /restablecer sin paso previo debería redirigir a recuperación")
    void testRestablecerPage() throws Exception {
        mockMvc.perform(get("/restablecer"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recuperar"));
    }

    @Test
    @DisplayName("POST /login con credenciales inválidas debería fallar")
    void testLoginInvalido() throws Exception {
        mockMvc.perform(post("/login")
            .with(csrf())
            .param("username", "invalid")
            .param("password", "wrong"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("GET /dashboard sin autenticación debería redirigir a login")
    void testDashboardSinAutenticacion() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("GET /actuator/health debería estar disponible")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                Assertions.assertTrue(status == 200 || status == 503,
                    "El health endpoint debe responder 200 o 503 segun el estado de sus dependencias");
            })
            .andExpect(jsonPath("$.status").exists());
    }
}
