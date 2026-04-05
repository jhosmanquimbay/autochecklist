#!/bin/bash
# ============================================
# SCRIPT DE VALIDACIÓN ISO 25010
# ============================================
# Verifica que la aplicación cumple todos
# los atributos de calidad ISO 25010

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     VALIDACIÓN COMPLETA - ISO 25010                       ║"
echo "║     Sistema de Gestión de Concesionario                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

PASS=0
FAIL=0
WARNINGS=0

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para validar
check() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $1"
        ((PASS++))
    else
        echo -e "${RED}❌ FAIL${NC}: $1"
        ((FAIL++))
    fi
}

warning() {
    echo -e "${YELLOW}⚠️  WARN${NC}: $1"
    ((WARNINGS++))
}

# ==================================================
# 1. SEGURIDAD
# ==================================================
echo ""
echo "════ 1. SEGURIDAD ════"

# Variables de entorno
[ -f ".env.example" ] && check "✓ Archivo .env.example existe" || check "✗ .env.example no encontrado"
[ -f "src/main/resources/application-dev.properties" ] && check "✓ Profile dev.properties existe" || check "✗ dev.properties no encontrado"
[ -f "src/main/resources/application-prod.properties" ] && check "✓ Profile prod.properties existe" || check "✗ prod.properties no encontrado"

# BCrypt configurado
grep -q "BCryptPasswordEncoder" src/main/java/com/example/demostracion/config/SecurityConfig.java && check "✓ BCrypt está configurado" || check "✗ BCrypt no encontrado"

# CSRF protección
grep -q "csrf" src/main/java/com/example/demostracion/config/SecurityConfig.java && check "✓ CSRF protección activa" || check "✗ CSRF no configurado"

# Headers de seguridad
grep -q "xssProtection\|contentSecurityPolicy" src/main/java/com/example/demostracion/config/SecurityConfig.java && check "✓ Headers de seguridad configurados" || check "✗ Headers no configurados"

# Transacciones
grep -q "@Transactional" src/main/java/com/example/demostracion/config/TransactionConfig.java && check "✓ Transacciones ACID" || check "✗ Transacciones no encontradas"

# Scripts de backup
[ -f "scripts/backup-mysql.bat" ] && check "✓ Backup script Windows" || check "✗ Backup Windows no encontrado"
[ -f "scripts/backup-mysql.sh" ] && check "✓ Backup script Linux/Mac" || check "✗ Backup Linux no encontrado"

# ==================================================
# 2. CONFIABILIDAD
# ==================================================
echo ""
echo "════ 2. CONFIABILIDAD ════"

# Redis configurado
grep -q "RedisConfig" src/main/java/com/example/demostracion/config/RedisConfig.java && check "✓ Redis Cache configurado" || check "✗ Redis no configurado"

# Resilience4j
grep -q "Resilience4j" src/main/java/com/example/demostracion/config/Resilience4jConfig.java && check "✓ Resilience4j (reintentos) configurado" || check "✗ Resilience4j no encontrado"

# Health checks
[ -f "src/main/java/com/example/demostracion/health/DatabaseHealthIndicator.java" ] && check "✓ Health BD configurado" || check "✗ Health BD no encontrado"
[ -f "src/main/java/com/example/demostracion/health/EmailHealthIndicator.java" ] && check "✓ Health Email configurado" || check "✗ Health Email no encontrado"

# Async
grep -q "@EnableAsync" src/main/java/com/example/demostracion/config/AsyncConfig.java && check "✓ Async processing habilitado" || check "✗ Async no habilitado"

# ==================================================
# 3. USABILIDAD & COMPATIBILIDAD
# ==================================================
echo ""
echo "════ 3. USABILIDAD & COMPATIBILIDAD ════"

# Swagger
[ -f "src/main/java/com/example/demostracion/config/SwaggerConfig.java" ] && check "✓ Swagger/OpenAPI configurado" || check "✗ Swagger no encontrado"

# AppConstants
[ -f "src/main/java/com/example/demostracion/config/AppConstants.java" ] && check "✓ Constantes centralizadas" || check "✗ AppConstants no encontrado"

