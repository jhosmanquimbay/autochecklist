# 🧪 CÓMO PROBAR LAS INTERFACES DE CORREO

## 🔐 PASO 1: HACER LOGIN

1. **Ve a**: http://localhost:8080/
2. **Haz login** con tus credenciales de usuario
3. **Navega al dashboard** correspondiente a tu rol

## 📧 PASO 2: ACCEDER AL SISTEMA DE CORREOS

### Desde el Dashboard:
- Busca el menú **"Correo"** en el sidebar izquierdo
- Debería tener un ícono de sobre (📧) y mostrar el número de mensajes no leídos

### Opciones disponibles en el menú desplegable:
- ✅ **Bandeja de entrada** - Ver correos recibidos
- ✅ **Enviados** - Ver correos enviados  
- ✅ **Papelera** - Ver correos eliminados
- ✅ **Correos Masivos** - Enviar a múltiples destinatarios

## 🔍 PASO 3: VERIFICAR BOTONES Y FUNCIONALIDADES

### En la Bandeja de Entrada:
- ✅ **Botón flotante (+)** - Para redactar nuevo mensaje
- ✅ **Botón estrella (⭐)** - Para marcar como favorito
- ✅ **Botón papelera (🗑️)** - Para mover a papelera
- ✅ **Click en mensaje** - Para abrir y leer

### En cada vista:
- ✅ **Sidebar de navegación** - Con todos los enlaces
- ✅ **Botones de acción** - En cada mensaje
- ✅ **Modales** - Para redactar y ver mensajes

## 🚨 SI NO VES LOS BOTONES:

### Posibles causas:
1. **No has hecho login** - Las interfaces requieren autenticación
2. **Rol incorrecto** - Algunos botones dependen del rol de usuario
3. **CSS no cargado** - Problemas de estilos
4. **JavaScript deshabilitado** - Algunos botones requieren JS

### Soluciones:
1. **Hacer login correctamente**
2. **Verificar que eres Admin, Gerente o Conductor**
3. **Refrescar la página** (F5)
4. **Abrir herramientas de desarrollador** (F12) y revisar errores

## 🔗 ENLACES DIRECTOS (después del login):

Reemplaza `[ID]` con tu ID de usuario:

- **Bandeja**: http://localhost:8080/correo/inbox/[ID]
- **Enviados**: http://localhost:8080/correo/enviados/[ID]  
- **Papelera**: http://localhost:8080/correo/papelera/[ID]
- **Masivos**: http://localhost:8080/correo/masivos/[ID]

## 📱 EJEMPLO DE USO:

1. **Login** → Dashboard
2. **Click en "Correo"** → Se abre menú desplegable
3. **Click en "Bandeja de entrada"** → Lista de mensajes
4. **Click en botón (+)** → Modal para redactar
5. **Click en un mensaje** → Modal para leer
6. **Click en 🗑️** → Mover a papelera

## ✅ ESTADO ACTUAL:

- ✅ **Aplicación funcionando**: Puerto 8080
- ✅ **Correos entrantes**: Recibiendo automáticamente
- ✅ **Base de datos**: Guardando mensajes
- ✅ **Plantillas**: Sin errores de sintaxis
- ✅ **Botones**: Presentes en el código HTML

**Si sigues sin ver los botones, comparte una captura de pantalla de lo que ves.**