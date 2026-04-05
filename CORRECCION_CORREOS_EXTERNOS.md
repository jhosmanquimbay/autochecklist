# 🔧 Corrección: Montaje de Correos Externos y Mejoría Visual

## 📋 Resumen de Cambios

Se han realizado dos mejoras importantes en el sistema de correos:

### 1️⃣ **Visualización de Mensajes Mejorada**

#### Lo que cambió:
- **Antes**: El asunto era gigantesco (1.4rem) y todos los elementos estaban mezclados
- **Ahora**: Diseño similar a Gmail con:
  - ✅ Asunto pequeño arriba (0.95rem)
  - ✅ Avatar del remitente a la izquierda con INICIALES
  - ✅ Nombre y correo del remitente de forma legible
  - ✅ **CONTENIDO PRINCIPAL PROMINENTE EN EL CENTRO** (grande y fácil de leer)
  - ✅ Fecha y hora pequeña ABAJO (0.8rem)

#### Archivos modificados:
- `src/main/resources/templates/correo/verFragment.html` - Nueva estructura HTML
- `src/main/resources/static/css/admin-theme.css` - Nuevos estilos CSS

### 2️⃣ **Diagnóstico para Correos Externos No Llegando**

#### Nuevo Endpoint API:
```
POST /correo/api/sync-inbox
```
- **Propósito**: Fuerza la sincronización manual de la bandeja IMAP
- **Respuesta**: JSON con status success/error

#### Página de Diagnóstico Mejorada:
- **Ruta**: `/correo/debug/{id}`
- **Nueva característica**: Botón **"Sincronizar Ahora"** que permite al usuario:
  1. Forzar la lectura manual de IMAP
  2. Ver el resultado en tiempo real
  3. Recibir feedback visual (éxito/error)

#### Instrucciones Incluidas:
La página de debug ahora incluye un panel de **"¿No llegan los correos?"** con:
- ✅ 5 pasos para resolver el problema
- ✅ Links a Google Account Security
- ✅ Instrucciones para generar contraseña de aplicación
- ✅ Verificación de configuración IMAP

#### Archivos modificados:
- `src/main/java/com/example/demostracion/controller/CorreoController.java`
  - Agregada inyección de `InboundMailService`
  - Agregada inyección de `MensajeRepository`
  - Mejorado endpoint `/api/sync-inbox` para ejecutar `pollInbox()` manualmente

- `src/main/resources/templates/correo/debug.html`
  - Nueva interfaz visual profesional
  - Botón de sincronización con feedback
  - Panel de instrucciones para resolver problemas
  - Mejor visualización de mensajes

---

## 🚀 Cómo Usar

### Para Ver Mensajes Externos Recibidos:
1. Ve a `/correo/inbox` o usa el menú Correo
2. Los mensajes externos ahora se verán con la interfaz mejorada:
   - Contenido prominente
   - Fecha y hora pequeña abajo
   - Similar a Gmail

### Para Sincronizar Correos Que No Llegan:
1. Ve a `/correo/debug/{tuIDUsuario}` (opcionalmente automático desde `/correo/debug`)
2. Haz clic en el botón **"Sincronizar Ahora"**
3. Espera el feedback (éxito o error)
4. Si hay error, sigue las instrucciones del panel mostrado en la página

---

## 🔍 Diagnóstico de Problemas

### Si no llegan correos externos:

**Paso 1**: Verifica que IMAP esté habilitado
- La app comprueba automáticamente si las credenciales están configuradas
- Si no, muestra: `⚠️ Credenciales IMAP no configuradas, omitiendo polling`

**Paso 2**: Genera contraseña de aplicación en Gmail
- Ve a: https://myaccount.google.com/security
- Busca "Contraseñas de aplicación"
- Crea una nueva contraseña para Gmail (no correo)
- Reemplázala en `application.properties`:
  ```properties
  mail.inbound.password=tu_nueva_contraseña
  ```

**Paso 3**: Verifica que IMAP esté activado en Gmail
- Gmail Settings → Forwarding and POP/IMAP
- Habilita IMAP

**Paso 4**: Sincroniza manualmente
- Usa el botón "Sincronizar Ahora" en la página de debug
- La app ejecutará `InboundMailService.pollInbox()` inmediatamente

**Paso 5**: Reinicia la aplicación
- Los cambios en `application.properties` requieren reinicio

---

## 📧 Configuración IMAP Actual

En tu `application.properties`:

```properties
# INBOUND (IMAP) CONFIG
mail.inbound.protocol=imaps
mail.inbound.host=imap.gmail.com
mail.inbound.port=993
mail.inbound.user=autochecklistoficial@gmail.com
mail.inbound.password=nmur ogif knsm zlkf
mail.inbound.folder=INBOX
mail.inbound.poll-interval-ms=60000
mail.inbound.mark-seen=true
mail.inbound.attachments-base=uploads/correos
```

⚠️ **IMPORTANTE**: Si la contraseña expira o genera error, genera una nueva en Google Account Security.

---

## 🎨 Cambios Visuales - Antes vs Después

### ANTES (Estructura confusa):
```
[ASUNTO MUY GRANDE (1.4rem)]
[Avatar] [Nombre] [Correo] [FECHA EN LA DERECHA]
[Estado] [Botones de acción]
─────────────────
[Contenido en fondo gris claro]
```

### DESPUÉS (Estilo Gmail):
```
┌─────────────────────────────────┐
│ asunto pequeño (0.95rem)  [Leído]│
├─────────────────────────────────┤
│ [Avatar] Nombre del Remitente    │
│         correo@ejemplo.com       │
├─────────────────────────────────┤
│                                 │
│  CONTENIDO PRINCIPAL PROMINENTE  │
│  (Fácil de leer, grande)        │
│                                 │
├─────────────────────────────────┤
│            17/02/2026 20:44      │ ← Fecha pequeña (0.8rem)
└─────────────────────────────────┘
```

---

## 🧪 Testing del Sistema

### Probar recepción de correos:
1. Envía un email externo a `autochecklistoficial@gmail.com`
2. Espera a que pase el intervalo de polling (60 segundos)
3. O usa el botón "Sincronizar Ahora" en `/correo/debug`
4. Verifica que aparezca en `/correo/inbox`

### Probar visualización:
1. Abre un correo recibido
2. Verifica que el contenido sea prominente
3. Verifica que la fecha esté pequeña abajo

---

## 🔧 Configuración del Polling

El sistema verifica automáticamente cada **60 segundos** (configurable):

```properties
mail.inbound.poll-interval-ms=60000
```

Para cambiar a cada 30 segundos:
```properties
mail.inbound.poll-interval-ms=30000
```

---

## 📝 Notas Importantes

1. El servicio `InboundMailService` se ejecuta automáticamente en background
2. Los mensajes externos se emparejan automáticamente con conversaciones
3. Si no se empareja, se asigna a un usuario fallback (configurable)
4. Los adjuntos se guardan en `uploads/correos/{id}/`

---

## ✅ Checklist de Verificación

- [ ] Visualización de mensajes: asunto pequeño, contenido grande, fecha abajo
- [ ] Botón "Sincronizar Ahora" funciona en `/correo/debug`
- [ ] Panel de instrucciones está visible en la página de debug
- [ ] Los correos externos se reciben en la bandeja

Si algo no funciona, revisá:
1. Los logs de consola cuando sincronizas
2. La configuración IMAP en `application.properties`
3. Que la contraseña de aplicación sea válida en Gmail

---

**Última actualización**: 23 de febrero de 2026
