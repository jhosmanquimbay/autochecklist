@echo off
echo ========================================
echo EJECUTANDO APLICACIÓN SPRING BOOT
echo ========================================
echo.

echo 1. Limpiando compilación anterior...
call mvn clean

echo.
echo 2. Compilando proyecto...
call mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Falló la compilación
    echo Revisa los errores arriba
    pause
    exit /b 1
)

echo.
echo 3. Verificando conexión a MySQL...
mysql -u root -p -e "SELECT 'MySQL OK' as status;" 2>nul
if %errorlevel% neq 0 (
    echo ADVERTENCIA: No se pudo verificar MySQL
    echo Asegúrate de que MySQL esté ejecutándose
    echo.
)

echo 4. Iniciando aplicación Spring Boot...
echo.
echo NOTA: Si ves errores de email, es normal si no has configurado Gmail
echo La aplicación seguirá funcionando para otras funciones
echo.
echo Presiona Ctrl+C para detener la aplicación
echo.

call mvn spring-boot:run