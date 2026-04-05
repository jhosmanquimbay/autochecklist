# 🔐 CONFIGURAR SMTP CON VARIABLES DE ENTORNO (MEJOR PRÁCTICA)

> **Esto es más SEGURO** que poner contraseñas directamente en archivos

## Por qué usar variables de entorno?

- ✅ Las contraseñas NO aparecen en el código
- ✅ Puedes usar diferentes valores en desarrollo/producción
- ✅ No subes secretos a Git (seguridad)

---

## Opción A: Variables de entorno del sistema (Windows)

### 1. Abre el administrador de variables

**Windows 10/11:**
1. Presiona: `Win + Pausa`
2. Haz clic en: **Configuración avanzada del sistema**
3. Haz clic en: **Variables de entorno**
4. En **Variables del usuario**, haz clic en: **Nueva...**

### 2. Agregar cada variable

Crea estas 3 variables:

| Variable | Valor |
|----------|-------|
| `SPRING_MAIL_HOST` | `smtp.mailtrap.io` |
| `SPRING_MAIL_USERNAME` | (Tu username de Mailtrap) |
| `SPRING_MAIL_PASSWORD` | (Tu password de Mailtrap) |

### 3. Ejemplo en `application.properties`

```properties
spring.mail.host=${SPRING_MAIL_HOST:smtp.mailtrap.io}
spring.mail.port=465
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.enable=true
```

**Nota:** `${SPRING_MAIL_HOST:smtp.mailtrap.io}` = usa la variable, si no existe usa `smtp.mailtrap.io`

### 4. Reinicia VS Code

Para que tome las nuevas variables

---

## Opción B: Archivo `.env` (más simple para desarrollo)

### 1. Crea archivo `.env` en la raíz del proyecto

```
# .env (NO SUBIR A GIT)
SPRING_MAIL_HOST=smtp.mailtrap.io
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=abc123def456
SPRING_MAIL_PASSWORD=xyz789uvw123
```

### 2. Instala Spring Boot Plugin para leer `.env`

En `pom.xml`, agrega:

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>2.5.4</version>
</dependency>
```

### 3. Actualiza `application.properties`

```properties
spring.mail.host=${SPRING_MAIL_HOST}
spring.mail.port=${SPRING_MAIL_PORT}
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.enable=true
```

### 4. IMPORTANTE: Agrega `.env` a `.gitignore`

```
# .gitignore
.env
*.properties
```

---

## Opción C: Archivo `application-prod.properties` (para producción)

### 1. Crea: `src/main/resources/application-prod.properties`

```properties
# Producción: SendGrid
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=${SENDGRID_API_KEY}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 2. Al ejecutar, especifica el perfil

```bash
# Desarrollo (usa application.properties)
mvn spring-boot:run

# Producción (usa application-prod.properties)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

---

## ✅ RESUMEN - RECOMENDACIÓN

| Método | Uso | Seguridad |
|--------|-----|----------|
| Directo en `application.properties` | Pruebas locales | ⚠️ Baja |
| Variables de entorno del sistema | Desarrollo | ✅ Media |
| Archivo `.env` | Desarrollo local | ✅ Media (necesita .gitignore) |
| Perfil `application-prod.properties` | Producción | ✅ Alta (con variables de entorno) |

**RECOMENDADO PARA TI:** Opción A (variables de entorno del sistema) o Opción B (`.env`)

---

Pregunta si necesitas ayuda con cualquiera de estas opciones. 🚀
