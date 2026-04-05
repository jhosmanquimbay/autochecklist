# 🐳 GUÍA DE DESPLIEGUE CON DOCKER

## 📋 REQUISITOS

- Docker Engine 20.10+
- Docker Compose 2.0+
- Git

## 🚀 INICIO RÁPIDO (DESARROLLO)

### 1️⃣ Clonar y Configurar

```bash
git clone https://github.com/tuorg/concesionario.git
cd concesionario
cp .env.example .env
```

### 2️⃣ Levantar servicios (Desarrollo)

```bash
docker-compose -f docker-compose.dev.yml up -d
```

Verifica que todo esté corriendo:
```bash
docker-compose -f docker-compose.dev.yml ps
```

### 3️⃣ Acceder a la aplicación

- **App**: http://localhost:8081
- **phpMyAdmin**: http://localhost:8080 (si agregaste)
- **Redis Commander**: http://localhost:8081 (si agregaste)

---

## 🏭 DESPLIEGUE A PRODUCCIÓN

### 1️⃣ Preparar ambiente

```bash
# Editar .env con credenciales reales
nano .env

# Asegurar que DB_PASSWORD y MAIL_PASSWORD son fuertes
```

### 2️⃣ Compilar imagen

```bash
docker build -t concesionario:1.0.0 .
```

### 3️⃣ Levantar stack completo

```bash
docker-compose up -d
```

Monitorear logs:
```bash
docker-compose logs -f app
```

### 4️⃣ Health Check

```bash
curl http://localhost:8081/actuator/health

# Respuesta esperada:
# {"status":"UP","components":{"database":{"status":"UP"},...}}
```

---

## 📊 MONITOREO Y MANTENIMIENTO

### Ver logs

```bash
# Todos los servicios
docker-compose logs

# Solo aplicación
docker-compose logs -f app

# Últimas 100 líneas
docker-compose logs --tail=100 app
```

### Acceder a base de datos

```bash
# Conectar a MySQL
docker-compose exec mysql mysql -u root -proot_password_prod auto

# Ejemplo: listar usuarios
SELECT * FROM usuario;
```

### Backup de Base de Datos

```bash
docker-compose exec mysql mysqldump -u root -proot_password_prod auto > backup.sql
```

### Restaurar Base de Datos

```bash
docker-compose exec -T mysql mysql -u root -proot_password_prod auto < backup.sql
```

---

## 🔧 COMANDOS ÚTILES

### Detener servicios

```bash
docker-compose down
```

### Detener y eliminar volúmenes (⚠️ perderás datos)

```bash
docker-compose down -v
```

### Reconstruir imagen

```bash
docker-compose build --no-cache
docker-compose up -d
```

### Ejecutar comando en contenedor

```bash
docker-compose exec app sh
```

### Ver estadísticas

```bash
docker stats
```

---

## 🔐 SEGURIDAD EN PRODUCCIÓN

### Variables críticas en .env

```bash
# Cambiar TODAS estas valores:
DB_USERNAME=app_user  # NO usar root
DB_PASSWORD=GENERATE_SECURE_PASSWORD
REDIS_PASSWORD=GENERATE_SECURE_PASSWORD
MAIL_PASSWORD=tu_app_password_real
```

### Certificado HTTPS

1. Generar keystore:
```bash
keytool -genkey -alias tomcat -storetype PKCS12 \
  -keyalg RSA -keysize 2048 -keystore keystore.p12 \
  -validity 365
```

2. Configurar en .env:
```bash
KEYSTORE_PATH=/path/to/keystore.p12
KEYSTORE_PASSWORD=tu_password
```

### Network aislada

```bash
# Los servicios corren en bridge network aislada
# Solo exponen puertos necesarios
```

---

## 📈 ESCALABILIDAD

### Múltiples instancias de app

```yaml
# Modificar docker-compose.yml
services:
  app-1:
    # ... configuración
  app-2:
    # ... configuración
    ports:
      - "8082:8081"
```

### Load Balancer (Nginx)

```dockerfile
# Agregar a docker-compose
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - app
```

---

## 🐛 TROUBLESHOOTING

### App no inicia

```bash
docker-compose logs app
# Revisar errores de conexión a BD
```

### Conexión a BD rechazada

```bash
docker-compose exec mysql mysql -h mysql -uroot -proot_password_prod -e "SELECT 1"
```

### Redis no responde

```bash
docker-compose exec redis redis-cli --requirepass redis_password_prod ping
```

### Puerto 8081 en uso

```bash
# Cambiar en docker-compose.yml
ports:
  - "8082:8081"  # Usar 8082
```

---

## 📝 CI/CD Integration

### GitHub Actions

```yaml
name: Build & Deploy

on: [push]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - run: docker build -t concesionario:latest .
      - run: docker-compose up -d
```

---

## 📞 SOPORTE

Para problemas específicos, revisar:
- Logs: `docker-compose logs`
- Health: http://localhost:8081/actuator/health
- Swagger: http://localhost:8081/swagger-ui.html

ISO 25010: ✅ Portabilidad alcanzada
