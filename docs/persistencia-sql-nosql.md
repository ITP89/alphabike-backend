# Persistencia con PostgreSQL y MongoDB

El requisito adicional indicado por el profesor es **utilizar ambas bases de datos**. Esta implementacion usa PostgreSQL para operaciones de negocio y MongoDB para consultar el historial de estados registrados de las citas. No se sustituye PostgreSQL ni se replica toda la base relacional.

## Modelo y flujo real

```mermaid
flowchart LR
  UI[Detalle de cita: cliente, encargado o admin] --> API[API autenticada]
  API --> PG[(PostgreSQL: citas y outbox)]
  PG --> W[Worker con reintentos]
  W --> M[(MongoDB: cita_eventos)]
  API --> M
  PG --> WS[WebSocket despues del commit]
```

Documento en `alphabike.cita_eventos`:

```json
{
  "_id": "UUID estable del evento SQL",
  "citaId": "UUID de la cita en PostgreSQL",
  "estado": "EN_PROCESO",
  "ocurridoEn": {"$date": "2026-11-10T15:00:00Z"}
}
```

`_id` es unico. El indice `cita_historial_fecha` cubre `citaId`, `ocurridoEn` descendente y `_id` descendente. No existe FK entre motores; el acceso y la existencia de la cita se comprueban en PostgreSQL antes de consultar MongoDB. El documento no guarda correo, contrasenas ni datos de pago.

Cada `CitaChanged` de creacion, estado, asignacion o cotizacion/pago que actualiza la cita deja un estado registrado. No es una auditoria completa de actores ni de todos los campos: pueden existir dos eventos consecutivos con el mismo estado. La fecha corresponde al registro del evento. Los documentos se escriben por el backend; el cliente no puede alterarlos ni borrarlos.

## Consistencia y recuperacion

1. La transaccion de negocio guarda la cita y un evento en `taller.historial_outbox` (listener `BEFORE_COMMIT`). Si hace rollback, ninguno se confirma.
2. Un worker procesa hasta 50 pendientes cada dos segundos, escribe en MongoDB con el mismo ID y solo despues borra el pendiente SQL.
3. Si Mongo falla, el pendiente permanece. Si el proceso cae despues de escribir en Mongo pero antes de borrar en SQL, el reintento reemplaza el mismo documento: no crea duplicados.
4. No hay transaccion distribuida entre motores. El historial tiene **consistencia eventual** y la interfaz consulta cada cinco segundos. La cita y el WebSocket pueden actualizarse antes de que aparezca el evento.

Flyway **V27** crea la outbox y agrega una instantanea inicial por cada cita preexistente. No inventa estados anteriores: la fecha de esa instantanea es la fecha de la migracion. Las citas nuevas generan eventos desde su creacion. No se necesitan documentos ficticios ni pasos manuales para iniciar la coleccion.

## Ejecutar ambos motores

Con los repositorios como carpetas hermanas, desde `alphabike-backend`:

```bash
docker compose up --build -d
docker compose ps
docker compose logs --tail=80 backend
```

El compose arranca PostgreSQL 16, MongoDB 8.0 con autenticacion, backend y frontend. Los dos motores tienen volumen persistente. No usar `docker compose down -v` si se desean conservar los datos. Las credenciales del compose son solo de desarrollo; en otro entorno usar secretos propios y no exponer las bases a Internet.

Para ejecutar Java directamente, configurar `backend/.env` con los valores PostgreSQL ya documentados y:

```dotenv
MONGODB_URI=mongodb://alphabike:alphabike_mongo_dev_password@localhost:27017/alphabike?authSource=admin&serverSelectionTimeoutMS=2000&connectTimeoutMS=2000&socketTimeoutMS=3000
HISTORIAL_ENABLED=true
```

Puede arrancarse solo la infraestructura con `docker compose up -d postgres mongo` y luego ejecutar `mvn spring-boot:run` desde `backend/`. El perfil H2 `demo` es una alternativa local, pero **no demuestra PostgreSQL + MongoDB**: para esta entrega usar la configuracion PostgreSQL normal. MongoDB no se configura en Vite ni debe recibir conexiones directas desde el navegador.

## Consulta y permisos

`GET /api/v1/citas/{id}/historial?pagina=0`, JWT requerido. Respuesta `ApiResponse` con hasta 50 documentos, mas recientes primero. `pagina` admite 0 a 10000. El frontend ofrece navegacion entre paginas.

- Cliente: solo sus citas. Encargado/admin: las citas que ya pueden consultar en el sistema.
- Cita inexistente: 404; ajena: 403; sin autenticacion: 403, conforme al filtro de seguridad actual.
- Mongo no disponible o historial deshabilitado: 503 con mensaje recuperable; las operaciones SQL siguen funcionando y se mantienen pendientes.
- Sin eventos: lista vacia, no una afirmacion de que Mongo esta sincronizado.

La aplicacion puede arrancar aunque Mongo no este disponible. **Arrancar Java por si solo no prueba que se esten usando ambos motores.** Verificar el endpoint y la coleccion; `HISTORIAL_ENABLED=false` sirve para aislar pruebas, pero no cumple la entrega con ambas bases.

## Evidencia reproducible

1. Crear una cita desde un cliente; abrir su detalle y el de un encargado/admin.
2. Cambiar a `EN_PROCESO`. Esperar unos segundos o pulsar `Actualizar historial`.
3. Confirmar eventos `PENDIENTE` y `EN_PROCESO` en la interfaz y en Mongo:

```bash
docker compose exec mongo mongosh --username alphabike --password alphabike_mongo_dev_password --authenticationDatabase admin alphabike --eval 'db.cita_eventos.find().sort({ocurridoEn:-1}).limit(5).toArray()'
```

4. Reiniciar los servicios y confirmar que los eventos siguen disponibles.
5. Detener Mongo (`docker compose stop mongo`), cambiar otra cita y consultar `SELECT count(*) FROM taller.historial_outbox;`. Reiniciar Mongo (`docker compose start mongo`): los pendientes deben drenarse y aparecer una sola vez en Mongo.

Pruebas automatizadas:

```bash
# Suite normal: SQL/H2, HTTP, WebSocket y worker con fallo/reintento.
mvn -B test
# Motor Mongo real; admite las mismas TEST_DB_* de CI para usar PostgreSQL.
MONGO_TEST_URI='mongodb://localhost:27017/alphabike_test?serverSelectionTimeoutMS=2000' mvn -B -Dtest=HistorialMongoIntegrationTest test
```

En PowerShell: `$env:MONGO_TEST_URI='mongodb://localhost:27017/alphabike_test?serverSelectionTimeoutMS=2000'` y luego ejecutar Maven. Sin esa variable, las dos pruebas de integracion Mongo se omiten explicitamente. El job PostgreSQL de GitHub Actions levanta **ambos servidores reales**, aplica Flyway y ejecuta las pruebas Mongo junto a las pruebas HTTP/WebSocket existentes.

Limites: historial anterior a V27 no reconstruible; no contiene actor ni diferencias de campos; eventos confirmados se conservan sin TTL; una caida prolongada aumenta la cola SQL y requiere supervision de almacenamiento. La paginacion por desplazamiento puede mover filas si entran eventos mientras se navega; usar `Mas recientes` para actualizar la primera pagina.
