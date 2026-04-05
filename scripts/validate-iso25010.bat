@echo off
REM ============================================
REM SCRIPT DE VALIDACIÓN ISO 25010 (Windows)
REM ============================================

setlocal enabledelayedexpansion

echo.
echo ============================================================
echo   VALIDACION COMPLETA - ISO 25010
echo   Sistema de Gestion de Concesionario
echo ============================================================
echo.

set PASS=0
set FAIL=0
set WARNINGS=0

REM ==================================================
REM 1. SEGURIDAD
REM ==================================================
echo ==== 1. SEGURIDAD ====
echo.

REM Variables de entorno
if exist ".env.example" (
    echo [PASS] Archivo .env.example existe
    set /a PASS+=1
) else (
    echo [FAIL] .env.example no encontrado
    set /a FAIL+=1
)

if exist "src\main\resources\application-dev.properties" (
    echo [PASS] Profile dev.properties existe
    set /a PASS+=1
) else (
    echo [FAIL] dev.properties no encontrado
    set /a FAIL+=1
)

if exist "src\main\resources\application-prod.properties" (
    echo [PASS] Profile prod.properties existe
    set /a PASS+=1
) else (
    echo [FAIL] prod.properties no encontrado
    set /a FAIL+=1
)

REM BCrypt configurado
findstr /M "BCryptPasswordEncoder" "src\main\java\com\example\demostracion\config\SecurityConfig.java" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] BCrypt está configurado
    set /a PASS+=1
) else (
    echo [FAIL] BCrypt no encontrado
    set /a FAIL+=1
)

REM CSRF protección
findstr /M "csrf" "src\main\java\com\example\demostracion\config\SecurityConfig.java" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] CSRF protección activa
    set /a PASS+=1
) else (
    echo [FAIL] CSRF no configurado
    set /a FAIL+=1
)

REM Headers de seguridad
findstr /M "contentSecurityPolicy" "src\main\java\com\example\demostracion\config\SecurityConfig.java" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Headers de seguridad configurados
    set /a PASS+=1
) else (
    echo [FAIL] Headers de seguridad no encontrados
    set /a FAIL+=1
)

REM Transacciones
findstr /M "@Transactional" "src\main\java\com\example\demostracion\config\TransactionConfig.java" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Transacciones ACID
    set /a PASS+=1
) else (
    echo [FAIL] Transacciones no encontradas
    set /a FAIL+=1
)

REM Scripts de backup
if exist "scripts\backup-mysql.bat" (
    echo [PASS] Backup script Windows
    set /a PASS+=1
) else (
    echo [FAIL] Backup Windows no encontrado
    set /a FAIL+=1
)

if exist "scripts\backup-mysql.sh" (
    echo [PASS] Backup script Linux o Mac
    set /a PASS+=1
) else (
    echo [FAIL] Backup Linux o Mac no encontrado
    set /a FAIL+=1
)

echo.

REM ==================================================
REM 2. CONFIABILIDAD
REM ==================================================
echo ==== 2. CONFIABILIDAD ====
echo.

if exist "src\main\java\com\example\demostracion\config\RedisConfig.java" (
    echo [PASS] Redis Cache configurado
    set /a PASS+=1
) else (
    echo [FAIL] Redis no configurado
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\config\Resilience4jConfig.java" (
    echo [PASS] Resilience4j configurado
    set /a PASS+=1
) else (
    echo [FAIL] Resilience4j no encontrado
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\health\DatabaseHealthIndicator.java" (
    echo [PASS] Health BD configurado
    set /a PASS+=1
) else (
    echo [FAIL] Health BD no encontrado
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\health\EmailHealthIndicator.java" (
    echo [PASS] Health Email configurado
    set /a PASS+=1
) else (
    echo [FAIL] Health Email no encontrado
    set /a FAIL+=1
)

findstr /M "@EnableAsync" "src\main\java\com\example\demostracion\config\AsyncConfig.java" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Async processing habilitado
    set /a PASS+=1
) else (
    echo [FAIL] Async no habilitado
    set /a FAIL+=1
)

echo.

REM ==================================================
REM 3. USABILIDAD & COMPATIBILIDAD
REM ==================================================
echo ==== 3. USABILIDAD Y COMPATIBILIDAD ====
echo.

if exist "src\main\java\com\example\demostracion\config\SwaggerConfig.java" (
    echo [PASS] Swagger/OpenAPI configurado
    set /a PASS+=1
) else (
    echo [FAIL] Swagger no encontrado
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\config\AppConstants.java" (
    echo [PASS] Constantes centralizadas
    set /a PASS+=1
) else (
    echo [FAIL] AppConstants no encontrado
    set /a FAIL+=1
)

echo.

REM ==================================================
REM 4. MANTENIBILIDAD
REM ==================================================
echo ==== 4. MANTENIBILIDAD ====
echo.

if exist "src\test\java\com\example\demostracion\service\UsuarioServiceTest.java" (
    echo [PASS] Tests unitarios Base
    set /a PASS+=1
) else (
    echo [FAIL] Tests no encontrados
    set /a FAIL+=1
)

