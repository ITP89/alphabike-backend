-- ============================================================
-- V20 - USAR FOTOGRAFÍAS REALES AL 100% PARA LA GALERÍA
-- ============================================================

UPDATE taller.trabajos_realizados
SET
  imagen_antes_url = '/images/trabajo-1-antes.jpg',
  imagen_despues_url = '/images/trabajo-1-despues.jpg'
WHERE id = 'trabajo-001';

UPDATE taller.trabajos_realizados
SET
  imagen_antes_url = '/images/trabajo-2-antes.jpg',
  imagen_despues_url = '/images/tija-fox.jpg'
WHERE id = 'trabajo-002';

UPDATE taller.trabajos_realizados
SET
  imagen_antes_url = '/images/trabajo-1-antes.jpg',
  imagen_despues_url = '/images/cadena-kmc.jpg'
WHERE id = 'trabajo-003';

UPDATE taller.trabajos_realizados
SET
  imagen_antes_url = '/images/trabajo-2-antes.jpg',
  imagen_despues_url = '/images/horquilla-rockshox.jpg'
WHERE id = 'trabajo-004';
