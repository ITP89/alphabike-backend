# Desplegar el backend en Render

El repositorio incluye `render.yaml` para un Web Service Docker gratuito, rama master. El frontend puede seguir en Vercel. Este archivo **prepara** el despliegue; no crea una cuenta ni acredita un servicio ya desplegado.

## Infraestructura y datos

- PostgreSQL externo persistente: puede reutilizarse el existente si sigue disponible y se dispone de credenciales vigentes. No reemplazar ni borrar una base real para desplegar.
- MongoDB externo persistente: usar la instancia existente o una administrada, por ejemplo MongoDB Atlas. Configurar acceso de red para Render y un usuario con permisos en la base AlphaBike.
- No ejecutar MongoDB dentro del contenedor web gratuito: su almacenamiento local es efimero.
- Render Postgres gratuito expira a los 30 dias. Si se elige esa opcion, preparar respaldo y continuidad antes del vencimiento. Migrar datos requiere una operacion separada y verificada; Flyway crea/actualiza esquemas, no traslada automaticamente datos de Railway.

## Crear desde Blueprint

1. Iniciar sesion en https://dashboard.render.com y elegir New > Blueprint.
2. Conectar `ITP89/alphabike-backend`, rama `master`, y usar `render.yaml` de la raiz.
3. Revisar que el servicio sea Free. El Blueprint no provisiona bases ni otros recursos de pago.
4. Completar los valores privados solicitados:

| Variable | Valor esperado |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST:PUERTO/BASE?sslmode=require` si el proveedor requiere TLS. No pegar una URL `postgres://` directamente |
| `DB_USERNAME` | Usuario PostgreSQL |
| `DB_PASSWORD` | Contrasena PostgreSQL |
| `MONGODB_URI` | URI de MongoDB con autenticacion, base de datos y parametros del proveedor |

`JWT_SECRET` se genera en Render. Los tokens del hosting anterior dejaran de servir: los usuarios deben iniciar sesion otra vez. No usar perfil `demo`, porque sustituye PostgreSQL por H2.

Si se configura manualmente en lugar de Blueprint: Language Docker, Dockerfile Path `./backend/Dockerfile`, Docker Build Context Directory `./backend`, sin cambiar Root Directory. El Dockerfile ya compila Java 21 y arranca el JAR. El backend toma `PORT` de Render automaticamente; no se necesita un comando Maven en Start Command.

## Registro y correo: condicion del plan gratuito

Render Free bloquea trafico saliente a puertos SMTP 25, 465 y 587. La configuracion Gmail SMTP actual (587) **no funcionara en ese plan**. No considerar verificado el registro publico hasta resolver esta dependencia.

El backend incluye transporte HTTPS con Resend. Configurar en las variables privadas de Render:

```dotenv
APP_MAIL_PROVIDER=resend
RESEND_API_KEY=<clave privada de envio>
APP_MAIL_FROM=AlphaBike <cuentas@DOMINIO_VERIFICADO>
```

La cuenta de Resend debe verificar el dominio del remitente. No usar la direccion Gmail como si fuera un dominio propio. Sin dominio verificado, el remitente de prueba de Resend tiene restricciones de destinatarios y no completa el registro publico general. No se habilita el proveedor sin credenciales validas. Mantener la clave fuera de Git y del frontend.

El cliente usa HTTPS, timeout de conexion de 5 segundos y lectura de 10 segundos. Un rechazo de Resend no se registra como entrega ni dispara otro envio SMTP. No se imprimen respuestas del proveedor, claves ni tokens en produccion. Las pruebas usan un servidor HTTP local y cubren activacion, recuperacion, nombre HTML escapado y rechazos.

Tambien se puede usar un proveedor SMTP que admita un puerto permitido (por ejemplo 2525, si su documentacion lo soporta), o un plan compatible con el SMTP requerido. No se crea ni contrata un proveedor automaticamente.

