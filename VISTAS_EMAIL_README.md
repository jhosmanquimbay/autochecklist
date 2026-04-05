# Vistas Web - Sistema de Correos Masivos

## Descripción
Interfaz web completa para el sistema de correos masivos, diseñada para facilitar el uso del servicio sin necesidad de conocimientos técnicos.

## Vistas Disponibles

### 1. Página Principal (`/email`)
**Archivo:** `src/main/resources/templates/email/index.html`

**Características:**
- Dashboard principal del sistema de correos
- Verificación automática de configuración SMTP
- Acceso rápido a todas las funcionalidades
- Información de la API REST
- Indicadores de estado en tiempo real

**Funcionalidades:**
- ✅ Estado del servidor SMTP
- ✅ Navegación intuitiva
- ✅ Documentación integrada
- ✅ Diseño responsive

### 2. Envío Simple (`/email/enviar`)
**Archivo:** `src/main/resources/templates/email/enviar.html`

**Características:**
- Formulario para envío de correos masivos
- Soporte para texto plano y HTML
- Vista previa en tiempo real
- Contador de destinatarios
- Validaciones del lado cliente

**Funcionalidades:**
- ✅ Lista de destinatarios (textarea)
- ✅ Asunto y mensaje
- ✅ Opción HTML/texto plano
- ✅ Envío síncrono y asíncrono
- ✅ Vista previa automática
- ✅ Validación de límites (100 destinatarios)

### 3. Envío con Adjuntos (`/email/enviar-adjuntos`)
**Archivo:** `src/main/resources/templates/email/enviar-adjuntos.html`

**Características:**
- Drag & drop para archivos
- Vista previa de archivos seleccionados
- Validación de tamaño (10MB por archivo)
- Iconos por tipo de archivo
- Gestión de archivos múltiples

**Funcionalidades:**
- ✅ Arrastrar y soltar archivos
- ✅ Selección múltiple
- ✅ Vista previa de archivos
- ✅ Eliminación individual
- ✅ Validación de tamaño
- ✅ Iconos por tipo de archivo

## Controlador Web

### EmailWebController
**Archivo:** `src/main/java/com/example/demostracion/controller/EmailWebController.java`

**Endpoints:**
- `GET /email` - Página principal
- `GET /email/enviar` - Formulario de envío simple
- `POST /email/enviar` - Procesar envío simple
- `POST /email/enviar-async` - Procesar envío asíncrono
- `GET /email/enviar-adjuntos` - Formulario con adjuntos
- `POST /email/enviar-adjuntos` - Procesar envío con adjuntos
- `GET /email/validar` - Validar configuración SMTP (AJAX)

## Características de la Interfaz

### Diseño y UX
- **Bootstrap 5** para diseño responsive
- **Font Awesome** para iconos
- **CSS personalizado** con animaciones
- **Tema oscuro/claro** adaptable
- **Animaciones suaves** y transiciones

### Validaciones del Cliente
```javascript
// Validación de destinatarios
function contarEmails() {
    const emails = texto.split(/[,\n\r]+/).filter(email => email.trim().length > 0);
    // Cambio de color según cantidad
    if (count > 100) counter.className = 'text-danger';
    else if (count > 80) counter.className = 'text-warning';
    else counter.className = 'text-success';
}

// Validación de archivos
if (archivo.size > 10 * 1024 * 1024) {
    alert('Archivo demasiado grande (máximo 10MB)');
    return;
}
```

### Vista Previa en Tiempo Real
- **Asunto:** Se actualiza automáticamente
- **Mensaje:** Renderiza HTML si está habilitado
- **Archivos:** Lista con iconos y tamaños
- **Destinatarios:** Contador dinámico

### Manejo de Estados
- **Éxito:** Alertas verdes con detalles
- **Advertencia:** Alertas amarillas con errores parciales
- **Error:** Alertas rojas con descripción
- **Info:** Alertas azules para procesos asíncronos

## Integración con el Dashboard

### Navegación
El sistema se integra perfectamente con el dashboard principal:

```html
<!-- En dashboard.html -->
<li><a th:href="@{/email}"><i class="fas fa-envelope"></i> Correos Masivos</a></li>

<!-- Tarjeta en dashboard -->
<div class="dashboard-card">
    <div class="icon"><i class="fas fa-envelope"></i></div>
    <h3>Correos Masivos</h3>
    <p>Sistema Activo</p>
    <p>Envío de correos electrónicos masivos</p>
</div>
```

## Funcionalidades JavaScript

### Drag & Drop de Archivos
```javascript
// Manejo de eventos drag & drop
['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
    fileDropArea.addEventListener(eventName, preventDefaults, false);
});

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;
    manejarArchivos(files);
}
```

