package com.example.demostracion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.example.demostracion.model.Usuario;
import com.example.demostracion.model.Rol;
import com.example.demostracion.repository.UsuarioRepository;

/**
 * Tests unitarios para UsuarioService
 * 
 * ISO 25010: Mantenibilidad - Capacidad de prueba
 * ✅ Cobertura de pruebas para confiabilidad
 * 
 * @author Sistema
 * @since 1.0
 */
@DisplayName("UsuarioService - Unit Tests")
public class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private com.example.demostracion.repository.ConductorRepository conductorRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService = new UsuarioService(usuarioRepository, conductorRepository);
    }

    @Test
    @DisplayName("Debería listar todos los usuarios")
    void testListarUsuarios() {
        // Arrange
        Usuario usuario1 = new Usuario();
        usuario1.setIdUsuario(1L);
        usuario1.setNombre("Juan");
        usuario1.setCorreo("juan@test.com");

        when(usuarioRepository.findAll()).thenReturn(java.util.List.of(usuario1));

        // Act
        var resultado = usuarioService.listarUsuarios();

        // Assert
        assertThat(resultado)
            .isNotEmpty()
            .hasSize(1)
            .contains(usuario1);

        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería buscar usuario por ID")
    void testBuscarPorId() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Juan");

        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuario));

        // Act
        var resultado = usuarioService.buscarPorId(1L);

        // Assert
        assertThat(resultado)
            .isPresent()
            .contains(usuario);
    }

    @Test
    @DisplayName("Debería guardar usuario correctamente")
    void testGuardarUsuario() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("Carlos");
        usuario.setCorreo("carlos@test.com");
        usuario.setContrasena("pass123");

        Rol rol = new Rol();
        rol.setNombre("ROLE_VENDEDOR");
        usuario.setRol(rol);

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(conductorRepository.findByUsername(anyString())).thenReturn(java.util.Optional.empty());

        // Act
        Usuario resultado = usuarioService.guardar(usuario);

        // Assert
        assertThat(resultado)
            .isNotNull()
            .extracting("nombre", "correo")
            .containsExactly("Carlos", "carlos@test.com");

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería fallar si el usuario es nulo")
    void testGuardarUsuarioNulo() {
        // Act & Assert
        assertThatThrownBy(() -> usuarioService.guardar(null))
            .isInstanceOf(Exception.class);
    }
}
