# Verificacion del entorno publicado

Comprobacion del 10 de noviembre de 2026.

## Resultado publico observado

- `https://alphabike-frontend.vercel.app` muestra la interfaz, pero catalogo y galeria muestran error de conexion.
- El JavaScript compilado del frontend apunta a `https://alphabike-backend-production.up.railway.app/api/v1`.
- Esa URL responde HTTP 404 de Railway con `Application not found`. No es una respuesta del controlador Spring ni prueba que la base de datos falle: el dominio no esta sirviendo la aplicacion esperada.
- La URL antigua `https://alphabike-backend.vercel.app` tambien devuelve 404. No debe usarse como sustituto sin desplegar y comprobar el backend alli.
- La conexion Vercel disponible no tiene acceso al equipo propietario `itp89s-projects`. Railway solicita inicio de sesion; no hay credenciales Railway ni SMTP de produccion en los repositorios.

No se puede certificar MongoDB ni entrega de correos en ese entorno mientras el backend no sea accesible.

## Recuperar y verificar el servicio existente de Railway

1. Abrir el proyecto existente en Railway y localizar el servicio vinculado a `ITP89/alphabike-backend`, rama `master`.
2. Verificar que la raiz del servicio sea `backend`, donde estan `pom.xml` y `Dockerfile`. Revisar el ultimo despliegue y sus logs antes de cambiar configuracion.
3. Revisar Networking y el dominio publico realmente asignado. Si el servicio esta eliminado o detenido, recuperar/desplegar el servicio existente antes de cambiar la URL del frontend. No crear servicios de pago sin revisar el proyecto y sus recursos actuales.
4. Confirmar variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MONGODB_URI`, `HISTORIAL_ENABLED=true`, `APP_FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`, `WS_ALLOWED_ORIGINS`. Los origenes del frontend deben incluir `https://alphabike-frontend.vercel.app`.
5. Railway proporciona `PORT`; el backend ya lo utiliza. Los logs deben mostrar arranque de Spring y Flyway V27 sin errores. No publicar credenciales ni el contenido completo de variables en capturas o documentos.
6. Consultar `/api/v1/servicios` en el dominio real: debe devolver HTTP 200 y JSON `ApiResponse`.
7. En Vercel, comprobar `VITE_API_URL=https://DOMINIO_REAL/api/v1` y, si se define, `VITE_WS_URL=wss://DOMINIO_REAL/ws/citas`. Redeplegar el frontend: las variables Vite se incorporan al compilar.
8. Comprobar desde la web catalogo, inicio de sesion, CRUD, evento WebSocket en otra sesion e historial Mongo. El historial debe mostrar un nuevo evento persistido, no solo una lista vacia o un error 503.

## Correo real frente a correo probado localmente

La prueba `RegistroSmtpIntegrationTest` realiza HTTP de registro, recibe el correo mediante SMTP real en un servidor de captura local, extrae el enlace MIME, verifica la cuenta y confirma inicio de sesion; no envia nada a destinatarios externos.

Eso acredita el codigo SMTP, no la configuracion ni entrega a Gmail en produccion. Para esa comprobacion hacen falta `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, autenticacion/TLS y un remitente permitido por el proveedor (`APP_MAIL_FROM`). `APP_FRONTEND_URL` debe ser el dominio publico. Con permiso para enviar un correo a una direccion de prueba controlada por el usuario, verificar recepcion, enlace y activacion. No usar un cliente real ni considerar exitoso el envio porque register devolvio 200: el envio es asincrono.

## Concurrencia verificada y corregida

Una prueba reproducible contra PostgreSQL demostro que cancelar y pagar la misma cita simultaneamente podian confirmarse ambos. Ahora todas las mutaciones de cita y cotizacion, incluido el pago, adquieren el mismo bloqueo de fila sobre la cita antes de cargar y validar el estado mutable. El pago vuelve a comprobar duplicados despues del bloqueo.

`TallerConcurrencyIntegrationTest` verifica con transacciones realmente contendidas:

- Cancelar frente a pagar: solo una operacion se confirma.
- Cambiar el precio de la cotizacion frente a pagar el precio anterior: solo una se confirma.
- Dos pagos sobre la misma cotizacion: solo uno se confirma.

Se conserva el indice unico de pagos como segunda proteccion. No se promete haber probado todas las combinaciones de concurrencia posibles.
