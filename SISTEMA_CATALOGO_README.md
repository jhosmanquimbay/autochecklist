# 🚗 Sistema de Catálogo de Vehículos

## Descripción del Sistema

Sistema completo de catálogo de vehículos con **dos vistas claramente separadas** según los permisos del usuario:

### 📌 1. Vista Catálogo (Usuario) - Solo Lectura
- **URL:** `/catalogo`
- **Acceso:** Público (todos los usuarios)
- **Funcionalidad:** Solo visualización, NO puede editar

### 📌 2. Vista Gestión (Gerente) - Administración Completa
- **URL:** `/catalogo/gestion`
- **Acceso:** Solo roles GERENTE y ADMIN
- **Funcionalidad:** CRUD completo (Crear, Leer, Actualizar, Eliminar)

---

## 🎯 Características Principales

### ✨ Vista Usuario (Solo Lectura)
- **Tarjetas de vehículos** con imagen, nombre, tipo, descripción y precio
- **Filtros por categoría:** Todos, Sedán, 4x4, Van, Pasajeros
- **Botón "Ver Detalles"** para información completa
- **Diseño moderno y responsive** basado en la vista de referencia
- **Sin controles de edición** - solo visualización

### 🔧 Vista Gestión (Gerente)
- **Panel administrativo** con controles completos
- **Agregar vehículos** con formulario completo
- **Editar vehículos** existentes
- **Eliminar vehículos** con confirmación
- **Subir imágenes** (archivo o URL)
- **Filtros administrativos** por categoría
- **Diseño diferenciado** para distinguir del catálogo público

---

## 🚀 Instalación y Ejecución

### Ejecutar Sistema Completo
```bash
ejecutar-sistema-catalogo.bat
```

### Ejecución Manual
```bash
mvn clean compile
mvn spring-boot:run
```

---

## 📋 URLs del Sistema

| Vista | URL | Acceso | Descripción |
|-------|-----|--------|-------------|
| **Catálogo Usuario** | `/catalogo` | Público | Solo lectura, filtros, ver detalles |
| **Gestión Gerente** | `/catalogo/gestion` | GERENTE/ADMIN | CRUD completo, administración |
| **Agregar Vehículo** | `/catalogo/agregar` | GERENTE/ADMIN | Formulario para nuevos vehículos |
| **Editar Vehículo** | `/catalogo/editar/{id}` | GERENTE/ADMIN | Formulario de edición |
| **Ver Detalles** | `/catalogo/detalles/{id}` | Público | Vista detallada del vehículo |
| **Dashboard** | `/dashboard` | Autenticado | Panel principal con accesos |

---

## 🎨 Estructura de Vistas

```
src/main/resources/templates/catalogo/
├── usuario.html      # Vista pública (solo lectura)
├── gestion.html      # Vista administrativa (CRUD)
├── formulario.html   # Agregar/Editar vehículos
└── detalles.html     # Vista detallada del vehículo
```

---

## 📊 Campos del Formulario

### Campos Obligatorios (*)
- **Marca:** Toyota, Honda, Mercedes, etc.
- **Modelo:** Corolla, Civic, C-Class, etc.
- **Tipo de Vehículo:** Sedán, 4x4, Van, Pasajeros, Deportivo, Compacto
- **Precio:** Valor en USD
- **Descripción:** Características del vehículo

### Campos Opcionales
- **Imagen:** URL o archivo subido (JPG, PNG, GIF)

---

## 🔐 Control de Permisos

### Vista Usuario (`/catalogo`)
```java
@PreAuthorize("permitAll()")
```
- ✅ Cualquier usuario puede acceder
- ✅ Solo visualización de vehículos
- ✅ Filtros por categoría
- ✅ Ver detalles
- ❌ NO puede agregar/editar/eliminar

### Vista Gerente (`/catalogo/gestion`)
```java
@PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
```
- ✅ Solo GERENTE y ADMIN
- ✅ Agregar nuevos vehículos
- ✅ Editar vehículos existentes
- ✅ Eliminar vehículos
- ✅ Gestión completa de imágenes

---

## 🎯 Flujo de Uso

### Para Usuarios Normales:
1. **Acceder:** `http://localhost:8080/catalogo`
2. **Filtrar:** Click en categorías (Todos, Sedán, 4x4, etc.)
3. **Ver detalles:** Click en "Ver Detalles"
4. **Navegar:** Solo lectura, sin opciones de edición

### Para Gerentes:
1. **Login:** Iniciar sesión con rol GERENTE/ADMIN
2. **Acceder gestión:** `http://localhost:8080/catalogo/gestion`
3. **Agregar vehículo:** Click en "Agregar Vehículo"
4. **Editar:** Click en "Editar" en cualquier vehículo
5. **Eliminar:** Click en "Eliminar" con confirmación
6. **Ver público:** Link a vista pública desde gestión

---

## 🎨 Diferencias Visuales

### Vista Usuario
- **Colores:** Gradientes suaves (azul/gris)
- **Header:** Navegación pública
- **Botones:** Solo "Ver Detalles"
- **Diseño:** Limpio y comercial

### Vista Gerente
- **Colores:** Gradientes administrativos (azul/púrpura)
- **Header:** Navegación administrativa
- **Botones:** Ver, Editar, Eliminar
- **Diseño:** Funcional y profesional

---

## 📱 Responsive Design

Ambas vistas están optimizadas para:
- **Desktop:** Grid de 3-4 columnas
- **Tablet:** Grid de 2 columnas
- **Móvil:** Grid de 1 columna
- **Controles:** Adaptación automática

---

## 🔄 Sincronización de Datos

- **Datos compartidos:** Ambas vistas usan la misma fuente de datos
- **Actualización automática:** Los cambios del gerente se reflejan inmediatamente en la vista usuario
- **Persistencia:** Los datos se mantienen durante la sesión de la aplicación

---

## 🎉 ¡Sistema Listo!

El sistema está **completamente funcional** con:

✅ **Separación clara de permisos**
✅ **Vista usuario solo de lectura**
✅ **Vista gerente con CRUD completo**
✅ **Diseño moderno y responsive**
✅ **Formularios completos con validación**
✅ **Gestión de imágenes (URL y upload)**
✅ **Filtros funcionales por categoría**
✅ **Integración con el dashboard**

**¡Disfruta tu nuevo sistema de catálogo de vehículos!** 🚗✨