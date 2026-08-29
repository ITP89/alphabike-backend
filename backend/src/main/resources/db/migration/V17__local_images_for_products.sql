-- ============================================================
-- V17 - USAR IMÁGENES LOCALES GARANTIZADAS SIN ERRORES NETWORK
-- ============================================================

UPDATE tienda.productos
SET imagen_url = '/images/freno-shimano.svg'
WHERE nombre LIKE '%Freno%';

UPDATE tienda.productos
SET imagen_url = '/images/casco-giro.svg'
WHERE nombre LIKE '%Casco%';

UPDATE tienda.productos
SET imagen_url = '/images/cadena-kmc.svg'
WHERE nombre LIKE '%Cadena%';

UPDATE tienda.productos
SET imagen_url = '/images/tija-fox.svg'
WHERE nombre LIKE '%Tija%';

UPDATE tienda.productos
SET imagen_url = '/images/llanta-maxxis.svg'
WHERE nombre LIKE '%Llanta%';

UPDATE tienda.productos
SET imagen_url = '/images/horquilla-rockshox.svg'
WHERE nombre LIKE '%Horquilla%';

UPDATE tienda.productos
SET imagen_url = '/images/pedales-pro.svg'
WHERE nombre LIKE '%Pedales%';

UPDATE tienda.productos
SET imagen_url = '/images/luz-knog.svg'
WHERE nombre LIKE '%Luz%';
