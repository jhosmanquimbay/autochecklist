package com.example.demostracion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demostracion.exception.EmailException;
import com.example.demostracion.model.Mensaje;
import com.example.demostracion.model.Rol;
import com.example.demostracion.model.Usuario;
import com.example.demostracion.repository.AdjuntoRepository;
import com.example.demostracion.repository.MensajeRepository;
import com.example.demostracion.repository.UsuarioRepository;

@DisplayName("MensajeService - Unit Tests")
class MensajeServiceTest {

    private MensajeService mensajeService;

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mensajeService = new MensajeService(mensajeRepository, usuarioRepository, adjuntoRepository, emailService);
    }

    @Test
    @DisplayName("Debe conservar la carpeta original al mover a papelera")
    void debeConservarCarpetaOriginalAlMoverAPapelera() {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(10L);
        mensaje.setCarpeta("sent");
        mensaje.setEliminado(false);

        when(mensajeRepository.findById(10L)).thenReturn(Optional.of(mensaje));

        mensajeService.moverAPapelera(10L);

        assertThat(mensaje.getCarpeta()).isEqualTo("sent");
        assertThat(mensaje.isEliminado()).isTrue();
        verify(mensajeRepository).save(mensaje);
    }

    @Test
    @DisplayName("Debe restaurar un enviado a la carpeta enviados")
    void debeRestaurarEnviadoASent() {
        Usuario remitente = crearUsuarioInterno(1L, "admin@demo.com", "ROLE_ADMIN");
        Usuario destinatario = crearUsuarioInterno(2L, "gerente@demo.com", "ROLE_GERENTE");

        Mensaje mensaje = new Mensaje();
        mensaje.setId(15L);
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setCarpeta("trash");
        mensaje.setEliminado(true);

        when(mensajeRepository.findById(15L)).thenReturn(Optional.of(mensaje));

        mensajeService.restaurarDePapelera(15L, 1L);

        assertThat(mensaje.isEliminado()).isFalse();
        assertThat(mensaje.getCarpeta()).isEqualTo("sent");
        verify(mensajeRepository).save(mensaje);
    }

    @Test
    @DisplayName("Debe crear copia en inbox al responder un mensaje interno")
    void debeCrearCopiaEnInboxAlResponderInterno() {
        Usuario remitenteOriginal = crearUsuarioInterno(1L, "admin@demo.com", "ROLE_ADMIN");
        Usuario usuarioActual = crearUsuarioInterno(2L, "gerente@demo.com", "ROLE_GERENTE");

        Mensaje original = new Mensaje();
        original.setId(20L);
        original.setRemitente(remitenteOriginal);
        original.setDestinatario(usuarioActual);
        original.setAsunto("Seguimiento");

        when(mensajeRepository.findById(20L)).thenReturn(Optional.of(original));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioActual));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mensajeService.responder(20L, 2L, "Respuesta interna", null);

        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository, times(2)).save(captor.capture());

        List<Mensaje> guardados = captor.getAllValues();
        assertThat(guardados).hasSize(2);
        assertThat(guardados.get(0).getCarpeta()).isEqualTo("sent");
        assertThat(guardados.get(1).getCarpeta()).isEqualTo("inbox");
        assertThat(guardados.get(0).getDestinatario()).isEqualTo(remitenteOriginal);
        assertThat(guardados.get(1).getDestinatario()).isEqualTo(remitenteOriginal);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Debe enviar por SMTP al responder un mensaje externo")
    void debeEnviarPorSmtpAlResponderExterno() {
        Usuario externo = crearUsuarioExterno(3L, "externo@demo.com");
        Usuario usuarioActual = crearUsuarioInterno(2L, "gerente@demo.com", "ROLE_GERENTE");

        Mensaje original = new Mensaje();
        original.setId(30L);
        original.setRemitente(externo);
        original.setDestinatario(usuarioActual);
        original.setAsunto("Consulta");

        when(mensajeRepository.findById(30L)).thenReturn(Optional.of(original));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioActual));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mensajeService.responder(30L, 2L, "Respuesta externa", null);

        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository).save(captor.capture());
        assertThat(captor.getValue().getCarpeta()).isEqualTo("sent");
        assertThat(captor.getValue().getDestinatario()).isNull();
        assertThat(captor.getValue().getDestinatarioExterno()).isEqualTo("externo@demo.com");
        verify(emailService).sendEmail(eq("externo@demo.com"), eq("Re: Consulta"), eq("Respuesta externa"), any());
    }

    @Test
    @DisplayName("Debe guardar en enviados aunque falle SMTP externo")
    void debeGuardarEnEnviadosAunqueFalleSmtpExterno() {
        Usuario remitente = crearUsuarioInterno(1L, "admin@demo.com", "ROLE_ADMIN");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remitente));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new EmailException("SMTP no disponible"))
                .when(emailService)
                .sendEmail(eq("externo@demo.com"), eq("Asunto"), eq("Contenido"), any());

        try {
            mensajeService.enviarAExterna(1L, "externo@demo.com", "Asunto", "Contenido", null);
        } catch (EmailException ex) {
            // esperado
        }

        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository).save(captor.capture());
        assertThat(captor.getValue().getCarpeta()).isEqualTo("sent");
        assertThat(captor.getValue().getDestinatarioExterno()).isEqualTo("externo@demo.com");
    }

    private Usuario crearUsuarioInterno(Long id, String correo, String nombreRol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setCorreo(correo);

        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        usuario.setRol(rol);
        return usuario;
    }

    private Usuario crearUsuarioExterno(Long id, String correo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setCorreo(correo);
        return usuario;
    }
}