-- ============================================================
-- V19 - TRABAJOS REALIZADOS DEL TALLER PARA LA GALERÍA
-- ============================================================

-- Limpiar trabajos antiguos de prueba
DELETE FROM taller.trabajos_realizados;

INSERT INTO taller.trabajos_realizados (id, titulo, descripcion, imagen_antes_url, imagen_despues_url, fecha, destacado)
VALUES
  (
    'trabajo-001',
    'Purga de Frenos Hidráulicos y Pulido de Disco',
    'Limpieza profunda de disco oxidado, sustitución de líquido mineral Shimano y montaje de cáliper purgado listo para descenso.',
    '/images/trabajo-1-antes.jpg',
    '/images/trabajo-1-despues.jpg',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    true
  ),
  (
    'trabajo-002',
    'Restauración y Repintado Custom Verde Olivo Mate',
    'Despintado total de cuadro corroído, aplicación de base anticorrosiva y pintura personalizada verde oliva mate con acabado cerámico.',
    '/images/trabajo-2-antes.jpg',
    '/images/trabajo-2-despues.svg',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    true
  ),
  (
    'trabajo-003',
    'Calibración y Overhaul de Transmisión 12V',
    'Instalación de cadena KMC X11, desengrase por ultrasonido de cassette y alineación de patilla de cambio trasera.',
    '/images/trabajo-3-antes.svg',
    '/images/trabajo-3-despues.svg',
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    true
  ),
  (
    'trabajo-004',
    'Servicio Técnico a Horquilla RockShox Pike 150mm',
    'Reemplazo de retenes SKF de baja fricción, cambio de aceite hidráulico 5WT y mantenimiento de cartucho de aire.',
    '/images/trabajo-4-antes.svg',
    '/images/trabajo-4-despues.svg',
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    true
  );
