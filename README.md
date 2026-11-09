# AlphaBike Backend

API REST de AlphaBike construida con Java 21, Spring Boot, Spring Security, JWT, JPA, PostgreSQL y Flyway.

## Seguridad urgente

Las credenciales de Supabase y el JWT secret estuvieron publicadas en `application.properties`. Deben rotarse antes de volver a usar el entorno:

- Cambiar la password del usuario de Supabase/PostgreSQL.
- Generar un `JWT_SECRET` nuevo, aleatorio y de al menos 32 caracteres.
- Invalidar tokens emitidos con el secreto anterior.
- Revisar el historial Git si el repositorio fue publico.

## Variables requeridas

Copia `backend/.env.example` a `backend/.env` y configura tu PostgreSQL. También puedes exportar variables del sistema; hay un ejemplo ampliado en `backend/src/main/resources/application-example.properties`.

| Variable | Descripcion |
| --- | --- |
| `DB_URL` | JDBC URL de PostgreSQL. |
| `DB_USERNAME` | Usuario de base de datos. |
| `DB_PASSWORD` | Password de base de datos. |
| `JWT_SECRET` | Secreto JWT de al menos 32 caracteres. |
| `JWT_EXPIRATION_MS` | Expiracion del token en milisegundos. Default: `86400000`. |
| `SERVER_PORT` | Puerto del backend. Default: `8080`. |
| `WS_ALLOWED_ORIGINS` | Origenes permitidos para `/ws/citas`; hereda `CORS_ALLOWED_ORIGINS` si se omite. |
| `CORS_ALLOWED_ORIGINS` | Origenes permitidos separados por coma. Default: `http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174`. |
| `JPA_DDL_AUTO` | Modo Hibernate. Default: `validate`. |
| `FLYWAY_ENABLED` | Habilita migraciones. Default: `true`. |
| `APP_SEED_ADMIN_ENABLED` | Crea/restaura un admin de desarrollo. Default: `false`. |
| `APP_SEED_ADMIN_EMAIL` | Email del admin seed. Default: `admin@alphabike.com`. |
| `APP_SEED_ADMIN_PASSWORD` | Password del admin seed. Obligatoria si el seed esta activo. |
| `APP_SEED_ADMIN_NAME` | Nombre del admin seed. |
| `APP_SEED_ADMIN_PHONE` | Telefono del admin seed. |
| `SPRINGDOC_API_DOCS_ENABLED` | Habilita `/v3/api-docs`. Default: `false`. |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | Habilita Swagger UI. Default: `false`. |
| `APP_FRONTEND_URL` | URL del frontend para verificacion y recuperacion. |
| `MAIL_HOST`, `MAIL_PORT` | Servidor y puerto SMTP. |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Credenciales SMTP; no deben subirse a Git. |
| `APP_MAIL_FROM` | Remitente de los correos de AlphaBike. |
| `APP_MAIL_LOG_LINKS` | Permite imprimir enlaces con token solo en desarrollo. Default: `false`. |

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

Swagger queda disponible en el perfil `demo` o habilitando `SPRINGDOC_API_DOCS_ENABLED=true` y `SPRINGDOC_SWAGGER_UI_ENABLED=true`:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## Tests

```bash
cd backend
mvn test
```

Los tests usan perfil `test`, H2 en memoria y Flyway deshabilitado.

## Integridad de compras y pagos

- El registro no entrega una sesion hasta que el correo haya sido verificado.
- Los pedidos pendientes pueden cancelarse con `PATCH /api/v1/pedidos/{id}/cancelar`; la operacion devuelve el stock reservado.
- El backend valida propiedad, estado, monto exacto y pago duplicado antes de registrar un pago.
- La migracion `V25` impide dos pagos aprobados para una misma referencia incluso ante solicitudes concurrentes.
- El frontend usa un proveedor `SANDBOX` para la demostracion academica. Para cobrar dinero real debe integrarse un proveedor externo y conservarse la validacion del lado servidor.

## Docker

Con `alphabike-backend` y `alphabike-frontend` clonados como carpetas hermanas, ejecuta desde `alphabike-backend`:

```bash
docker compose up --build
```

Servicios:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Migraciones

Las migraciones viven en `backend/src/main/resources/db/migration`. La migracion inicial crea los esquemas base sin borrar datos. Los resets destructivos deben hacerse manualmente solo en entornos locales.


## Avance 2 Semana 14

Implementa CRUD conectado a PostgreSQL, validación, errores centralizados y notificaciones bidireccionales por WebSocket nativo de Spring. El cliente recibe cambios de sus citas; el personal autorizado recibe cambios del taller. Las notificaciones se publican después del commit y la interfaz recupera el estado persistido por REST.

- [Modelo de datos completo y esquemas](docs/diagrama-er.md).
- [Protocolo, variables, arquitectura y guion de exposición](docs/avance-2.md).
- `ws://localhost:8080/ws/citas` en desarrollo, `wss://` con HTTPS.
- Primer frame: `{"type":"AUTH","token":"JWT"}`. Respuesta `READY`; `SYNC` → `CITAS_SYNC`; `PING` → `PONG`.
- Evento de cambio: `CITA_UPDATED`, con `citaId` y `estado`. Sin tokens en URLs ni eventos de otros clientes.
- Mantener backend y frontend en procesos separados o usar Docker Compose.

### Diagrama entidad relación

```mermaid
erDiagram
    USUARIOS ||--o{ PEDIDOS : compra
    USUARIOS ||--o{ CITAS : agenda
    USUARIOS o|--o{ CITAS : atiende
    CATEGORIAS ||--o{ PRODUCTOS : clasifica
    PROVEEDORES o|--o{ PRODUCTOS : suministra
    PEDIDOS ||--|{ DETALLE_PEDIDOS : contiene
    PRODUCTOS ||--o{ DETALLE_PEDIDOS : vendido
    SERVICIOS ||--o{ CITAS : solicitado
    CITAS ||--o| COTIZACIONES : presupuesto
    CITAS o|--o{ TRABAJOS_REALIZADOS : documenta
```

Los pagos apuntan a pedido o cotización por tipo e ID; se validan en servicio y tienen unicidad de pago confirmado en PostgreSQL.

### Pruebas de integración PostgreSQL

La suite rápida usa H2. El workflow propuesto en [ci-postgres.example.yml](docs/ci-postgres.example.yml) agrega `Avance2IntegrationTest` con PostgreSQL 16, migraciones reales y `ddl-auto=validate`. La credencial disponible no permite modificar workflows: el archivo está listo para incorporarlo a `.github/workflows/ci.yml` con permiso `workflow`. Para repetir la prueba localmente en una base **exclusiva de pruebas** configura:

```properties
TEST_DB_URL=jdbc:postgresql://localhost:5432/alphabike_test
TEST_DB_USERNAME=alphabike
TEST_DB_PASSWORD=your-local-test-password
TEST_DB_DRIVER=org.postgresql.Driver
TEST_DB_DIALECT=org.hibernate.dialect.PostgreSQLDialect
TEST_SQL_INIT_MODE=never
TEST_DDL_AUTO=validate
TEST_FLYWAY_ENABLED=true
```

Exporta esas variables en tu shell y ejecuta desde `backend`:

```bash
mvn -B -Dtest=Avance2IntegrationTest test
```

La suite crea datos de prueba. Nunca apuntar TEST_DB_URL a producción. Las pruebas cubren CRUD HTTP, cotizaciones aceptadas, cancelación idempotente, permisos y conexiones WebSocket reales con aislamiento entre usuarios.
