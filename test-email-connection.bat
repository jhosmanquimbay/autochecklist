@echo off
echo ========================================
echo PROBANDO CONEXION DE CORREO
echo ========================================
echo.

echo Verificando si la aplicacion esta corriendo...
netstat -an | findstr :8080
if %errorlevel% neq 0 (
    echo ERROR: La aplicacion no esta corriendo en puerto 8080
    echo Ejecuta: mvn spring-boot:run
    pause
    exit /b 1
)

echo.
echo La aplicacion esta corriendo. Revisando logs...
echo.
echo Ultimas lineas del log:
tail -n 20 app.log 2>nul || (
    echo No se puede leer app.log
    echo Verifica que la aplicacion este generando logs
)

echo.
echo ========================================
echo INSTRUCCIONES:
echo ========================================
echo 1. Si ves "Authentication failed" - necesitas nueva contraseña de Gmail
echo 2. Ve a: https://myaccount.google.com/security
echo 3. Genera nueva contraseña de aplicación
echo 4. Actualiza application.properties
echo 5. Reinicia la aplicación
echo ========================================
pause