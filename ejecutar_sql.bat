@echo off
echo ========================================
echo  AGREGAR COLUMNA eliminado_permanente
echo ========================================
echo.
echo Este script agregara la columna necesaria
echo para la funcionalidad "Vaciar Papelera"
echo.
echo Base de datos: auto
echo Usuario: root
echo.
pause

echo.
echo Ejecutando script SQL...
echo.

mysql -u root auto < agregar_columna_eliminado_permanente.sql

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo  EXITO: Columna agregada correctamente
    echo ========================================
    echo.
    echo Ahora puedes:
    echo 1. Reiniciar la aplicacion Spring Boot
    echo 2. Probar la funcionalidad "Vaciar Papelera"
    echo.
) else (
    echo.
    echo ========================================
    echo  ERROR: No se pudo ejecutar el script
    echo ========================================
    echo.
    echo Posibles causas:
    echo - MySQL no esta instalado o no esta en PATH
    echo - El usuario root requiere password
    echo - La base de datos 'auto' no existe
    echo.
    echo Solucion alternativa:
    echo 1. Abre MySQL Workbench
    echo 2. Conectate a la base de datos
    echo 3. Ejecuta manualmente:
    echo    USE auto;
    echo    ALTER TABLE mensaje ADD COLUMN eliminado_permanente BOOLEAN DEFAULT FALSE AFTER eliminado;
    echo.
)

pause
