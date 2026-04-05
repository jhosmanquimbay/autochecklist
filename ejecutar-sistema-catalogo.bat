@echo off
echo ========================================
echo SISTEMA DE CATÁLOGO DE VEHÍCULOS
echo ========================================
echo.

echo Compilando proyecto...
call mvn clean compile
if %errorlevel% neq 0 (
    echo ERROR: Falló la compilación
    pause
    exit /b 1
)
echo.

echo Creando directorios necesarios...
if not exist "uploads\vehiculos" (
    mkdir uploads\vehiculos
    echo Directorio uploads\vehiculos creado.
) else (
    echo Directorio uploads\vehiculos ya existe.
)
echo.

echo Iniciando aplicación...
echo.
echo ========================================
echo SISTEMA DISPONIBLE EN:
echo ========================================
echo.
echo 📋 VISTA USUARIO (Solo lectura):
echo    http://localhost:8080/catalogo
echo.
echo 🔧 VISTA GERENTE (Administración):
echo    http://localhost:8080/catalogo/gestion
echo    (Requiere rol GERENTE o ADMIN)
echo.
echo 🏠 DASHBOARD:
echo    http://localhost:8080/dashboard
echo.
echo ========================================
echo FUNCIONALIDADES:
echo ========================================
echo.
echo ✅ Vista Usuario:
echo    - Ver catálogo de vehículos
echo    - Filtrar por categoría (Todos, Sedán, 4x4, Van, Pasajeros)
echo    - Ver detalles de cada vehículo
echo    - Diseño responsive y moderno
echo.
echo ✅ Vista Gerente:
echo    - Agregar nuevos vehículos
echo    - Editar vehículos existentes
echo    - Eliminar vehículos
echo    - Subir imágenes o usar URLs
echo    - Filtros administrativos
echo.
echo Presiona Ctrl+C para detener la aplicación
echo.

call mvn spring-boot:run