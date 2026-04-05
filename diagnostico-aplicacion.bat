@echo off
echo ========================================
echo DIAGNÓSTICO DE LA APLICACIÓN
echo ========================================
echo.

echo 1. Verificando Java...
java -version
echo.

echo 2. Verificando Maven...
mvn -version
echo.

echo 3. Verificando conexión a MySQL...
echo Intentando conectar a MySQL en localhost:3306...
mysql -u root -p -e "SELECT 'Conexión MySQL exitosa' as resultado;" 2>nul
if %errorlevel% neq 0 (
    echo ERROR: No se pudo conectar a MySQL
    echo Asegúrate de que MySQL esté ejecutándose en puerto 3306
) else (
    echo MySQL conectado correctamente
)
echo.

echo 4. Verificando base de datos 'auto'...
mysql -u root -p -e "USE auto; SELECT 'Base de datos auto existe' as resultado;" 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Base de datos 'auto' no existe
    echo Ejecuta: CREATE DATABASE auto;
) else (
    echo Base de datos 'auto' existe
)
echo.

echo 5. Compilando proyecto...
call mvn clean compile
if %errorlevel% neq 0 (
    echo ERROR: Falló la compilación
    pause
    exit /b 1
)
echo.

echo 6. Ejecutando tests...
call mvn test -Dtest=!EmailServiceTest
echo.

echo ========================================
echo DIAGNÓSTICO COMPLETADO
echo ========================================
echo.
echo Para ejecutar la aplicación:
echo mvn spring-boot:run
echo.
pause