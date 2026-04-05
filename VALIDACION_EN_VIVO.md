# ✅ CHECKLIST DE VALIDACIÓN EN VIVO - ISO 25010

## 🚀 ANTES DE EMPEZAR

Asegúrate de que la app esté corriendo:
```bash
java -jar target/demostracion-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# O con Maven:
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
# O con Docker:
docker-compose up
```

La app debe estar en: **http://localhost:8081**

---

## 📋 **1. VALIDACIÓN DE SEGURIDAD**

### ✅ Test: Verificar que las credenciales NO están en el código

```bash
# Buscar contraseñas hardcodeadas
grep -r "password" src/main/resources/application.properties | grep -v ".example"
grep -r "autochecklistoficial" src/main/resources/application.properties

# Resultado esperado: VACÍO (sin coincidencias)
# ✅ Las credenciales deben estar solo en .env
```

### ✅ Test: BCrypt configurado

```bash
# Verificar que BCryptPasswordEncoder está en uso
curl -s http://localhost:8081/actuator/info | grep -i "bcrypt\|security"

# Buscar en código
grep -r "BCryptPasswordEncoder\|passwordEncoder" src/main/java/com/example/demostracion/config/SecurityConfig.java

# Resultado esperado: ContieneBCrypt
```

### ✅ Test: CSRF Protection

```bash
# 1. Acceder a un formulario y buscar token
curl -s http://localhost:8081/login | grep -i "csrf"

# 2. Intenta POST sin token (debería fallar)
curl -X POST http://localhost:8081/usuarios -d "nombre=test" 
# Resultado esperado: 403 Forbidden (CSRF validation failed)

# 3. Con token (debería funcionar)
TOKEN=$(curl -s http://localhost:8081/login | grep -o 'name="[^"]*token[^"]*"' | head -1)
# Luego POST con el token
```

### ✅ Test: Headers de Seguridad

```bash
# Verificar que los headers están presentes
curl -I http://localhost:8081/ | grep -i "X-Frame-Options\|X-XSS-Protection\|Strict-Transport-Security"

# Resultado esperado:
# X-Frame-Options: SAMEORIGIN
# X-XSS-Protection: 1; mode=block
# etc.
```

### ✅ Test: Transacciones ACID

```bash
# Verificar logs de transacción
grep -i "@Transactional" src/main/java/com/example/demostracion/service/*.java | wc -l

# Resultado esperado: > 5 servicios con @Transactional
```

---

## ⚡ **2. VALIDACIÓN DE RENDIMIENTO & CONFIABILIDAD**

### ✅ Test: Redis Cache

```bash
# 1. Verificar que Redis está conectado
curl -s http://localhost:8081/actuator/health/redis | jq .
# Resultado esperado: "status":"UP"

# 2. Verificar caché en estadísticas
curl -s http://localhost:8081/actuator/metrics/cache.hits | jq .
curl -s http://localhost:8081/actuator/metrics/cache.misses | jq .

# 3. Monitorear hits/misses
watch -n 1 "curl -s http://localhost:8081/actuator/metrics/cache.hits | jq '.measurements[0].value'"
```

### ✅ Test: Async Processing

```bash
# 1. Verificar que el pool de threads de email está configurado
curl -s http://localhost:8081/actuator/beans | grep -i "emailThreadPool"

# 2. Ver métricas de threading
curl -s http://localhost:8081/actuator/metrics/system.cpu.usage | jq .

# 3. Log debería mostrar: "Starting email-X thread"
```

### ✅ Test: Resilience4j (Reintentos)

```bash
# 1. Verificar configuración
grep -r "max-attempts\|wait-duration" src/main/resources/application-*.properties

# Resultado esperado:
# resilience4j.retry.configs.default.max-attempts=3
# resilience4j.retry.configs.default.wait-duration=1000

# 2. Forzar fallo para ver reintentos:
# (Desconecta BD o email y verifica los logs)
```

### ✅ Test: Health Checks

```bash
# Health general
curl -s http://localhost:8081/actuator/health | jq .

# Resultado esperado:
# {
#   "status": "UP",
#   "components": {
#     "database": {"status": "UP"},
#     "email": {"status": "UP"},
#     "redis": {"status": "UP"},
#     ...
#   }
# }

# Health específico de BD
curl -s http://localhost:8081/actuator/health/db | jq .

# Health específico de Email
curl -s http://localhost:8081/actuator/health/email | jq .
```

### ✅ Test: Performance (Tiempo de respuesta)

```bash
# Sin caché (primera vez)
time curl -s http://localhost:8081/vehiculos

# Con caché (segunda vez - debería ser más rápido)
time curl -s http://localhost:8081/vehiculos

# Resultado esperado: Segunda llamada ~10x más rápida
```

---

## 🧪 **3. VALIDACIÓN DE MANTENIBILIDAD**

### ✅ Test: Tests Unitarios

```bash
# Ejecutar tests
mvn test

# Resultado esperado:
# Tests informados: >= 40
# Tests exitosos: >= 35
# Failures: 0

# Ver cobertura
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### ✅ Test: Swagger/OpenAPI

```bash
# 1. Acceder a Swagger UI
# Abrir: http://localhost:8081/swagger-ui.html

# 2. Descargar JSON de API
curl -s http://localhost:8081/v3/api-docs | jq '.paths | keys'

# Resultado esperado: Lista de todos los endpoints documentados
```

### ✅ Test: Constantes Centralizadas

```bash
# Verificar que existen
[ -f src/main/java/com/example/demostracion/config/AppConstants.java ] && echo "✓ AppConstants existe"

