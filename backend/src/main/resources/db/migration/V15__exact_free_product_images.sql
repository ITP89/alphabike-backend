-- ============================================================
-- V15 - IMÁGENES LIBRES DE DERECHOS EXACTAS Y RELACIONADAS
-- ============================================================

-- Freno de Disco Hidráulico
UPDATE tienda.productos
SET 
  imagen_url = 'https://images.unsplash.com/photo-1511994298241-608e28f14fde?auto=format&fit=crop&w=800&q=80',
  descripcion = 'Freno de disco hidráulico de 2 pistones para MTB. Excelente potencia de frenado y latiguillo purgado.'
WHERE nombre LIKE '%Freno%';

-- Casco de Ciclismo
UPDATE tienda.productos
SET 
  imagen_url = 'https://images.unsplash.com/photo-1559348349-86f1f65817fe?auto=format&fit=crop&w=800&q=80',
  descripcion = 'Casco de ciclismo aerodinámico con protección contra impactos y ranuras de ventilación.'
WHERE nombre LIKE '%Casco%';

-- Cadena y Transmisión
UPDATE tienda.productos
SET 
  imagen_url = 'https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?auto=format&fit=crop&w=800&q=80',
  descripcion = 'Cadena de alta durabilidad para transmisiones de 11 y 12 velocidades con eslabón rápido.'
WHERE nombre LIKE '%Cadena%';

-- Tija Telescópica y Sillín
UPDATE tienda.productos
SET 
  imagen_url = 'https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?auto=format&fit=crop&w=800&q=80',
  descripcion = 'Tija ajustable de aluminio de alta calidad para un ajuste perfecto de altura.'
WHERE nombre LIKE '%Tija%';

-- Llanta y Neumático MTB
UPDATE tienda.productos
SET 
  imagen_url = 'https://images.unsplash.com/photo-1507035895480-2b3156c31fc8?auto=format&fit=crop&w=800&q=80',
  descripcion = 'Neumático de montaña con tacos de alto agarre para terrenos secos y húmedos.'
WHERE nombre LIKE '%Llanta%' OR nombre LIKE '%Neumático%';

-- Horquilla de Suspensión
UPDATE tienda.productos
SET 
  imagen_url = 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=800&q=80',
  descripcion = 'Horquilla de suspensión delantera con cartucho de aire regulable para absorbencia de impactos.'
WHERE nombre LIKE '%Horquilla%' OR nombre LIKE '%Suspensión%';

-- Insertar Pedales y Luz si no existen
INSERT INTO tienda.productos (id, categoria_id, nombre, descripcion, marca, precio, stock, imagen_url, estado)
VALUES
  (
    'prod-007',
    (SELECT id FROM tienda.categorias WHERE nombre = 'Accesorios' LIMIT 1),
    'Pedales de Plataforma Aluminio Pro',
    'Pedales de plataforma de aluminio CNC con pines antideslizantes para ciclismo de montaña.',
    'Crankbrothers',
    135.00,
    22,
    'https://images.unsplash.com/photo-1571068316344-75bc76f77890?auto=format&fit=crop&w=800&q=80',
    'ACTIVO'
  ),
  (
    'prod-008',
    (SELECT id FROM tienda.categorias WHERE nombre = 'Accesorios' LIMIT 1),
    'Luz LED Delantera Recargable 900 Lumens',
    'Luz de alta potencia recargable por USB para conducción nocturna segura en ciudad y trocha.',
    'Knog',
    99.00,
    40,
    'https://images.unsplash.com/photo-1517649763962-0c623266010b?auto=format&fit=crop&w=800&q=80',
    'ACTIVO'
  )
ON CONFLICT (id) DO UPDATE SET
  nombre = EXCLUDED.nombre,
  descripcion = EXCLUDED.descripcion,
  marca = EXCLUDED.marca,
  precio = EXCLUDED.precio,
  stock = EXCLUDED.stock,
  imagen_url = EXCLUDED.imagen_url,
  estado = EXCLUDED.estado;
