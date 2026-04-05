package com.example.demostracion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

/**
 * Configuración de CSRF y Seguridad adicional
 * 
 * ISO 25010: Seguridad - Integridad
 * ✅ Protege contra ataques CSRF
 * ✅ Valida origen de solicitudes
 * 
 * @author Sistema
 * @since 1.0
 */
@Configuration
public class CsrfSecurityConfig implements WebMvcConfigurer {

    /**
     * Repositorio de tokens CSRF
     * Almacena CSRF tokens en sesión HTTP
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setParameterName(AppConstants.CSRF_HEADER_NAME);
        repository.setHeaderName(AppConstants.CSRF_HEADER_NAME);
        return repository;
    }

    /**
     * Registra interceptores para aplicar CSRF tokens en Thymeleaf
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CsrfTokenInterceptor());
    }
}
