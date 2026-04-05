@echo off
REM ========================================
REM SCRIPT DE BACKUP - Base de Datos MySQL
REM ========================================
REM ISO 25010: Confiabilidad - Recuperabilidad
REM Crear backup diario automatizado de la BD
REM Requiere: MySQL instalado y en PATH
REM Uso: Ejecutar con Task Scheduler diariamente
REM ========================================

setlocal enabledelayedexpansion

REM ========== CONFIGURACIÓN ==========
set DB_HOST=localhost
set DB_USER=root
set DB_PASSWORD=
set DB_NAME=auto
set BACKUP_DIR=D:\backups\mysql
set TIMESTAMP=%date:~10,4%%date:~4,2%%date:~7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\%DB_NAME%_backup_%TIMESTAMP%.sql
set LOG_FILE=%BACKUP_DIR%\backup.log

REM Crear directorio si no existe
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM ========== LOG DE EJECUCIÓN ==========
echo. >> "%LOG_FILE%"
echo ========================================== >> "%LOG_FILE%"
echo BACKUP INICIADO: %date% %time% >> "%LOG_FILE%"
echo ========================================== >> "%LOG_FILE%"

REM ========== REALIZAR BACKUP ==========
echo Realizando backup de %DB_NAME%...
mysqldump --user=%DB_USER% --password=%DB_PASSWORD% --host=%DB_HOST% --complete-insert --result-file="%BACKUP_FILE%" %DB_NAME% 2>> "%LOG_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo. >> "%LOG_FILE%"
    echo ESTADO: ✅ ÉXITO >> "%LOG_FILE%"
    echo Archivo: %BACKUP_FILE% >> "%LOG_FILE%"
    
    REM Obtener tamaño del archivo
    for %%F in ("%BACKUP_FILE%") do (
        set SIZE=%%~zF
        echo Tamaño: !SIZE! bytes >> "%LOG_FILE%"
    )
    
    echo BACKUP COMPLETADO: %date% %time% >> "%LOG_FILE%"
    
    REM ========== COMPRIMIR BACKUP ==========
    REM Comprimir con 7-Zip si está instalado
    where /q 7z
    if %ERRORLEVEL% EQU 0 (
        echo Comprimiendo archivo...
        7z a -tzip "%BACKUP_FILE%.zip" "%BACKUP_FILE%" >> "%LOG_FILE%" 2>&1
        if %ERRORLEVEL% EQU 0 (
            echo Archivo comprimido: %BACKUP_FILE%.zip >> "%LOG_FILE%"
            del "%BACKUP_FILE%"
        )
    )
    
    REM ========== LIMPIAR BACKUPS ANTIGUOS (mayor a 30 días) ==========
    echo. >> "%LOG_FILE%"
    echo Limpiando backups antiguos... >> "%LOG_FILE%"
    for /F "delims=" %%F in ('dir /b "%BACKUP_DIR%\*.sql*" 2^>nul') do (
        for /F %%A in ('powershell -Command "[Math]::Floor(((Get-Date) - (Get-Item '%BACKUP_DIR%\%%F').CreationTime).TotalDays)"') do (
            if %%A GEQ 30 (
                echo Eliminando: %%F (%%A días de antigüedad) >> "%LOG_FILE%"
                del "%BACKUP_DIR%\%%F"
            )
        )
    )
    
    echo. >> "%LOG_FILE%"
    exit /b 0
) else (
    echo. >> "%LOG_FILE%"
    echo ESTADO: ❌ ERROR >> "%LOG_FILE%"
    echo El backup no se completó. Verificar log arriba. >> "%LOG_FILE%"
    echo BACKUP FALLIDO: %date% %time% >> "%LOG_FILE%"
    echo. >> "%LOG_FILE%"
    exit /b 1
)
