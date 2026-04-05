package com.example.demostracion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración de ejecución asíncrona
 * 
 * ISO 25010: Rendimiento - Comportamiento temporal
 * ✅ Procesa correos sin bloquear la respuesta HTTP
 * ✅ Mejora velocidad de respuesta a usuario
 * 
 * @author Sistema
 * @since 1.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Thread pool para envío de correos
     * Threads: 5 core, máximo 10, cola: 1000 tareas
     */
    @Bean(name = AppConstants.EMAIL_POOL_NAME)
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(AppConstants.EMAIL_THREAD_POOL_SIZE);
        executor.setMaxPoolSize(AppConstants.EMAIL_THREAD_POOL_SIZE * 2);
        executor.setQueueCapacity(AppConstants.EMAIL_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool para operaciones generales asincronicas
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}