### Validación SMTP en Tiempo Real
```javascript
function verificarConfiguracion() {
    fetch('/email/validar')
        .then(response => response.text())
        .then(data => {
            if (data.includes('✅')) {
                indicator.className = 'status-indicator status-ok';
                text.textContent = 'Configuración válida';
            } else {
                indicator.className = 'status-indicator status-error';
                text.textContent = 'Configuración inválida';
            }
        });
}
```

### Gestión de Archivos
```javascript
function manejarArchivos(files) {
    Array.from(files).forEach(archivo => {
        if (archivo.size > 10 * 1024 * 1024) {
            alert(`El archivo ${archivo.name} es demasiado grande`);
            return;
        }
        
        archivosSeleccionados.push(archivo);
        // Crear elemento visual del archivo
        const fileItem = crearElementoArchivo(archivo);
        listaArchivos.appendChild(fileItem);
    });
}
```

## Estilos Personalizados

### CSS Personalizado
**Archivo:** `src/main/resources/static/css/email-style.css`

**Características:**
- Variables CSS para colores consistentes
- Animaciones suaves (fadeIn, pulse, spin)
- Efectos hover mejorados
- Gradientes en botones
- Sombras dinámicas
- Estados de loading
- Responsive design

### Animaciones
```css
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.form-section {
    animation: fadeIn 0.6s ease-out;
}

.btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
```

## Mensajes de Usuario

### Tipos de Mensajes
- **success:** Envío exitoso con contador
- **warning:** Envío parcial con lista de errores
- **error:** Error completo con descripción
- **info:** Proceso asíncrono iniciado

### Ejemplos
```html
<!-- Éxito -->
<div class="alert alert-success">
    ✅ Correos enviados exitosamente: 25
</div>

<!-- Advertencia -->
<div class="alert alert-warning">
    ⚠️ Envío parcial: 23 enviados, 2 fallidos
    <ul>
        <li>Error enviando a email-invalido@: Invalid format</li>
        <li>Error enviando a otro@example.com: Connection timeout</li>
    </ul>
</div>
```

## Accesibilidad

### Características
- **Navegación por teclado** completa
- **Etiquetas ARIA** apropiadas
- **Contraste de colores** adecuado
- **Texto alternativo** en iconos
- **Focus visible** en elementos interactivos

### Implementación
```html
<label for="destinatariosTexto" class="form-label">
    Lista de correos electrónicos
    <small class="text-muted">(separados por comas o saltos de línea)</small>
</label>

<button type="submit" class="btn btn-primary" aria-describedby="submit-help">
    <i class="fas fa-paper-plane" aria-hidden="true"></i> 
    Enviar Ahora
</button>
<small id="submit-help" class="text-muted">Envío síncrono</small>
```

## Responsive Design

### Breakpoints
- **Desktop:** Sidebar fijo, vista previa lateral
- **Tablet:** Sidebar colapsable, vista previa abajo
- **Mobile:** Stack vertical, botones full-width

### Adaptaciones
```css
@media (max-width: 768px) {
    .sticky-top {
        position: relative !important;
        top: auto !important;
    }
    
    .btn {
        margin-bottom: 10px;
    }
}
```

## Seguridad

### Validaciones
- **CSRF Protection** habilitado por defecto
- **Validación de archivos** por tamaño y tipo
- **Sanitización de HTML** en vista previa
- **Límites de destinatarios** aplicados
- **Validación de emails** del lado cliente y servidor

### Implementación
```javascript
// Validación del formulario
document.getElementById('emailForm').addEventListener('submit', function(e) {
    const destinatarios = document.getElementById('destinatariosTexto').value.trim();
    if (!destinatarios) {
        e.preventDefault();
        alert('Por favor ingresa al menos un destinatario');
        return;
    }
    
    const emails = destinatarios.split(/[,\n\r]+/).filter(email => email.trim().length > 0);
    if (emails.length > 100) {
        e.preventDefault();
        alert('No puedes enviar a más de 100 destinatarios a la vez');
        return;
    }
});
```

## Próximas Mejoras

### Funcionalidades Planeadas
- [ ] Plantillas de correo predefinidas
- [ ] Programación de envíos
- [ ] Historial de envíos
- [ ] Estadísticas y métricas
- [ ] Importación de listas desde CSV/Excel
- [ ] Editor WYSIWYG para HTML
- [ ] Vista previa de correo en diferentes clientes
- [ ] Gestión de listas de distribución

¡Las vistas web están completamente funcionales y listas para usar! 🎨✨