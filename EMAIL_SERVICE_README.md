# Servicio de Correos Masivos - Spring Boot

## Descripción
Sistema completo para el envío de correos electrónicos masivos con soporte para archivos adjuntos, procesamiento asíncrono y validaciones robustas.

## Características
- ✅ Envío masivo de correos (hasta 100 destinatarios por solicitud)
- ✅ Soporte para texto plano y HTML
- ✅ Archivos adjuntos (PDF, imágenes, documentos)
- ✅ Procesamiento síncrono y asíncrono
- ✅ Validaciones de emails y contenido
- ✅ Manejo robusto de errores
- ✅ Logging detallado
- ✅ API REST documentada

## Configuración

### 1. Dependencias (ya incluidas en pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### 2. Configuración de Correo (application.properties)
```properties
# Configuración para Gmail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Para otros proveedores:
# Outlook: smtp-mail.outlook.com:587
# Yahoo: smtp.mail.yahoo.com:587
# Custom SMTP: tu-servidor-smtp.com:587
```

### 3. Configuración de App Password para Gmail
1. Ir a Google Account Settings
2. Seguridad → Verificación en 2 pasos (debe estar habilitada)
3. Contraseñas de aplicaciones → Generar nueva
4. Usar la contraseña generada en `spring.mail.password`

## Endpoints API

### 1. Health Check
```http
GET /api/email/health
```
**Respuesta:** `200 OK` - "Servicio de correo funcionando correctamente"

### 2. Validar Configuración
```http
GET /api/email/validar-configuracion
```
**Respuesta:** 
- `200 OK` - "Configuración de correo válida"
- `503 Service Unavailable` - "Configuración de correo inválida"

### 3. Envío Masivo Síncrono
```http
POST /api/email/enviar
Content-Type: application/json

{
  "destinatarios": [
    "usuario1@example.com",
    "usuario2@example.com"
  ],
  "asunto": "Asunto del correo",
  "mensaje": "Contenido del mensaje",
  "esHtml": false
}
```

### 4. Envío Masivo Asíncrono
```http
POST /api/email/enviar-async
Content-Type: application/json

{
  "destinatarios": ["email1@example.com", "email2@example.com"],
  "asunto": "Procesamiento asíncrono",
  "mensaje": "Este correo se procesa en segundo plano",
  "esHtml": false
}
```

### 5. Envío con Archivos Adjuntos
```http
POST /api/email/enviar-con-adjuntos
Content-Type: multipart/form-data

email: {
  "destinatarios": ["usuario@example.com"],
  "asunto": "Documentos adjuntos",
  "mensaje": "Archivos en adjunto",
  "esHtml": false
}
archivos: [archivo1.pdf, imagen.jpg]
```

## Estructura de Respuesta

### Respuesta Exitosa
```json
{
  "exitoso": true,
  "mensaje": "Correos enviados exitosamente",
  "totalEnviados": 5,
  "totalFallidos": 0,
  "errores": null,
  "fechaEnvio": "2024-12-10T15:30:45"
}
```

### Respuesta con Errores Parciales
```json
{
  "exitoso": false,
  "mensaje": "Algunos correos fallaron",
  "totalEnviados": 3,
  "totalFallidos": 2,
  "errores": [
    "Error enviando a email-invalido@: Invalid email format",
    "Error enviando a otro@example.com: Connection timeout"
  ],
  "fechaEnvio": "2024-12-10T15:30:45"
}
```

## Validaciones

### Emails
- Formato válido (regex pattern)
- No duplicados
- Máximo 100 destinatarios por solicitud

### Contenido
- Asunto: máximo 200 caracteres, no vacío
- Mensaje: máximo 5000 caracteres, no vacío
- Lista de destinatarios: no vacía

### Archivos Adjuntos
- Tamaño máximo por archivo: configurado en Spring Boot
- Tipos soportados: todos (PDF, imágenes, documentos, etc.)
- Limpieza automática de archivos temporales

## Ejemplos de Uso

### Ejemplo 1: Correo Simple
```bash
curl -X POST http://localhost:8080/api/email/enviar \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarios": ["test@example.com"],
    "asunto": "Prueba",
    "mensaje": "Mensaje de prueba",
    "esHtml": false
  }'
```

### Ejemplo 2: Correo HTML
```bash
curl -X POST http://localhost:8080/api/email/enviar \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarios": ["test@example.com"],
    "asunto": "Newsletter",
    "mensaje": "<h1>Título</h1><p>Contenido <strong>HTML</strong></p>",
    "esHtml": true
  }'
```

### Ejemplo 3: Con Archivos (usando form-data)
```bash
curl -X POST http://localhost:8080/api/email/enviar-con-adjuntos \
  -F 'email={"destinatarios":["test@example.com"],"asunto":"Con adjuntos","mensaje":"Ver archivos adjuntos","esHtml":false};type=application/json' \
  -F 'archivos=@documento.pdf' \
  -F 'archivos=@imagen.jpg'
```

## Configuración Avanzada

### Pool de Hilos para Async
```properties
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=100
```

### Límites de Archivos
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

## Monitoreo y Logs

El servicio genera logs detallados:
- `INFO`: Inicio y finalización de envíos
- `DEBUG`: Cada correo enviado individualmente
- `WARN`: Errores parciales en envíos masivos
- `ERROR`: Errores críticos y excepciones

## Códigos de Estado HTTP

- `200 OK`: Envío exitoso
- `202 Accepted`: Solicitud asíncrona aceptada
- `206 Partial Content`: Envío parcialmente exitoso
- `400 Bad Request`: Errores de validación
- `413 Payload Too Large`: Archivos demasiado grandes
- `500 Internal Server Error`: Errores del servidor
- `503 Service Unavailable`: Configuración de correo inválida

## Seguridad y Mejores Prácticas

1. **Nunca hardcodear credenciales** - usar variables de entorno
2. **Limitar destinatarios** - máximo 100 por solicitud
3. **Validar archivos adjuntos** - tamaño y tipo
4. **Rate limiting** - considerar implementar para producción
5. **Autenticación** - agregar seguridad a los endpoints
6. **Monitoreo** - logs y métricas para producción

## Troubleshooting

### Error: "Authentication failed"
- Verificar username/password
- Para Gmail: usar App Password, no la contraseña normal
- Verificar que 2FA esté habilitado

### Error: "Connection timeout"
- Verificar host y puerto SMTP
- Revisar firewall/proxy
- Probar con telnet: `telnet smtp.gmail.com 587`

### Error: "Invalid email format"
- El servicio valida automáticamente los emails
- Revisar formato: usuario@dominio.com

## Archivos Creados

1. `EmailRequest.java` - DTO para solicitudes
2. `EmailResponse.java` - DTO para respuestas
3. `EmailException.java` - Excepción personalizada
4. `EmailService.java` - Lógica de negocio
5. `EmailController.java` - Controlador REST
6. `AsyncConfig.java` - Configuración asíncrona
7. `GlobalExceptionHandler.java` - Manejo de errores
8. `ejemplos-postman.json` - Colección de Postman

¡El servicio está listo para usar! 🚀