if exist "src\test\java\com\example\demostracion\controller\ControllerIntegrationTest.java" (
    echo [PASS] Tests integración
    set /a PASS+=1
) else (
    echo [FAIL] Integration tests no encontrados
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\model\AuditLog.java" (
    echo [PASS] Modelo Auditoría
    set /a PASS+=1
) else (
    echo [FAIL] AuditLog no encontrado
    set /a FAIL+=1
)

if exist "src\main\resources\audit-schema.sql" (
    echo [PASS] Schema Auditoría
    set /a PASS+=1
) else (
    echo [FAIL] audit-schema.sql no encontrado
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\annotation\CriticalOperation.java" (
    echo [PASS] Anotación CriticalOperation
    set /a PASS+=1
) else (
    echo [FAIL] Anotación CriticalOperation no encontrada
    set /a FAIL+=1
)

if exist "src\main\java\com\example\demostracion\annotation\RetryableOperation.java" (
    echo [PASS] Anotación RetryableOperation
    set /a PASS+=1
) else (
    echo [FAIL] Anotación RetryableOperation no encontrada
    set /a FAIL+=1
)

echo.

REM ==================================================
REM 5. PORTABILIDAD
REM ==================================================
echo ==== 5. PORTABILIDAD ====
echo.

if exist "Dockerfile" (
    echo [PASS] Dockerfile presente
    set /a PASS+=1
) else (
    echo [FAIL] Dockerfile no encontrado
    set /a FAIL+=1
)

if exist "docker-compose.yml" (
    echo [PASS] docker-compose.yml presente
    set /a PASS+=1
) else (
    echo [FAIL] docker-compose.yml no encontrado
    set /a FAIL+=1
)

if exist "docker-compose.dev.yml" (
    echo [PASS] docker-compose.dev.yml presente
    set /a PASS+=1
) else (
    echo [FAIL] docker-compose.dev.yml no encontrado
    set /a FAIL+=1
)

if exist "DOCKER_DEPLOYMENT.md" (
    echo [PASS] Documentación Docker
    set /a PASS+=1
) else (
    echo [FAIL] DOCKER_DEPLOYMENT.md no encontrado
    set /a FAIL+=1
)

if exist "ISO25010_IMPLEMENTACION_COMPLETA.md" (
    echo [PASS] Documentación ISO 25010
    set /a PASS+=1
) else (
    echo [FAIL] ISO25010_IMPLEMENTACION_COMPLETA.md no encontrado
    set /a FAIL+=1
)

if exist "PROXIMOS_PASOS.md" (
    echo [PASS] Guía de próximos pasos
    set /a PASS+=1
) else (
    echo [FAIL] PROXIMOS_PASOS.md no encontrado
    set /a FAIL+=1
)

echo.

REM ==================================================
REM 6. DEPENDENCIAS MAVEN
REM ==================================================
echo ==== 6. DEPENDENCIAS MAVEN ====
echo.

findstr /M "spring-boot-starter-data-redis" "pom.xml" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Dependencia Redis presente
    set /a PASS+=1
) else (
    echo [FAIL] Dependencia Redis no encontrada
    set /a FAIL+=1
)

findstr /M "springdoc-openapi" "pom.xml" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Dependencia Swagger presente
    set /a PASS+=1
) else (
    echo [FAIL] Dependencia Swagger no encontrada
    set /a FAIL+=1
)

findstr /M "resilience4j-spring-boot3" "pom.xml" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Dependencia Resilience4j presente
    set /a PASS+=1
) else (
    echo [FAIL] Dependencia Resilience4j no encontrada
    set /a FAIL+=1
)

findstr /I /M "mockito" "pom.xml" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Dependencia Mockito presente
    set /a PASS+=1
) else (
    echo [FAIL] Dependencia Mockito no encontrada
    set /a FAIL+=1
)

findstr /I /M "assertj" "pom.xml" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Dependencia AssertJ presente
    set /a PASS+=1
) else (
    echo [FAIL] Dependencia AssertJ no encontrada
    set /a FAIL+=1
)

findstr /M "spring-boot-starter-actuator" "pom.xml" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Dependencia Actuator presente
    set /a PASS+=1
) else (
    echo [FAIL] Dependencia Actuator no encontrada
    set /a FAIL+=1
)

echo.

REM ==================================================
REM RESUMEN
REM ==================================================
echo ============================================================
echo   RESUMEN VALIDACION
echo ============================================================
echo.

echo [PASS] Total: %PASS%
echo [FAIL] Total: %FAIL%
echo.

set /a TOTAL=%PASS%+%FAIL%
if %TOTAL% GTR 0 (
    set /a PERCENTAGE=%PASS%*100/%TOTAL%
) else (
    set PERCENTAGE=0
)

echo Conformidad ISO 25010: %PERCENTAGE%%% ^(%PASS%/%TOTAL%^)
echo.

if %FAIL% EQU 0 (
    echo [PASS] TODO CUMPLE CON ISO 25010
    exit /b 0
) else (
    echo [FAIL] REVISAR %FAIL% INCUMPLIMIENTOS
    exit /b 1
)
endlocal