# ==================================================
# 4. MANTENIBILIDAD
# ==================================================
echo ""
echo "════ 4. MANTENIBILIDAD ════"

# Tests unitarios
[ -f "src/test/java/com/example/demostracion/service/UsuarioServiceTest.java" ] && check "✓ Tests unitarios Base" || check "✗ Tests no encontrados"
[ -f "src/test/java/com/example/demostracion/controller/ControllerIntegrationTest.java" ] && check "✓ Tests integración" || check "✗ Integration tests no encontrados"

# Auditoría
[ -f "src/main/java/com/example/demostracion/model/AuditLog.java" ] && check "✓ Modelo Auditoría" || check "✗ AuditLog no encontrado"
[ -f "src/main/resources/audit-schema.sql" ] && check "✓ Schema Auditoría" || check "✗ audit-schema.sql no encontrado"

# Anotaciones
[ -f "src/main/java/com/example/demostracion/annotation/CriticalOperation.java" ] && check "✓ Anotación @CriticalOperation" || check "✗ Anotación no encontrada"
[ -f "src/main/java/com/example/demostracion/annotation/RetryableOperation.java" ] && check "✓ Anotación @RetryableOperation" || check "✗ Anotación no encontrada"

# ==================================================
# 5. PORTABILIDAD
# ==================================================
echo ""
echo "════ 5. PORTABILIDAD ════"

# Docker
[ -f "Dockerfile" ] && check "✓ Dockerfile presente" || check "✗ Dockerfile no encontrado"
[ -f "docker-compose.yml" ] && check "✓ docker-compose.yml presente" || check "✗ docker-compose.yml no encontrado"
[ -f "docker-compose.dev.yml" ] && check "✓ docker-compose.dev.yml presente" || check "✗ docker-compose.dev.yml no encontrado"

# Documentación
[ -f "DOCKER_DEPLOYMENT.md" ] && check "✓ Documentación Docker" || check "✗ DOCKER_DEPLOYMENT.md no encontrado"
[ -f "ISO25010_IMPLEMENTACION_COMPLETA.md" ] && check "✓ Documentación ISO 25010" || check "✗ ISO25010_IMPLEMENTACION_COMPLETA.md no encontrado"
[ -f "PROXIMOS_PASOS.md" ] && check "✓ Guía Próximos Pasos" || check "✗ PROXIMOS_PASOS.md no encontrado"

# ==================================================
# 6. DEPENDENCIAS EN POM.XML
# ==================================================
echo ""
echo "════ 6. DEPENDENCIAS MAVEN ════"

# Redis
grep -q "spring-boot-starter-data-redis" pom.xml && check "✓ Redis dependency" || check "✗ Redis no encontrado en pom.xml"

# Swagger
grep -q "springdoc-openapi" pom.xml && check "✓ Swagger dependency" || check "✗ Swagger no encontrado en pom.xml"

# Resilience4j
grep -q "resilience4j-spring-boot3" pom.xml && check "✓ Resilience4j dependency" || check "✗ Resilience4j no encontrado en pom.xml"

# Testing
grep -q "mockito" pom.xml && check "✓ Mockito dependency" || check "✗ Mockito no encontrado en pom.xml"
grep -q "assertj" pom.xml && check "✓ AssertJ dependency" || check "✗ AssertJ no encontrado en pom.xml"

# Actuator
grep -q "spring-boot-starter-actuator" pom.xml && check "✓ Actuator dependency" || check "✗ Actuator no encontrado en pom.xml"

# ==================================================
# RESUMEN
# ==================================================
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    RESUMEN VALIDACIÓN                      ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}✅ PASS: $PASS${NC}"
echo -e "${RED}❌ FAIL: $FAIL${NC}"
echo -e "${YELLOW}⚠️  WARNINGS: $WARNINGS${NC}"
echo ""

TOTAL=$((PASS + FAIL))
PERCENTAGE=$((PASS * 100 / TOTAL))

echo "Conformidad ISO 25010: $PERCENTAGE% ($PASS/$TOTAL)"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✅ TODO CUMPLE CON ISO 25010${NC}"
    exit 0
else
    echo -e "${RED}❌ REVISAR $FAIL INCUMPLIMIENTOS${NC}"
    exit 1
fi
