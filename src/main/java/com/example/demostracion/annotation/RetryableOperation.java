package com.example.demostracion.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Anotación para operaciones que deben reintentarse en caso de fallo
 * 
 * ISO 25010: Confiabilidad - Tolerancia a fallos
 * ✅ Reintentos automáticos para operaciones de correo
 * ✅ Manejo de excepciones transitorias
 * 
 * @author Sistema
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Retry(name = "default")
public @interface RetryableOperation {
    String value() default "Operation with retry capability";
    int maxAttempts() default 3;
    long delayMs() default 1000;
}