# Contar constantes
grep -c "public static final" src/main/java/com/example/demostracion/config/AppConstants.java

# Resultado esperado: > 30 constantes
```

### ✅ Test: Auditoría

```bash
# 1. Verificar tabla audit_log
mysql -u root -p auto -e "SELECT COUNT(*) FROM audit_log;"

# 2. Ver último registro
mysql -u root -p auto -e "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 1\G"

# Resultado esperado: Tabla con registros de operaciones
```

---

## 📡 **4. VALIDACIÓN DE COMPATIBILIDAD**

### ✅ Test: Swagger API

```bash
# Verificar disponibilidad
curl -s -o /dev/null -w "HTTP %{http_code}" http://localhost:8081/swagger-ui.html

# Resultado esperado: HTTP 200
```

### ✅ Test: Endpoints REST

```bash
# Verificar principales endpoints
curl -s http://localhost:8081/api/v1/usuarios | jq . 2>/dev/null && echo "✓ API REST funciona"

# Resultado esperado: JSON válido
```

---

## 📦 **5. VALIDACIÓN DE PORTABILIDAD**

### ✅ Test: Docker

```bash
# 1. Verificar Dockerfile
docker build -t concesionario:test --no-cache . 2>&1 | tail -5

# Resultado esperado: Successfully tagged concesionario:test

# 2. Verificar docker-compose
docker-compose config > /dev/null && echo "✓ docker-compose válido"

# 3. Levantar con Docker
docker-compose up -d
sleep 10
docker-compose ps

# Resultado esperado: 3 contenedores corriendo (MySQL, Redis, App)
```

### ✅ Test: Perfiles Spring

```bash
# Dev profile debería tener DEBUG logs
java -jar app.jar --spring.profiles.active=dev &
sleep 5
grep -i "DEBUG\|TRACE" logs/app.log | wc -l
# Resultado esperado: > 100 líneas de DEBUG

# Prod profile debería tener WARN logs
java -jar app.jar --spring.profiles.active=prod &
sleep 5
grep -i "WARN\|ERROR" logs/app.log | wc -l
# Resultado esperado: < 20 líneas de WARN (más silencioso)
```

---

## 📊 **6. MÉTRICAS COMPLETAS**

```bash
# Obtener todas las métricas
curl -s http://localhost:8081/actuator/metrics | jq '.names | length'

# Ver métrica específica (CPU)
curl -s http://localhost:8081/actuator/metrics/system.cpu.usage | jq '.measurements'

# Ver métrica de memoria
curl -s http://localhost:8081/actuator/metrics/jvm.memory.used | jq '.measurements'

# Ver métrica HTTP requests
curl -s http://localhost:8081/actuator/metrics/http.server.requests | jq '.measurements'
```

---

## 🔍 **7. CHECKLIST RÁPIDO (5 minutos)**

```bash
#!/bin/bash
echo "╔════════════════════════════════════════════╗"
echo "║   VALIDACIÓN RÁPIDA ISO 25010             ║"
echo "╚════════════════════════════════════════════╝"
echo ""

# 1. Health
echo "1. Health Check..."
STATUS=$(curl -s http://localhost:8081/actuator/health | jq -r '.status')
[ "$STATUS" = "UP" ] && echo "✅ PASS" || echo "❌ FAIL"

# 2. Swagger
echo "2. Swagger Available..."
SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/swagger-ui.html)
[ "$SWAGGER" = "200" ] && echo "✅ PASS" || echo "❌ FAIL"

# 3. Redis
echo "3. Redis Connected..."
REDIS=$(curl -s http://localhost:8081/actuator/health/redis | jq -r '.status')
[ "$REDIS" = "UP" ] && echo "✅ PASS" || echo "❌ FAIL"

# 4. Database
echo "4. Database Connected..."
DB=$(curl -s http://localhost:8081/actuator/health/db | jq -r '.status')
[ "$DB" = "UP" ] && echo "✅ PASS" || echo "❌ FAIL"

# 5. Tests
echo "5. Running Tests..."
mvn test -q 2>/dev/null && echo "✅ PASS" || echo "❌ FAIL"

echo ""
echo "✅ SI TODO ES PASS = CUMPLE ISO 25010"
```

---

## 🏆 **RESULTADO ESPERADO FINAL**

```
╔════════════════════════════════════════════════════════════╗
║                   ISO 25010 VALIDADO                       ║
╠════════════════════════════════════════════════════════════╣
║ 1. SEGURIDAD              ✅ 100%  (7/7)                   ║
║ 2. CONFIABILIDAD         ✅ 100%  (5/5)                   ║
║ 3. USABILIDAD/COMPAT      ✅ 100%  (3/3)                   ║
║ 4. MANTENIBILIDAD        ✅ 100%  (4/4)                   ║
║ 5. PORTABILIDAD          ✅ 100%  (4/4)                   ║
╠════════════════════════════════════════════════════════════╣
║ CONFORMIDAD TOTAL:  97% ✅  LISTO PARA PRODUCCIÓN         ║
╚════════════════════════════════════════════════════════════╝
```

---

## 🆘 Si algo falla

1. **Revisa logs**: `docker-compose logs app`
2. **Verifica servicios**: `docker-compose ps`
3. **Health endpoint**: http://localhost:8081/actuator/health
4. **Reinicia todo**: `docker-compose down && docker-compose up`

¡LISTO! 🚀
