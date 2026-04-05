package com.example.demostracion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaTransactionManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Configuración de Transacciones
 * 
 * ISO 25010: Confiabilidad - Integridad
 * ✅ Asegura ACID en operaciones críticas
 * ✅ Previene estados inconsistentes
 * 
 * @author Sistema
 * @since 1.0
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * Gestor de transacciones JPA
     * Requiere que los services usen @Transactional
     */
    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        // Habilitar reintentos en caso de deadlock
        transactionManager.setRollbackOnCommitFailure(true);
        return transactionManager;
    }
}
