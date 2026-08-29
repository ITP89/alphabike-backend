-- ============================================================
-- V14 - SEED DE PRODUCTOS REALES CON IMÁGENES EN SUPABASE
-- Actualiza productos existentes e inserta nuevos con fotos Unsplash
-- ============================================================

-- Insertar categorías si no existen
INSERT INTO tienda.categorias (id, nombre, descripcion)
VALUES 
  ('cat-frenos', 'Frenos', 'Frenos de disco hidráulicos y mecánicos'),
  ('cat-transmision', 'Transmisión', 'Cadenas, piñones y desviadores'),
  ('cat-accesorios', 'Accesorios', 'Tijas, cascos, pedales y luces'),
  ('cat-suspension', 'Suspensiones', 'Horquillas y amortiguadores'),
  ('cat-llantas', 'Llantas y Neumáticos', 'Cubiertas tubeless y aros')
ON CONFLICT (nombre) DO UPDATE SET descripcion = EXCLUDED.descripcion;

-- Actualizar productos antiguos de prueba para que sean activos y tengan imágenes reales
UPDATE tienda.productos
SET 
  nombre = 'Freno de Disco Hidráulico Shimano MT200',
  descripcion = 'Freno de disco hidráulico de 2 pistones para MTB. Incluye maneta, pinza y latiguillo purgado listo para instalar.',
  marca = 'Shimano',
  precio = 149.90,
  stock = 25,
  imagen_url = 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=800&q=80',
  estado = 'ACTIVO'
WHERE id = '562a9c65-9c35-42dc-af59-c6af0d8690bb' OR nombre LIKE '%Shimano%';

UPDATE tienda.productos
SET 
  nombre = 'Tija Telescópica Fox Transfer Factory 31.6mm',
  descripcion = 'Tija telescópica con recubrimiento Kashima de suave accionamiento hidráulico para Enduro y Trail.',
  marca = 'FOX',
  precio = 450.00,
  stock = 12,
  imagen_url = 'https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?auto=format&fit=crop&w=800&q=80',
  estado = 'ACTIVO'
WHERE id = '8cdb7dc1-d660-4102-8cef-e4b6a290a125' OR (nombre = 'Tija' AND estado = 'DESCONTINUADO');

-- Activar cualquier producto Tija restante
UPDATE tienda.productos
SET 
  nombre = 'Tija Aluminio Ultraligera Pro 30.9mm',
  descripcion = 'Tija de sillín en aluminio de alta resistencia 7075 T6.',
  marca = 'Pro',
  precio = 75.00,
  stock = 30,
  imagen_url = 'https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?auto=format&fit=crop&w=800&q=80',
  estado = 'ACTIVO'
WHERE nombre = 'Tija';

-- Activar Cadena KMC descontinuada
UPDATE tienda.productos
SET 
  nombre = 'Cadena KMC X11 11 Velocidades',
  descripcion = 'Cadena reforzada de 11 velocidades compatible con Shimano y SRAM.',
  marca = 'KMC',
  precio = 65.00,
  stock = 40,
  imagen_url = 'https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?auto=format&fit=crop&w=800&q=80',
  estado = 'ACTIVO'
WHERE nombre LIKE '%Cadena%';

-- Insertar más productos reales
INSERT INTO tienda.productos (id, categoria_id, nombre, descripcion, marca, precio, stock, imagen_url, estado)
VALUES
  (
    'prod-002',
    (SELECT id FROM tienda.categorias WHERE nombre = 'Accesorios' LIMIT 1),
    'Casco de Ciclismo Giro Register MIPS',
    'Casco para ciclismo urbano y montaña con sistema de protección MIPS integrado y ventilación optimizada.',
    'Giro',
    189.00,
    15,
    'https://images.unsplash.com/photo-1559348349-86f1f65817fe?auto=format&fit=crop&w=800&q=80',
    'ACTIVO'
  ),
  (
    'prod-005',
    (SELECT id FROM tienda.categorias WHERE nombre = 'Llantas y Neumáticos' LIMIT 1),
    'Llanta Maxxis Minion DHF 29x2.50 WT 3C',
    'Neumático delantero para MTB con agarre superior en curvas y compuesto 3C MaxxTerra.',
    'Maxxis',
    210.00,
    18,
    'https://images.unsplash.com/photo-1507035895480-2b3156c31fc8?auto=format&fit=crop&w=800&q=80',
    'ACTIVO'
  ),
  (
    'prod-006',
    (SELECT id FROM tienda.categorias WHERE nombre = 'Suspensiones' LIMIT 1),
    'Horquilla RockShox Pike Ultimate 150mm',
    'Horquilla de suspensión de 150mm de recorrido con cartucho Charger 3 RC2 para rodado 29.',
    'RockShox',
    1290.00,
    8,
    'https://images.unsplash.com/photo-1511994298241-608e28f14fde?auto=format&fit=crop&w=800&q=80',
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
