package com.example.demostracion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demostracion.model.PasswordResetToken;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.PasswordResetTokenRepository;
import com.example.demostracion.repository.UsuarioRepository;

@SuppressWarnings("null")
class RecuperacionContrasenaServiceTest {

    private RecuperacionContrasenaService recuperacionContrasenaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        recuperacionContrasenaService = new RecuperacionContrasenaService(
                usuarioRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                emailService);
    }

    @Test
    void deberiaGenerarCodigoYEnviarCorreoCuandoUsuarioExiste() {
        Usuario usuario = usuario(5L, "cliente@correo.com", "Cliente Demo");
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean resultado = recuperacionContrasenaService
            .solicitarRestablecimiento(" CLIENTE@correo.com ", "http://localhost:8081");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> cuerpoCorreo = ArgumentCaptor.forClass(String.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        verify(passwordResetTokenRepository).deleteByUsuarioIdUsuario(5L);
        verify(emailService).sendEmail(
                eq("cliente@correo.com"),
                contains("Código para restablecer tu contraseña"),
                cuerpoCorreo.capture(),
                isNull());

        PasswordResetToken tokenGuardado = captor.getValue();
        String cuerpo = cuerpoCorreo.getValue();
        assertThat(tokenGuardado.getUsuario()).isSameAs(usuario);
        assertThat(tokenGuardado.getTokenHash()).hasSize(64);
        assertThat(tokenGuardado.getFechaExpiracion()).isAfter(tokenGuardado.getFechaSolicitud());
        assertThat(tokenGuardado.getFechaUso()).isNull();
        assertThat(resultado).isTrue();
        assertThat(cuerpo)
            .contains("Código de verificación")
            .contains("10 minutos")
            .contains("http://localhost:8081/restablecer")
            .doesNotContain("token=")
            .containsPattern(">\\d{6}<");
    }

    @Test
    void noDeberiaHacerNadaCuandoElCorreoNoExiste() {
        when(usuarioRepository.findByCorreoIgnoreCase("inexistente@correo.com")).thenReturn(Optional.empty());

        boolean resultado = recuperacionContrasenaService
            .solicitarRestablecimiento("inexistente@correo.com", "http://localhost:8081");

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
        assertThat(resultado).isFalse();
    }

    @Test
    void deberiaValidarCodigoYRetornarIdTokenCuandoCoincide() {
        Usuario usuario = usuario(8L, "cliente@correo.com", "Cliente Demo");
        PasswordResetToken token = new PasswordResetToken();
        token.setIdPasswordResetToken(44L);
        token.setUsuario(usuario);
        token.setTokenHash(hash("123456"));
        token.setFechaSolicitud(LocalDateTime.now().minusMinutes(5));
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(5));

        when(usuarioRepository.findByCorreoIgnoreCase("cliente@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordResetTokenRepository.findTopByUsuarioIdUsuarioAndFechaUsoIsNullOrderByFechaSolicitudDesc(8L))
                .thenReturn(Optional.of(token));

        Long tokenId = recuperacionContrasenaService.validarCodigo("cliente@correo.com", "123456");

        assertThat(tokenId).isEqualTo(44L);
    }

    @Test
    void deberiaRestablecerContrasenaCuandoTokenAutorizadoSigueVigente() {
        Usuario usuario = usuario(8L, "cliente@correo.com", "Cliente Demo");
        PasswordResetToken token = new PasswordResetToken();
        token.setIdPasswordResetToken(88L);
        token.setUsuario(usuario);
        token.setTokenHash(hash("123456"));
        token.setFechaSolicitud(LocalDateTime.now().minusMinutes(4));
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(6));

        when(passwordResetTokenRepository.findById(88L)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Nueva1234")).thenReturn("HASH-NUEVO");

        recuperacionContrasenaService.restablecerContrasenaAutorizada(88L, "Nueva1234");

        assertThat(usuario.getContrasena()).isEqualTo("HASH-NUEVO");
        assertThat(token.getFechaUso()).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void deberiaRechazarContrasenaSinLetraONumero() {
        assertThatThrownBy(() -> recuperacionContrasenaService.validarPoliticaContrasena("abcdefgh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos una letra y un número");

        assertThatThrownBy(() -> recuperacionContrasenaService.validarPoliticaContrasena("12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos una letra y un número");
    }

    @Test
    void codigoEsValidoDeberiaRetornarFalsoCuandoExpira() {
        Usuario usuario = usuario(3L, "cliente@correo.com", "Cliente Demo");
        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setTokenHash(hash("123456"));
        token.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        when(usuarioRepository.findByCorreoIgnoreCase("cliente@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordResetTokenRepository.findTopByUsuarioIdUsuarioAndFechaUsoIsNullOrderByFechaSolicitudDesc(3L))
                .thenReturn(Optional.of(token));

        assertThat(recuperacionContrasenaService.codigoEsValido("cliente@correo.com", "123456")).isFalse();
    }

    @Test
    void deberiaRechazarCodigoConFormatoInvalido() {
        assertThatThrownBy(() -> recuperacionContrasenaService.validarCodigo("cliente@correo.com", "12A45"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("6 dígitos");
    }

    private Usuario usuario(Long id, String correo, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setCorreo(correo);
        usuario.setNombre(nombre);
        usuario.setActivo(true);
        return usuario;
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}