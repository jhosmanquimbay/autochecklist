package com.example.demostracion.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.demostracion.dto.RecuperarContrasenaForm;
import com.example.demostracion.service.RecuperacionContrasenaService;

@SuppressWarnings("null")
class RecuperacionContrasenaControllerTest {

    @Mock
    private RecuperacionContrasenaService recuperacionContrasenaService;

    private RecuperacionContrasenaController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new RecuperacionContrasenaController(recuperacionContrasenaService);
    }

    @Test
    void deberiaMostrarErrorCuandoElCorreoNoExiste() {
        RecuperarContrasenaForm form = new RecuperarContrasenaForm();
        form.setCorreo("cliente-no-existe-prueba@gmail.com");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "recuperarForm");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/recuperar");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8081);

        MockHttpSession session = new MockHttpSession();
        ConcurrentModel model = new ConcurrentModel();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(recuperacionContrasenaService.solicitarRestablecimiento(
                "cliente-no-existe-prueba@gmail.com",
                "http://localhost:8081")).thenReturn(false);

        String vista = controller.solicitarRestablecimiento(
                form,
                bindingResult,
                request,
                session,
                model,
                redirectAttributes);

        assertThat(vista).isEqualTo("recuperar");
        assertThat(model.getAttribute("errorEnvio"))
                .isEqualTo("No encontramos una cuenta activa con ese correo. Verifica el correo registrado del cliente.");
        assertThat(session.getAttribute("passwordResetPendingEmail")).isNull();
    }

    @Test
    void deberiaRedirigirARestablecerCuandoElCorreoExiste() {
        RecuperarContrasenaForm form = new RecuperarContrasenaForm();
        form.setCorreo("cliente.prueba.autologin@gmail.com");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "recuperarForm");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/recuperar");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8081);

        MockHttpSession session = new MockHttpSession();
        ConcurrentModel model = new ConcurrentModel();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(recuperacionContrasenaService.solicitarRestablecimiento(
                "cliente.prueba.autologin@gmail.com",
                "http://localhost:8081")).thenReturn(true);
        when(recuperacionContrasenaService.obtenerCorreoEnmascaradoPorCorreo("cliente.prueba.autologin@gmail.com"))
                .thenReturn("cl***@gma***");
        when(recuperacionContrasenaService.obtenerExpiracionMinutos()).thenReturn(10);

        String vista = controller.solicitarRestablecimiento(
                form,
                bindingResult,
                request,
                session,
                model,
                redirectAttributes);

        assertThat(vista).isEqualTo("redirect:/restablecer");
        assertThat(session.getAttribute("passwordResetPendingEmail"))
                .isEqualTo("cliente.prueba.autologin@gmail.com");
        assertThat(redirectAttributes.getFlashAttributes().get("codigoEnviado")).isEqualTo(Boolean.TRUE);
        assertThat(redirectAttributes.getFlashAttributes().get("correoDestino")).isEqualTo("cl***@gma***");
    }
}