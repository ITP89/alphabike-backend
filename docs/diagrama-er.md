# Modelo de datos de AlphaBike

PostgreSQL organiza las tablas en cuatro esquemas. Las claves son UUID representados como VARCHAR y los importes usan NUMERIC(10,2).

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

    USUARIOS {
        varchar id PK
        varchar nombre
        varchar email UK
        varchar password_hash
        varchar telefono
        varchar rol
        varchar estado
        boolean email_verificado
        timestamp fecha_registro
    }
    CATEGORIAS {
        varchar id PK
        varchar nombre UK
        varchar descripcion
    }
    PROVEEDORES {
        varchar id PK
        varchar nombre
        varchar ruc UK
        varchar email
        varchar telefono
        varchar direccion
        varchar estado
    }
    PRODUCTOS {
        varchar id PK
        varchar categoria_id FK
        varchar proveedor_id FK
        varchar nombre
        varchar marca
        numeric precio
        numeric precio_minimo_venta
        integer stock
        text imagen_url
        varchar estado
    }
    PEDIDOS {
        varchar id PK
        varchar cliente_id FK
        timestamp fecha
        varchar estado
        varchar tipo_entrega
        varchar direccion_entrega
        varchar numero_seguimiento
        numeric costo_envio
        numeric total
    }
    DETALLE_PEDIDOS {
        varchar id PK
        varchar pedido_id FK
        varchar producto_id FK
        integer cantidad
        numeric precio_lista
        numeric precio_acordado
        numeric subtotal
    }
    SERVICIOS {
        varchar id PK
        varchar nombre UK
        varchar descripcion
        numeric precio_base
        integer duracion_min
    }
    CITAS {
        varchar id PK
        varchar cliente_id FK
        varchar encargado_id FK
        varchar servicio_id FK
        date fecha
        time hora
        varchar estado
        varchar bici_descripcion
        varchar observaciones
    }
    COTIZACIONES {
        varchar id PK
        varchar cita_id FK,UK
        varchar descripcion
        numeric monto
        timestamp fecha_emision
        varchar estado
    }
    TRABAJOS_REALIZADOS {
        varchar id PK
        varchar cita_id FK
        varchar titulo
        varchar descripcion
        varchar imagen_antes_url
        varchar imagen_despues_url
        date fecha
        boolean destacado
    }
    PAGOS {
        varchar id PK
        varchar referencia_tipo
        varchar referencia_id
        numeric monto
        varchar metodo_pago
        varchar estado
        timestamp fecha
        varchar codigo_autorizacion
        varchar transaccion_id
    }
```

| Esquema | Tablas |
|---|---|
| auth_app | usuarios |
| tienda | categorias, proveedores, productos, pedidos, detalle_pedidos |
| taller | servicios, citas, cotizaciones, trabajos_realizados |
| pagos | pagos |

`pagos.referencia_id` es una referencia polimórfica a pedido o cotización según `referencia_tipo`; no es una FK física. La existencia y propiedad se validan en PagoService. V25 aplica un índice único parcial sobre tipo e ID cuando el estado es PAGADO.

Las migraciones V1–V26 están en `backend/src/main/resources/db/migration`. La separación en esquemas se realiza en V12. No editar migraciones ya aplicadas ni usar `ddl-auto=create` en una base con datos reales. Las tablas del diagrama omiten algunos campos secundarios, como tokens temporales, para facilitar su lectura.

V26 agrega a pedidos `operacion_id` (unico y nullable) y `solicitud_hash` para ventas presenciales. El identificador permite recuperar la misma venta ante reintentos; el hash impide reutilizarlo con un contenido diferente. Los pedidos previos y los creados por checkout mantienen esos campos nulos.
