# Validación del Avance 2

Fecha de ejecución local: 9 de noviembre de 2026.

## Resultados

| Comprobación | Resultado |
|---|---|
| Suite Maven con H2 | 19 pruebas correctas, sin fallos ni errores |
| Integración HTTP y WebSocket con PostgreSQL | 5 pruebas correctas |
| Flyway sobre PostgreSQL nuevo | 25 migraciones aplicadas; Hibernate valida el esquema |
| Frontend ESLint y TypeScript | Correctos |
| Frontend Vitest | 10 pruebas correctas |
| Build de producción | Correcto |
| npm audit con umbral high | Sin alertas altas o críticas; cuatro moderadas en herramientas de desarrollo |
| npm audit --omit=dev | Sin vulnerabilidades reportadas |

Entorno local: Windows, Java 25 con objetivo de compilación Java 21, Maven 3.9.11, Node 24, Vite 8.3.7, Vitest 5.1 y PostgreSQL 18.6 en una base temporal exclusiva. El CI activo usa Java 21 y Node 22. El workflow propuesto en ci-postgres.example.yml agrega PostgreSQL 16; su activacion requiere permiso workflow en GitHub.

## Flujos comprobados

- Crear, leer, actualizar y eliminar un servicio por HTTP con persistencia.
- Validación de entrada inválida y respuestas HTTP coherentes.
- Lectura de proveedores por ENCARGADO; rechazo del mismo acceso para CLIENTE.
- Lectura y aceptación de la cotización propia; rechazo del acceso de otro cliente.
- Cobro interno de una cotización aceptada; rechazo de pago duplicado y de modificación posterior al pago.
- Cancelación de pedido por el endpoint administrativo y por el del cliente: stock restaurado una sola vez.
- Rechazo de editar precios o cancelar directamente un pedido pagado; preparación posterior con respuesta 200.
- WebSocket real con propietario, encargado y cliente ajeno: el último no recibe datos de otra cita.
- Rechazo de JWT inválido y de un Origin no permitido; AUTH/READY, PING/PONG y SYNC/CITAS_SYNC.
- UI del cliente actualizada mediante CITA_UPDATED, sin navegación ni recarga.

## Prueba manual de navegador

Se abrió `/citas` con un usuario sintético en PostgreSQL. La interfaz mostró «Citas en vivo» y una cita pendiente. Desde otra sesión autenticada se actualizó la cita mediante la API a EN_PROCESO. La página cambió a «En proceso» y mostró la notificación sin recargar.

Después se detuvo y reinició el proceso del backend conservando la misma base PostgreSQL. El indicador cambió a reconexión y volvió a conectado; la cita conservó su estado. La reconciliación solicita de nuevo el estado persistido por REST.

![Cita actualizada en vivo](citas-en-vivo.png)

## Límites

Estas comprobaciones acreditan los flujos del Avance 2, no una auditoría completa de producción. No se probaron carga masiva, despliegue multiinstancia ni proveedor bancario real. El proyecto conserva el sandbox de pagos académico. La exposición oral y la aprobación de los PR deben realizarlas las personas responsables del trabajo.
