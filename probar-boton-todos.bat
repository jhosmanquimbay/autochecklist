@echo off
echo ========================================
echo PROBANDO BOTÓN "TODOS"
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

echo Iniciando aplicación...
echo.
echo ========================================
echo PRUEBAS DEL BOTÓN "TODOS":
echo ========================================
echo.
echo 1. Vista Usuario (Público):
echo    - Ir a: http://localhost:8080/catalogo
echo    - Click en "Todos" → Debe mostrar todos los vehículos
echo    - URL resultante: http://localhost:8080/catalogo
echo.
echo 2. Vista Gerente (Admin):
echo    - Ir a: http://localhost:8080/catalogo/gestion
echo    - Click en "Todos" → Debe mostrar todos los vehículos
echo    - URL resultante: http://localhost:8080/catalogo/gestion
echo.
echo 3. Rutas alternativas disponibles:
echo    - http://localhost:8080/catalogo/todos
echo    - http://localhost:8080/catalogo/gestion/todos
echo.
echo ========================================
echo COMPORTAMIENTO ESPERADO:
echo ========================================
echo.
echo ✅ El botón "Todos" debe:
echo    - Mostrar TODOS los vehículos sin filtro
echo    - Mantenerse activo (resaltado)
echo    - Redirigir a la vista completa del catálogo
echo    - Funcionar en ambas vistas (usuario y gerente)
echo.
echo Presiona Ctrl+C para detener la aplicación
echo.

call mvn spring-boot:run