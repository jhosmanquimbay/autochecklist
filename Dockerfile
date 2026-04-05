# Multi-stage build para optimizar imagen
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

# Compilar aplicación
RUN mvn clean package -DskipTests

# Stage final
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="sistema@concesionario.local"
LABEL version="1.0.0"
LABEL description="Aplicación de Gestión de Concesionario"

WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /build/target/demostracion-0.0.1-SNAPSHOT.jar app.jar

# Usuario no-root por seguridad
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Puertos: 8081 (app), 8009 (AJP si es necesario)
EXPOSE 8081

# Variables de entorno
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
