@echo off
echo ========================================
echo VERIFICACION DE INTERFACES DE CORREO
echo ========================================
echo.

echo 1. Verificando que la aplicacion este corriendo...
netstat -an | findstr :8080
if %errorlevel% neq 0 (
    echo ERROR: La aplicacion no esta corriendo en puerto 8080
    pause
    exit /b 1
)

echo.
echo 2. Verificando respuesta del servidor...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing; Write-Host 'Servidor responde: HTTP' $response.StatusCode } catch { Write-Host 'Error:' $_.Exception.Message }"

echo.
echo 3. Verificando pagina de test de correos...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/test-email' -UseBasicParsing; Write-Host 'Pagina de test: HTTP' $response.StatusCode } catch { Write-Host 'Error:' $_.Exception.Message }"

echo.
echo ========================================
echo INSTRUCCIONES:
echo ========================================
echo 1. Ve a: http://localhost:8080/
echo 2. Haz login con tus credenciales
echo 3. Busca el menu "Correo" en el sidebar
echo 4. Deberia tener opciones: Inbox, Enviados, Papelera, Masivos
echo.
echo Si no ves los botones:
echo - Verifica que hayas hecho login
echo - Presiona F12 y revisa errores en la consola
echo - Refresca la pagina (F5)
echo ========================================
pause