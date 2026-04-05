package com.example.demostracion.config;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Resilience4j para tolerancia a fallos
 * 
 * ISO 25010: Confiabilidad - Tolerancia a fallos
 * ✅ Circuit Breaker para detectar servicios caídos
 * ✅ Reintentos automáticos para fallos transitorios
 * 
 * @author Sistema
 * @since 1.0
 */
// @Configuration
public class Resilience4jConfig {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jConfig.class);

    /**
     * Configurar eventos del registry de Retry
     */
    /*
    @Bean
    public RegistryEventConsumer<Retry> retryRegistryEventConsumer() {
        return new RegistryEventConsumer<Retry>() {
            @Override
            public void onEntryAdded(EntryAddedEvent<Retry> event) {
                Retry retry = event.getAddedEntry();
                logger.info("Retry agregado: {} con {} intentos", 
                    retry.getName(), 
                    retry.getRetryConfig().getMaxAttempts());
            }

            @Override
            public void onEntryRemoved(EntryRemovedEvent<Retry> event) {
                logger.info("Retry removido: {}", event.getRemovedEntry().getName());
            }
        };
    }
    */

    /**
     * Configurar eventos del registry de CircuitBreaker
     */
    /*
    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAdded(EntryAddedEvent<CircuitBreaker> event) {
                CircuitBreaker cb = event.getAddedEntry();
                logger.info("CircuitBreaker agregado: {} - Umbral: {}", 
                    cb.getName(), 
                    cb.getCircuitBreakerConfig().getFailureRateThreshold());
            }

            @Override
            public void onEntryRemoved(EntryRemovedEvent<CircuitBreaker> event) {
                logger.info("CircuitBreaker removido: {}", event.getRemovedEntry().getName());
            }
        };
    }
    */
}
