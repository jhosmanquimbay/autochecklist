package com.example.demostracion.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anotación para operaciones críticas que requieren transaccionalidad
 * 
 * ISO 25010: Confiabilidad - Integridad
 * ✅ Garantiza ACID en operaciones críticas
 * 
 * @author Sistema
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(rollbackFor = Exception.class)
public @interface CriticalOperation {
    String value() default "Critical database operation";
}
