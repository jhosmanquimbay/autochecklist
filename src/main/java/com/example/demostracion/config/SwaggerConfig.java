package com.example.demostracion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI
 * 
 * ISO 25010: Compatibilidad - Interoperabilidad
 * ✅ Documentación automática de APIs
 * ✅ Facilita integración con sistemas terceros
 * 
 * Acceso: http://localhost:8081/swagger-ui.html
 * JSON: http://localhost:8081/v3/api-docs
 * 
 * @author Sistema
 * @since 1.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API - Gestión de Concesionario")
                .version(AppConstants.APP_VERSION)
                .description("API REST para gestión de vehículos, usuarios y correos en concesionario")
                .contact(new Contact()
                    .name("Sistema")
                    .url("http://localhost:8081")
                    .email("admin@concesionario.local")
                )
                .license(new License()
                    .name("Licencia Interna")
                    .url("http://localhost:8081/license")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Bearer token para autenticación")
                )
            );
    }
}
