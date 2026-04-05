package com.example.demostracion.config;

/**
 * Constantes globales de la aplicación
 * Centraliza valores utilizados en múltiples componentes
 * 
 * ISO 25010: Mantenibilidad - Modularidad
 * 
 * @author Sistema
 * @since 1.0
 */
public class AppConstants {

    private AppConstants() {
        // Clase de constantes - no instanciar
        throw new IllegalStateException("Utility class");
    }

    // ==================== ROLES ====================
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_GERENTE = "ROLE_GERENTE";
    public static final String ROLE_VENDEDOR = "ROLE_VENDEDOR";
    public static final String ROLE_CONDUCTOR = "ROLE_CONDUCTOR";

    // ==================== SEGURIDAD ====================
    public static final int BCRYPT_STRENGTH = 12;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCK_TIME_MINUTES = 30;
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    // ==================== CORREO ====================
    public static final int EMAIL_MAX_RETRIES = 3;
    public static final int EMAIL_RETRY_DELAY_MS = 1000;
    public static final String EMAIL_POOL_NAME = "emailThreadPool";
    public static final int EMAIL_THREAD_POOL_SIZE = 5;
    public static final int EMAIL_QUEUE_CAPACITY = 1000;
    public static final int EMAIL_IMAP_POLL_INTERVAL_MS = 60000; // 1 minuto

    // ==================== CACHE ====================
    public static final String CACHE_VEHICULOS = "vehiculos";
    public static final String CACHE_USUARIOS = "usuarios";
    public static final String CACHE_ROLES = "roles";
    public static final long CACHE_TTL_MINUTES = 15;

    // ==================== ALMACENAMIENTO ====================
    public static final String UPLOADS_DIR = "uploads/";
    public static final String CORREOS_DIR = UPLOADS_DIR + "correos/";
    public static final String VEHICULOS_DIR = UPLOADS_DIR + "vehiculos/";
    public static final long MAX_FILE_SIZE_BYTES = 52428800; // 50MB
    public static final String[] ALLOWED_FILE_TYPES = {
        "image/jpeg", "image/png", "image/gif",
        "application/pdf", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    // ==================== BASE DE DATOS ====================
    public static final int DB_POOL_MIN_SIZE = 2;
    public static final int DB_POOL_MAX_SIZE = 20;
    public static final long DB_CONNECTION_TIMEOUT_MS = 30000;
    public static final long DB_IDLE_TIMEOUT_MS = 600000;
    public static final long DB_MAX_LIFETIME_MS = 1800000;

    // ==================== PAGINACIÓN ====================
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== AUDITORÍA ====================
    public static final String AUDIT_TABLE = "audit_log";
    public static final String AUDIT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // ==================== MENSAJES ====================
    public static final String SUCCESS_MESSAGE = "Operación realizada exitosamente";
    public static final String ERROR_MESSAGE = "Error al procesar la solicitud";
    public static final String UNAUTHORIZED_MESSAGE = "No tienes permisos para acceder a este recurso";
    public static final String NOT_FOUND_MESSAGE = "El recurso solicitado no fue encontrado";

    // ==================== VERSIÓN ====================
    public static final String APP_VERSION = "1.0.0";
    public static final String API_VERSION = "/api/v1";
}
