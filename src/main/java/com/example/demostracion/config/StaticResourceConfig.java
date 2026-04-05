package com.example.demostracion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve archivos de /uploads/** desde la carpeta física uploads/ en la raíz del proyecto
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