Para usar un SMTP compatible, agregar en Render `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `APP_MAIL_FROM` y los valores TLS/autenticacion indicados por el proveedor. Mantener `APP_MAIL_LOG_LINKS=false`; no publicar enlaces de activacion. La prueba local SMTP del repositorio verifica el codigo, no las restricciones de red ni la entrega externa de Render.

## Conectar el frontend y comprobar

Cuando Render indique Live, obtener su dominio real, sin inventarlo a partir del nombre del servicio. Comprobar `https://DOMINIO_REAL/api/v1/servicios`: HTTP 200 con JSON.

En el proyecto frontend de Vercel actualizar:

```dotenv
VITE_API_URL=https://DOMINIO_REAL/api/v1
VITE_WS_URL=wss://DOMINIO_REAL/ws/citas
```

Redeplegar el frontend para incorporar esas variables. Luego comprobar catalogo, login, CRUD, dos sesiones con WebSocket y un nuevo evento del historial Mongo. Comprobar registro/activacion con correo real solo despues de configurar el proveedor y una direccion de prueba autorizada.

El health check de servicios valida la respuesta HTTP y el acceso relacional, **no certifica MongoDB ni SMTP**. Comprobarlos por sus flujos correspondientes.

## Limites relevantes para la entrega

Render Free entra en reposo tras 15 minutos sin trafico HTTP o mensajes WebSocket. La primera peticion posterior puede tardar alrededor de un minuto en reactivar el servicio. Tambien tiene cuotas mensuales; no promete disponibilidad continua. Verificar el arranque antes de la evaluacion y no almacenar datos persistentes en el disco del servicio web.

Fuentes oficiales verificadas el 10/11/2026:

- https://render.com/docs/free
- https://render.com/docs/docker
- https://render.com/docs/blueprint-spec
- https://render.com/docs/websocket

- API de correo HTTPS: https://resend.com/docs/api-reference/emails/send-email

## Mercado Pago Checkout Pro para pedidos de la tienda

La integracion viene desactivada. Validarla primero con un backend y una base
de datos de pruebas. En Mercado Pago Developers, crear una aplicacion Checkout
Pro con Orders para un vendedor de Peru. En su configuracion de Webhooks,
seleccionar **Prueba**, indicar la URL HTTPS del backend con el sufijo
`/api/v1/mercado-pago/webhook` y habilitar **Order (Mercado Pago)**.

Configurar en el backend de pruebas:

- `MP_CHECKOUT_ENABLED=true`
- `MP_LIVE=false`
- `MP_ACCESS_TOKEN`: token de prueba de la aplicacion.
- `MP_WEBHOOK_SECRET`: firma secreta de Webhooks.
- `MP_SELLER_ID`: identificador numerico del vendedor devuelto por la API.
- `APP_FRONTEND_URL`: origen HTTPS del frontend de pruebas.

Guardar las credenciales solo en variables privadas del backend. Publicar primero
el backend con la migracion V28 y luego el frontend correspondiente. Probar con
una tarjeta de prueba y comprobar en el panel de Mercado Pago que se recibe el
webhook y en AlphaBike que el pedido pasa a pagado una sola vez. Repetir con
una tarjeta rechazada y verificar que el pedido nunca se marca como pagado.

Para habilitar cobros reales, usar la cuenta de vendedor de la empresa, las
credenciales de produccion, `MP_LIVE=true` y el webhook de produccion. Los
medios de pago disponibles dependen de Mercado Pago. El alcance es el checkout
de pedidos de tienda. Reembolsos y contracargos se gestionan en Mercado Pago
y se concilian manualmente con AlphaBike.

- [Pruebas con Checkout Pro](https://www.mercadopago.com.pe/developers/es/docs/checkout-pro-orders/integration-test/test-purchase-with-card)
- [Configuracion de notificaciones](https://www.mercadopago.com.pe/developers/es/docs/checkout-pro-orders/notifications)
