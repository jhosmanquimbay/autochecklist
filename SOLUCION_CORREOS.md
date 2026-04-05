# SOLUCIÓN PARA PROBLEMA DE CORREOS

## PROBLEMA IDENTIFICADO
❌ **Authentication failed (535 5.7.8)** - La contraseña de aplicación de Gmail no es válida

## PASOS PARA SOLUCIONARLO

### 1. GENERAR NUEVA CONTRASEÑA DE APLICACIÓN EN GMAIL

1. Ve a tu cuenta de Gmail: https://myaccount.google.com/
2. Ir a **Seguridad** → **Verificación en 2 pasos** (debe estar activada)
3. Ir a **Contraseñas de aplicaciones**
4. Generar nueva contraseña para "Correo"
5. **COPIAR LA CONTRASEÑA GENERADA** (16 caracteres sin espacios)

### 2. ACTUALIZAR CONFIGURACIÓN

Reemplaza en `src/main/resources/application.properties`:

```properties
# CAMBIAR ESTAS LÍNEAS:
spring.mail.password=dsii dkoz rehf xcdd
mail.inbound.password=dsii dkoz rehf xcdd

# POR LA NUEVA CONTRASEÑA (ejemplo):
spring.mail.password=abcd efgh ijkl mnop
mail.inbound.password=abcd efgh ijkl mnop
```

### 3. VERIFICAR CONFIGURACIÓN GMAIL

Asegúrate de que:
- ✅ Verificación en 2 pasos esté ACTIVADA
- ✅ "Acceso de aplicaciones menos seguras" esté DESACTIVADO (usar contraseñas de aplicación)
- ✅ IMAP esté habilitado en Gmail (Configuración → Reenvío e IMAP)

### 4. REINICIAR APLICACIÓN

```bash
# Detener la aplicación actual
Ctrl+C

# Reiniciar
mvn spring-boot:run
```

## CONFIGURACIÓN ACTUAL DETECTADA

```properties
# SMTP (Envío)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=autochecklistoficial@gmail.com
spring.mail.password=dsii dkoz rehf xcdd  ← ESTA CONTRASEÑA ESTÁ EXPIRADA

# IMAP (Recepción)
mail.inbound.host=imap.gmail.com
mail.inbound.port=993
mail.inbound.user=autochecklistoficial@gmail.com
mail.inbound.password=dsii dkoz rehf xcdd  ← ESTA CONTRASEÑA ESTÁ EXPIRADA
```

## DESPUÉS DE ACTUALIZAR

Los correos deberían funcionar:
- ✅ Envío de correos internos
- ✅ Envío de correos externos
- ✅ Recepción de correos externos (cada 60 segundos)
- ✅ Respuestas automáticas

## HERRAMIENTAS DE DIAGNÓSTICO

### 1. Página de Test (NUEVA)
Ve a: http://localhost:8080/test-email
- Prueba la conexión SMTP en tiempo real
- Muestra errores específicos
- Instrucciones paso a paso

### 2. Script de Test
Ejecuta: `test-email-connection.bat`
- Verifica que la app esté corriendo
- Muestra los últimos logs

## LOGS PARA VERIFICAR

Busca en los logs:
- ✅ `📧 Iniciando polling de correos entrantes...`
- ✅ `✅ Polling completado exitosamente`
- ✅ `[MensajeService] Mensaje guardado con ID: X`
- ❌ `❌ ERROR DE AUTENTICACIÓN IMAP` (no debería aparecer)

## ACCESO RÁPIDO

1. **Test de conexión**: http://localhost:8080/test-email
2. **Bandeja de entrada**: http://localhost:8080/correo/inbox/[tu_usuario_id]
3. **Enviar correo**: http://localhost:8080/correo/enviar/[tu_usuario_id]