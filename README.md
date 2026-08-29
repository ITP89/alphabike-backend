# AlphaBike Backend

API REST de AlphaBike construida con Java 21, Spring Boot, Spring Security, JWT, JPA, PostgreSQL y Flyway.

## Seguridad urgente

Las credenciales de Supabase y el JWT secret estuvieron publicadas en `application.properties`. Deben rotarse antes de volver a usar el entorno:

- Cambiar la password del usuario de Supabase/PostgreSQL.
- Generar un `JWT_SECRET` nuevo, aleatorio y de al menos 32 caracteres.
- Invalidar tokens emitidos con el secreto anterior.
- Revisar el historial Git si el repositorio fue publico.

## Variables requeridas

Puedes exportarlas en el sistema o crear `backend/.env` usando como guia `backend/src/main/resources/application-example.properties`.

| Variable | Descripcion |
| --- | --- |
| `DB_URL` | JDBC URL de PostgreSQL. |
| `DB_USERNAME` | Usuario de base de datos. |
| `DB_PASSWORD` | Password de base de datos. |
| `JWT_SECRET` | Secreto JWT de al menos 32 caracteres. |
| `JWT_EXPIRATION_MS` | Expiracion del token en milisegundos. Default: `86400000`. |
| `SERVER_PORT` | Puerto del backend. Default: `8080`. |
| `CORS_ALLOWED_ORIGINS` | Origenes permitidos separados por coma. Default: `http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174`. |
| `JPA_DDL_AUTO` | Modo Hibernate. Default: `validate`. |
| `FLYWAY_ENABLED` | Habilita migraciones. Default: `true`. |
| `APP_SEED_ADMIN_ENABLED` | Crea/restaura un admin de desarrollo. Default: `false`. |
| `APP_SEED_ADMIN_EMAIL` | Email del admin seed. Default: `admin@alphabike.com`. |
| `APP_SEED_ADMIN_PASSWORD` | Password del admin seed. Obligatoria si el seed esta activo. |
| `APP_SEED_ADMIN_NAME` | Nombre del admin seed. |
| `APP_SEED_ADMIN_PHONE` | Telefono del admin seed. |

## Admin local de desarrollo

La migracion inicial crea tablas, pero no crea usuarios. Para recuperar un admin local de prueba, agrega estas variables en `backend/.env` y reinicia el backend:

```properties
APP_SEED_ADMIN_ENABLED=true
APP_SEED_ADMIN_EMAIL=admin@alphabike.com
APP_SEED_ADMIN_PASSWORD=admin1234
APP_SEED_ADMIN_NAME=Administrador AlphaBike
APP_SEED_ADMIN_PHONE=999999999
```

Ese seed solo debe usarse en desarrollo. En produccion o Supabase real, crea/restaura el usuario admin con una contrasena nueva y privada.

## Ejecutar local

```bash
cd backend
mvn spring-boot:run
```

## Ejecutar sin Supabase

Si no tienes las credenciales de Supabase, usa el perfil `demo`. Este perfil levanta una base H2 local en `backend/data`, crea datos de prueba y restaura este admin:

```txt
admin@alphabike.com
admin1234
```

Comando:

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Este modo es solo para desarrollo o presentacion local. No reemplaza PostgreSQL/Supabase para produccion.

Swagger queda disponible en:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## Tests

```bash
cd backend
mvn test
```

Los tests usan perfil `test`, H2 en memoria y Flyway deshabilitado.

## Docker

Desde el directorio padre que contiene ambos repositorios:

```bash
docker compose up --build
```

Servicios:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Migraciones

Las migraciones viven en `backend/src/main/resources/db/migration`. La migracion inicial es `V1__init_schema.sql`.
