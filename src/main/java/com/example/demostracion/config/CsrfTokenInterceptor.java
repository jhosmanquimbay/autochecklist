package com.example.demostracion.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor para inyectar tokens CSRF en response
 * 
 * @author Sistema
 * @since 1.0
 */
public class CsrfTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // El token CSRF es inyectado automáticamente por Spring Security
        // Este interceptor asegura que esté disponible en la Response
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            response.addHeader("X-CSRF-TOKEN", token.getToken());
        }
        return true;
    }
}
