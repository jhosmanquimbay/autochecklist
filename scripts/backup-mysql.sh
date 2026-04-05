#!/bin/bash
#########################################
# SCRIPT DE BACKUP - Base de Datos MySQL
#########################################
# ISO 25010: Confiabilidad - Recuperabilidad
# Crear backup diario automatizado de la BD
# Requiere: mysqldump instalado
# Uso: ./backup-mysql.sh
# O agregar a crontab: 0 2 * * * /path/to/backup-mysql.sh
#########################################

set -e  # Exit on error

# ========== CONFIGURACIÓN ==========
DB_HOST="localhost"
DB_USER="root"
DB_PASSWORD=""
DB_NAME="auto"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_backup_${TIMESTAMP}.sql"
LOG_FILE="${BACKUP_DIR}/backup.log"

# Crear directorio si no existe
mkdir -p "$BACKUP_DIR"

# ========== LOG DE EJECUCIÓN ==========
{
    echo ""
    echo "=========================================="
    echo "BACKUP INICIADO: $(date)"
    echo "=========================================="
    
    # ========== REALIZAR BACKUP ==========
    echo "Realizando backup de $DB_NAME..."
    
    if [ -z "$DB_PASSWORD" ]; then
        mysqldump --user="$DB_USER" --host="$DB_HOST" \
            --complete-insert \
            --result-file="$BACKUP_FILE" \
            "$DB_NAME"
    else
        mysqldump --user="$DB_USER" --password="$DB_PASSWORD" --host="$DB_HOST" \
            --complete-insert \
            --result-file="$BACKUP_FILE" \
            "$DB_NAME"
    fi
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "ESTADO: ✅ ÉXITO"
        echo "Archivo: $BACKUP_FILE"
        echo "Tamaño: $(du -h "$BACKUP_FILE" | cut -f1)"
        
        # ========== COMPRIMIR BACKUP ==========
        if command -v gzip &> /dev/null; then
            echo "Comprimiendo archivo..."
            gzip -f "$BACKUP_FILE"
            echo "Archivo comprimido: ${BACKUP_FILE}.gz"
        fi
        
        # ========== LIMPIAR BACKUPS ANTIGUOS (mayor a 30 días) ==========
        echo ""
        echo "Limpiando backups antiguos..."
        find "$BACKUP_DIR" -name "${DB_NAME}_backup_*.sql*" -type f -mtime +30 -delete
        
        echo "BACKUP COMPLETADO: $(date)"
        echo ""
        exit 0
    else
        echo ""
        echo "ESTADO: ❌ ERROR"
        echo "El backup no se completó. Verificar conexión a MySQL."
        echo "BACKUP FALLIDO: $(date)"
        echo ""
        exit 1
    fi
    
} | tee -a "$LOG_FILE"
