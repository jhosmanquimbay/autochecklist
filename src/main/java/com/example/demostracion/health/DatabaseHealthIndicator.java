package com.example.demostracion.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Indicador de salud de Base de Datos
 * 
 * ISO 25010: Confiabilidad - Disponibilidad
 * ✅ Monitoreo continuo de conexión a BD
 * 
 * Acceso: GET /actuator/health
 * 
 * @author Sistema
 * @since 1.0
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DataSource dataSource;

    @Override
    public Health health() {
        if (dataSource == null) {
            return Health.down()
                .withDetail("error", "DataSource no configurado")
                .build();
        }

        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "Conectada")
                    .build();
            } else {
                return Health.down()
                    .withDetail("error", "Conexión no válida")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
