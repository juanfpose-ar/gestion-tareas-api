-- ================================================================
-- GestorTareas — Datos de prueba (PostgreSQL)
-- ================================================================
-- Ejecutado automáticamente por restart-backend.sh / build-deploy.sh
-- después de que la API arrancó y Hibernate completó ddl-auto:update.
--
-- Escenario:
--   Tablero 1: "Desarrollo Web App"     — 4 versiones, 12 tickets
--     · v0.9.0              → CERRADO
--     · v1.0.0              → FINALIZADO
--     · v1.1.0              → EN_CURSO
--     · v2.0.0              → POR_HACER
--   Tablero 2: "Marketing Q3 2026"      — 1 versión,   6 tickets (fondo: angostura)
--     · Campaña Julio 2026  → EN_CURSO
--   Tablero 3: "Diseño UI/UX"           — 2 versiones, 5 tickets  (fondo: bariloche)
--     · Sprint Diseño 1     → EN_CURSO
--     · Sprint Diseño 2     → POR_HACER
--   Tablero 4: "Soporte & QA"           — 2 versiones, 5 tickets  (fondo: purmamarca)
--     · Fix Release 1.0     → FINALIZADO
--     · Hotfix 1.1          → EN_CURSO
--
-- Usuarios (contraseña = "admin" para todos):
--   admin · maria.garcia · lucas.torres · valentina.ruiz
--   sofia.mendez · diego.ferrari · carlos.vega
-- ================================================================

BEGIN;

-- ── 1. Limpieza ───────────────────────────────────────────────
TRUNCATE TABLE
  gestortareas.notas_tarea,
  gestortareas.checklist_items,
  gestortareas.ticket_etiquetas,
  gestortareas.ticket_asignados,
  gestortareas.ticket_informados,
  gestortareas.ticket_vinculos,
  gestortareas.adjuntos,
  gestortareas.recordatorios,
  gestortareas.reunion_tickets,
  gestortareas.reuniones,
  gestortareas.tickets,
  gestortareas.versiones,
  gestortareas.etiquetas,
  gestortareas.estados_tablero,
  gestortareas.usuario_tableros,
  gestortareas.tableros,
  gestortareas.usuario_conversacion,
  gestortareas.mensajes,
  gestortareas.conversaciones,
  gestortareas.usuarios
RESTART IDENTITY CASCADE;

-- ── 2. Usuarios ───────────────────────────────────────────────
-- Hash BCrypt de la contraseña "admin". Todos usan la misma.
INSERT INTO gestortareas.usuarios
  (id, username, password, nombre_completo, rol, activo, email, color_avatar)
VALUES
  (1, 'admin',           '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'Admin',            'ADMIN', true, 'admin@empresa.com',       '#6366f1'),
  (2, 'maria.garcia',    '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'María García',     'USER',  true, 'maria@empresa.com',       '#ef4444'),
  (3, 'lucas.torres',    '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'Lucas Torres',     'USER',  true, 'lucas@empresa.com',       '#10b981'),
  (4, 'valentina.ruiz',  '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'Valentina Ruiz',  'USER',  true, 'valentina@empresa.com',   '#f59e0b'),
  (5, 'sofia.mendez',    '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'Sofía Méndez',    'USER',  true, 'sofia@empresa.com',       '#ec4899'),
  (6, 'diego.ferrari',   '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'Diego Ferrari',   'USER',  true, 'diego@empresa.com',       '#f97316'),
  (7, 'carlos.vega',     '$2a$10$v70czOi1Q7DIVRLK/NessuBZvnuBmbLvhWlD17w54kPxbBkspsl.a', 'Carlos Vega',     'USER',  true, 'carlos@empresa.com',      '#06b6d4');

-- ── 3. Tableros ──────────────────────────────────────────────
INSERT INTO gestortareas.tableros
  (id, titulo, descripcion, imagen_fondo_url, fecha_creacion, archivado)
VALUES
  (1, 'Desarrollo Web App',
      'Backend y frontend del sistema de gestión de tareas',
      NULL,
      NOW() - INTERVAL '70 DAY', false),
  (2, 'Marketing Q3 2026',
      'Planificación y ejecución de campañas para el tercer trimestre',
      '/backgrounds/angostura.jpg',
      NOW() - INTERVAL '20 DAY', false),
  (3, 'Diseño UI/UX',
      'Sistema de diseño, componentes y experiencia de usuario',
      '/backgrounds/bariloche.jpg',
      NOW() - INTERVAL '15 DAY', false),
  (4, 'Soporte & QA',
      'Seguimiento de bugs, incidencias y calidad del producto',
      '/backgrounds/purmamarca.jpg',
      NOW() - INTERVAL '30 DAY', false);

-- ── 4. Usuarios → Tableros ────────────────────────────────────
INSERT INTO gestortareas.usuario_tableros (usuario_id, tablero_id) VALUES
  (1, 1), (1, 2), (1, 3), (1, 4),  -- admin  → todos
  (2, 1), (2, 4),                   -- María  → Dev + Soporte
  (3, 1), (3, 3),                   -- Lucas  → Dev + Diseño
  (4, 1), (4, 2),                   -- Valentina → Dev + Marketing
  (5, 1), (5, 3),                   -- Sofía  → Dev + Diseño
  (6, 1), (6, 3),                   -- Diego  → Dev + Diseño
  (7, 1), (7, 4);                   -- Carlos → Dev + Soporte

-- ── 5. Estados ───────────────────────────────────────────────
INSERT INTO gestortareas.estados_tablero (id, nombre, orden, color_hex, tablero_id) VALUES
  -- Tablero 1: Desarrollo Web App
  (1,  'Backlog',        1, '#64748b', 1),
  (2,  'En Progreso',    2, '#3b82f6', 1),
  (3,  'En Revisión',    3, '#f59e0b', 1),
  (4,  'Hecho',          4, '#10b981', 1),
  -- Tablero 2: Marketing
  (5,  'Ideas',          1, '#8b5cf6', 2),
  (6,  'En Ejecución',   2, '#3b82f6', 2),
  (7,  'Revisión',       3, '#f59e0b', 2),
  (8,  'Publicado',      4, '#10b981', 2),
  -- Tablero 3: Diseño UI/UX
  (9,  'Backlog',        1, '#64748b', 3),
  (10, 'Diseñando',      2, '#8b5cf6', 3),
  (11, 'En revisión',    3, '#f59e0b', 3),
  (12, 'Aprobado',       4, '#10b981', 3),
  -- Tablero 4: Soporte & QA
  (13, 'Reportado',      1, '#ef4444', 4),
  (14, 'En análisis',    2, '#f97316', 4),
  (15, 'En resolución',  3, '#3b82f6', 4),
  (16, 'Resuelto',       4, '#10b981', 4);

-- ── 6. Etiquetas ─────────────────────────────────────────────
INSERT INTO gestortareas.etiquetas (id, nombre, color, color_texto, tablero_id) VALUES
  -- Tablero 1
  (1,  'Bug',            '#fca5a5', '#7f1d1d', 1),
  (2,  'Feature',        '#bfdbfe', '#1e3a5f', 1),
  (3,  'Urgente',        '#fdba74', '#7c2d12', 1),
  (4,  'Refactor',       '#e9d5ff', '#4c1d95', 1),
  (5,  'Documentación',  '#e2e8f0', '#334155', 1),
  -- Tablero 2
  (6,  'Redes Sociales', '#a5f3fc', '#164e63', 2),
  (7,  'Email',          '#86efac', '#14532d', 2),
  (8,  'Campaña',        '#fef08a', '#713f12', 2),
  -- Tablero 3
  (9,  'Prototipo',      '#fce7f3', '#831843', 3),
  (10, 'Wireframe',      '#ede9fe', '#4c1d95', 3),
  (11, 'Animación',      '#dbeafe', '#1e40af', 3),
  (12, 'Accesibilidad',  '#d1fae5', '#065f46', 3),
  -- Tablero 4
  (13, 'Crítico',        '#fecaca', '#7f1d1d', 4),
  (14, 'Regresión',      '#fed7aa', '#7c2d12', 4),
  (15, 'UX',             '#e9d5ff', '#4c1d95', 4),
  (16, 'Performance',    '#bfdbfe', '#1e3a5f', 4);

-- ── 7. Versiones ─────────────────────────────────────────────
INSERT INTO gestortareas.versiones (id, titulo, fecha_vencimiento, tablero_id, estado) VALUES
  -- Tablero 1
  (1, 'v0.9.0 — MVP',        '2026-05-31 23:59:59', 1, 'CERRADO'),
  (2, 'v1.0.0',              '2026-06-30 23:59:59', 1, 'FINALIZADO'),
  (3, 'v1.1.0',              '2026-07-31 23:59:59', 1, 'EN_CURSO'),
  (4, 'v2.0.0',              '2026-09-30 23:59:59', 1, 'POR_HACER'),
  -- Tablero 2
  (5, 'Campaña Julio 2026',  '2026-07-31 23:59:59', 2, 'EN_CURSO'),
  -- Tablero 3
  (6, 'Sprint Diseño 1',     '2026-07-31 23:59:59', 3, 'EN_CURSO'),
  (7, 'Sprint Diseño 2',     '2026-09-15 23:59:59', 3, 'POR_HACER'),
  -- Tablero 4
  (8, 'Fix Release 1.0',     '2026-06-30 23:59:59', 4, 'FINALIZADO'),
  (9, 'Hotfix 1.1',          '2026-07-31 23:59:59', 4, 'EN_CURSO');

-- ── 8. Tickets ───────────────────────────────────────────────
-- Tablero 1: Desarrollo Web App
INSERT INTO gestortareas.tickets
  (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, orden,
   fecha_creacion, fecha_modificacion, fecha_vencimiento, version_id, creador_id, completado)
VALUES
  -- v0.9.0 CERRADO
  (1,  'Configurar proyecto con Docker Compose',
       'Crear docker-compose.yml con servicios de backend, base de datos y frontend listos para desarrollo local.',
       'MEDIA',  1, 4, false, 1, NOW() - INTERVAL '60 DAY', NOW() - INTERVAL '32 DAY', '2026-05-10 23:59:59', 1, 1, true),
  (2,  'Diseñar esquema de base de datos inicial',
       'Modelar las entidades principales del MVP: usuarios, tableros, estados y tickets.',
       'ALTA',   1, 4, false, 2, NOW() - INTERVAL '58 DAY', NOW() - INTERVAL '30 DAY', '2026-05-15 23:59:59', 1, 1, true),

  -- v1.0.0 FINALIZADO
  (3,  'Implementar autenticación JWT',
       'Configurar Spring Security con JWT. Incluye filtro de autenticación, generación y validación de tokens.',
       'ALTA',   1, 4, false, 1, NOW() - INTERVAL '40 DAY', NOW() - INTERVAL '2 DAY',  '2026-06-10 23:59:59', 2, 1, true),
  (4,  'Crear módulo de gestión de usuarios',
       'CRUD de usuarios con roles (ADMIN/USER), activación/desactivación de cuentas y cambio de contraseña.',
       'MEDIA',  1, 4, false, 2, NOW() - INTERVAL '38 DAY', NOW() - INTERVAL '2 DAY',  '2026-06-15 23:59:59', 2, 2, true),

  -- v1.1.0 EN_CURSO
  (5,  'Desarrollar API REST de tickets',
       'Endpoints para crear, editar, archivar y reordenar tickets. Filtros por estado, etiqueta y versión.',
       'ALTA',   1, 2, false, 1, NOW() - INTERVAL '10 DAY', NOW() - INTERVAL '1 DAY',  '2026-07-10 23:59:59', 3, 2, false),
  (6,  'Integrar notificaciones por email',
       'Usar Spring Mail + Thymeleaf para enviar recordatorios de vencimiento y menciones en comentarios.',
       'MEDIA',  1, 2, false, 2, NOW() - INTERVAL '8 DAY',  NOW() - INTERVAL '1 DAY',  '2026-07-20 23:59:59', 3, 1, false),
  (7,  'Optimizar consultas con índices',
       'Revisar planes de ejecución de las queries más lentas y agregar índices compuestos donde corresponda.',
       'BAJA',   1, 1, false, 3, NOW() - INTERVAL '5 DAY',  NOW(),                     '2026-07-25 23:59:59', 3, 3, false),
  (8,  'Corregir bug en validación de formularios',
       'Al ingresar un email con punto al final (ej: user@dominio.com.) el formulario no rechaza el valor.',
       'MEDIA',  1, 1, false, 4, NOW() - INTERVAL '2 DAY',  NOW(),                     '2026-07-05 23:59:59', 3, 3, false),

  -- v2.0.0 POR_HACER
  (9,  'Agregar paginación en listados',
       'Implementar paginación con Pageable de Spring en endpoints de tableros y tickets.',
       'MEDIA',  1, 1, false, 1, NOW() - INTERVAL '3 DAY',  NOW(),                     '2026-08-15 23:59:59', 4, 1, false),
  (10, 'Diseñar arquitectura de microservicios',
       'Evaluar la migración desde monolito: identificar bounded contexts y definir contratos de API entre servicios.',
       'ALTA',   1, 1, false, 2, NOW() - INTERVAL '3 DAY',  NOW(),                     '2026-09-01 23:59:59', 4, 1, false),
  (11, 'Implementar caché con Redis',
       'Cachear respuestas de tableros y tickets activos para reducir carga en base de datos en horarios pico.',
       'MEDIA',  1, 1, false, 3, NOW() - INTERVAL '2 DAY',  NOW(),                     '2026-08-30 23:59:59', 4, 2, false),

  -- Sin versión
  (12, 'Agregar modo oscuro al frontend',
       'Implementar dark mode con variables CSS y prefers-color-scheme. Respetar preferencia guardada en localStorage.',
       'BAJA',   1, 1, false, 5, NOW() - INTERVAL '1 DAY',  NOW(),                     NULL, NULL, 3, false);

-- Tablero 2: Marketing Q3 2026
INSERT INTO gestortareas.tickets
  (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, orden,
   fecha_creacion, fecha_modificacion, fecha_vencimiento, version_id, creador_id, completado)
VALUES
  (13, 'Rediseñar landing page',
       'Actualizar el diseño de la home: nuevas secciones de testimonios, casos de éxito y CTA principal renovado.',
       'ALTA',  2, 6, false, 1, NOW() - INTERVAL '7 DAY',  NOW() - INTERVAL '1 DAY',  '2026-07-15 23:59:59', 5, 1, false),
  (14, 'Crear newsletter de julio',
       'Newsletter mensual con novedades del producto, artículos más leídos del blog y próximos webinars.',
       'MEDIA', 2, 5, false, 1, NOW() - INTERVAL '6 DAY',  NOW() - INTERVAL '2 DAY',  '2026-07-01 23:59:59', 5, 4, false),
  (15, 'Campaña de Instagram Stories',
       'Diseñar 5 stories para la semana del 14 al 18 de julio. CTA con swipe-up y link a landing de descarga.',
       'MEDIA', 2, 6, false, 2, NOW() - INTERVAL '5 DAY',  NOW() - INTERVAL '1 DAY',  '2026-07-14 23:59:59', 5, 4, false),
  (16, 'Análisis de competencia Q3',
       'Relevar estrategias de comunicación y pricing de los 3 principales competidores durante Q2 2026.',
       'BAJA',  2, 5, false, 2, NOW() - INTERVAL '4 DAY',  NOW(),                     '2026-07-20 23:59:59', NULL, 1, false),
  (17, 'Publicar blog: 5 tips de productividad',
       'Artículo de 900 palabras orientado a usuarios de herramientas de gestión. Incluir infografía y CTA al producto.',
       'BAJA',  2, 8, false, 1, NOW() - INTERVAL '10 DAY', NOW() - INTERVAL '3 DAY',  '2026-06-30 23:59:59', NULL, 1, false),
  (18, 'A/B test en emails de bienvenida',
       'Probar dos versiones del email de onboarding (formal vs. informal) para mejorar tasa de apertura y clics.',
       'MEDIA', 2, 7, false, 1, NOW() - INTERVAL '4 DAY',  NOW() - INTERVAL '1 DAY',  '2026-07-28 23:59:59', 5, 1, false);

-- Tablero 3: Diseño UI/UX
INSERT INTO gestortareas.tickets
  (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, orden,
   fecha_creacion, fecha_modificacion, fecha_vencimiento, version_id, creador_id, completado)
VALUES
  -- Sprint Diseño 1 EN_CURSO
  (19, 'Rediseño del dashboard principal',
       'Nueva distribución de widgets, métricas clave en la parte superior y accesos rápidos laterales. Figma entregable.',
       'ALTA',  3, 10, false, 1, NOW() - INTERVAL '12 DAY', NOW() - INTERVAL '1 DAY', '2026-07-20 23:59:59', 6, 5, false),
  (20, 'Flujo de onboarding de nuevos usuarios',
       'Diseñar el wizard de bienvenida: 4 pasos con tooltip de configuración inicial y saludo personalizado.',
       'MEDIA', 3, 9,  false, 2, NOW() - INTERVAL '10 DAY', NOW() - INTERVAL '2 DAY', '2026-07-25 23:59:59', 6, 5, false),
  (21, 'Documentar design tokens del sistema',
       'Crear librería de tokens en Figma: colores, tipografía, espaciado y sombras. Exportar como JSON para el frontend.',
       'BAJA',  3, 11, false, 3, NOW() - INTERVAL '8 DAY',  NOW() - INTERVAL '1 DAY', '2026-07-28 23:59:59', 6, 6, false),

  -- Sprint Diseño 2 POR_HACER
  (22, 'Sistema de animaciones de transición',
       'Definir micro-interacciones para modales, toasts y navegación entre vistas. Usar Framer Motion en el frontend.',
       'MEDIA', 3, 9,  false, 1, NOW() - INTERVAL '5 DAY',  NOW(),                    '2026-08-30 23:59:59', 7, 5, false),
  (23, 'Auditoría de accesibilidad WCAG 2.1',
       'Revisar contraste de colores, navegación por teclado y etiquetas ARIA en todos los componentes principales.',
       'ALTA',  3, 9,  false, 2, NOW() - INTERVAL '3 DAY',  NOW(),                    '2026-09-10 23:59:59', 7, 5, false);

-- Tablero 4: Soporte & QA
INSERT INTO gestortareas.tickets
  (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, orden,
   fecha_creacion, fecha_modificacion, fecha_vencimiento, version_id, creador_id, completado)
VALUES
  -- Fix Release 1.0 FINALIZADO — todos completados
  (24, 'Error en carga de adjuntos mayores a 10 MB',
       'Al subir un archivo de más de 10 MB el endpoint devuelve 413 sin mensaje claro. Aumentar límite y mejorar error.',
       'ALTA',  4, 16, false, 1, NOW() - INTERVAL '25 DAY', NOW() - INTERVAL '5 DAY', '2026-06-20 23:59:59', 8, 7, true),
  (25, 'Timeout al listar tableros con más de 50 tickets',
       'La query de tableros con join a tickets supera los 30 segundos cuando hay más de 50 tickets activos.',
       'MEDIA', 4, 16, false, 2, NOW() - INTERVAL '22 DAY', NOW() - INTERVAL '5 DAY', '2026-06-25 23:59:59', 8, 2, true),

  -- Hotfix 1.1 EN_CURSO
  (26, 'Bug en paginación en dispositivos móviles',
       'En pantallas menores a 375px el botón "siguiente página" queda fuera del viewport y no es accesible.',
       'ALTA',  4, 15, false, 1, NOW() - INTERVAL '8 DAY',  NOW() - INTERVAL '1 DAY', '2026-07-15 23:59:59', 9, 7, false),
  (27, 'Rendimiento bajo en tabla de tickets masiva',
       'Con más de 200 tickets en un tablero la vista tabla tarda más de 4 segundos en renderizar. Virtualizar filas.',
       'MEDIA', 4, 14, false, 2, NOW() - INTERVAL '6 DAY',  NOW(),                    '2026-07-25 23:59:59', 9, 2, false),

  -- Sin versión
  (28, 'Analizar logs de errores del servidor',
       'Revisar los logs de producción de los últimos 7 días e identificar errores 500 recurrentes para priorizar.',
       'BAJA',  4, 13, false, 1, NOW() - INTERVAL '2 DAY',  NOW(),                    '2026-07-31 23:59:59', NULL, 7, false);

-- ── 9. Ticket → Etiquetas ────────────────────────────────────
INSERT INTO gestortareas.ticket_etiquetas (ticket_id, etiqueta_id) VALUES
  -- Tablero 1
  (1,  2),           -- Docker:      Feature
  (2,  5),           -- Esquema DB:  Documentación
  (3,  2), (3,  3),  -- JWT:         Feature + Urgente
  (5,  2),           -- API Tickets: Feature
  (7,  4),           -- Optimizar:   Refactor
  (8,  1), (8,  3),  -- Bug email:   Bug + Urgente
  (9,  2),           -- Paginación:  Feature
  (10, 4), (10, 5),  -- Microserv:   Refactor + Documentación
  (11, 4),           -- Redis:       Refactor
  -- Tablero 2
  (13, 6),           -- Landing:     Redes Sociales
  (14, 7),           -- Newsletter:  Email
  (15, 6), (15, 8),  -- Instagram:   Redes Sociales + Campaña
  (18, 7), (18, 8),  -- A/B test:    Email + Campaña
  -- Tablero 3
  (19, 9),           -- Dashboard:   Prototipo
  (20, 10),          -- Onboarding:  Wireframe
  (21, 12),          -- Tokens:      Accesibilidad
  (22, 11),          -- Animaciones: Animación
  (23, 12),          -- WCAG:        Accesibilidad
  -- Tablero 4
  (24, 13),          -- Adjuntos:    Crítico
  (25, 16),          -- Timeout:     Performance
  (26, 14), (26, 15),-- Mobile bug:  Regresión + UX
  (27, 16);          -- Tabla perf:  Performance

-- ── 10. Ticket → Asignados ───────────────────────────────────
INSERT INTO gestortareas.ticket_asignados (ticket_id, usuario_id) VALUES
  -- Tablero 1
  (1,  3),           -- Docker:     Lucas
  (2,  1),           -- Esquema:    Admin
  (3,  2),           -- JWT:        María
  (4,  3),           -- Usuarios:   Lucas
  (5,  2), (5, 3),   -- API:        María + Lucas
  (6,  2),           -- Emails:     María
  (8,  1),           -- Bug:        Admin
  (10, 1),           -- Microserv:  Admin
  -- Tablero 2
  (13, 4),           -- Landing:    Valentina
  (14, 4),           -- Newsletter: Valentina
  (15, 4),           -- Instagram:  Valentina
  (18, 4),           -- A/B test:   Valentina
  -- Tablero 3
  (19, 5),           -- Dashboard:  Sofía
  (20, 5), (20, 6),  -- Onboarding: Sofía + Diego
  (21, 6),           -- Tokens:     Diego
  (22, 5),           -- Animaciones:Sofía
  (23, 5), (23, 6),  -- WCAG:       Sofía + Diego
  -- Tablero 4
  (24, 7),           -- Adjuntos:   Carlos
  (25, 7), (25, 2),  -- Timeout:    Carlos + María
  (26, 7),           -- Mobile bug: Carlos
  (27, 2);           -- Tabla perf: María

-- ── 11. Ticket → Informados ──────────────────────────────────
INSERT INTO gestortareas.ticket_informados (ticket_id, usuario_id) VALUES
  (5,  1),           -- API REST:    admin informado (asignados: maría, lucas)
  (8,  2), (8, 3),   -- Bug form:    María y Lucas informados (asignado: admin)
  (13, 2),           -- Landing:     María informada (asignado: valentina)
  (16, 1), (16, 3),  -- Competencia: admin y Lucas informados
  (19, 6),           -- Dashboard:   Diego informado (asignado: sofía)
  (26, 1),           -- Mobile bug:  admin informado (asignado: carlos)
  (28, 1), (28, 2);  -- Logs:        admin y María informados

-- ── 12. Checklist items ──────────────────────────────────────
-- Ticket 1: Docker Compose — v0.9.0 CERRADO (4/4)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (1,  1,  'Crear docker-compose.yml base',             true,  1),
  (2,  1,  'Configurar servicio PostgreSQL',            true,  2),
  (3,  1,  'Configurar servicio backend Spring Boot',  true,  3),
  (4,  1,  'Configurar servicio frontend Vite',        true,  4);

-- Ticket 3: JWT — v1.0.0 FINALIZADO (5/5)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (5,  3,  'Crear JwtTokenProvider con firma HS256',   true,  1),
  (6,  3,  'Implementar JwtAuthenticationFilter',      true,  2),
  (7,  3,  'Agregar endpoint POST /auth/login',        true,  3),
  (8,  3,  'Proteger rutas con @PreAuthorize',         true,  4),
  (9,  3,  'Test de integración para /auth/login',     true,  5);

-- Ticket 5: API REST — v1.1.0 EN_CURSO (2/5)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (10, 5,  'Definir TicketRequest y TicketCardDTO',    true,  1),
  (11, 5,  'GET /tickets/tablero/{id}',                true,  2),
  (12, 5,  'POST /tickets y PUT /tickets/{id}',        false, 3),
  (13, 5,  'Endpoint de reordenamiento drag & drop',   false, 4),
  (14, 5,  'Tests de integración del controlador',     false, 5);

-- Ticket 13: Landing page — Marketing EN_CURSO (2/4)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (15, 13, 'Relevamiento de referencias y competencia',true,  1),
  (16, 13, 'Wireframe aprobado en Figma',              true,  2),
  (17, 13, 'Diseño final aprobado por el cliente',     false, 3),
  (18, 13, 'Implementación HTML/CSS responsive',       false, 4);

-- Ticket 19: Dashboard rediseño — Diseño EN_CURSO (2/4)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (19, 19, 'Relevamiento de la UI actual',             true,  1),
  (20, 19, 'Wireframe de nueva distribución aprobado', true,  2),
  (21, 19, 'Prototipo en alta fidelidad en Figma',     false, 3),
  (22, 19, 'Pruebas de usabilidad con 3 usuarios',     false, 4);

-- Ticket 24: Bug adjuntos — Fix Release FINALIZADO (4/4)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (23, 24, 'Reproducir el bug con archivo de 15 MB',  true,  1),
  (24, 24, 'Identificar causa raíz en el controller', true,  2),
  (25, 24, 'Aumentar límite a 50 MB en config',       true,  3),
  (26, 24, 'Test de regresión con 5 tipos de archivo',true,  4);

-- Ticket 26: Bug móvil — Hotfix EN_CURSO (1/3)
INSERT INTO gestortareas.checklist_items (id, ticket_id, texto, completado, orden) VALUES
  (27, 26, 'Reproducir en iPhone SE y Galaxy A32',    true,  1),
  (28, 26, 'Ajustar media query para <375px',         false, 2),
  (29, 26, 'Verificar en Chrome DevTools emulation',  false, 3);

-- ── 13. Notas ────────────────────────────────────────────────
INSERT INTO gestortareas.notas_tarea (id, ticket_id, texto, fecha_hora) VALUES
  (1, 1,
   'v0.9.0 entregada el 31/05. El compose funciona correctamente en todos los entornos. Se cierra la versión.',
   NOW() - INTERVAL '32 DAY'),

  (2, 3,
   'v1.0.0 cerrada el 30/06. Decisión técnica: HS256 con clave de 256 bits en APP_JWT_SECRET. Token expira en 24hs.',
   NOW() - INTERVAL '2 DAY'),

  (3, 8,
   'Bug reproducido en Chrome 125 y Firefox 127. Ocurre cuando el email termina con punto (user@empresa.com.). La validación frontend lo deja pasar.',
   NOW()),

  (4, 10,
   'v2.0.0: Ver propuesta en Confluence. Prioridad: separar servicio de notificaciones por ser el más desacoplado.',
   NOW()),

  (5, 13,
   'El cliente aprobó la paleta nueva. Fuente: Inter (reemplaza Roboto). Confirmar con marketing antes del diseño final.',
   NOW()),

  (6, 19,
   'Sofía: el nuevo dashboard reduce los clicks promedio de 4 a 2 para llegar a las métricas principales. Aprobado concepto.',
   NOW() - INTERVAL '1 DAY'),

  (7, 24,
   'Bug cerrado: el límite de upload estaba seteado en 1 MB en application.properties. Aumentado a 50 MB y desplegado.',
   NOW() - INTERVAL '5 DAY'),

  (8, 26,
   'Afecta iPhone SE (375px) y Galaxy A32 (360px). El botón de paginación tiene margin-right: 16px que lo empuja fuera del viewport.',
   NOW() - INTERVAL '1 DAY'),

  (9, 27,
   'Profiling con React DevTools: el problema está en el re-render de todas las filas al hacer hover. Implementar React.memo en TableRow.',
   NOW());

-- ── 14. Mensajería ────────────────────────────────────────────
INSERT INTO gestortareas.conversaciones (id, asunto, fecha_ultima_actividad) VALUES
  (1, 'Revisión de requerimientos JWT',              NOW() - INTERVAL '10 MINUTE'),
  (2, 'Docker Compose no levanta base de datos',     NOW() - INTERVAL '1 DAY'),
  (3, 'Dudas sobre campaña de Marketing',            NOW() - INTERVAL '3 DAY'),
  (4, 'Idea para arquitectura de microservicios',    NOW() - INTERVAL '2 HOUR'),
  (5, 'Kick-off Sprint Diseño 1',                    NOW() - INTERVAL '30 MINUTE'),
  (6, 'Bug crítico en carga de adjuntos',            NOW() - INTERVAL '45 MINUTE'),
  (7, 'Revisión de prototipo del dashboard',         NOW() - INTERVAL '3 HOUR'),
  (8, 'Performance en tablero de tickets masivo',    NOW() - INTERVAL '1 HOUR');

INSERT INTO gestortareas.mensajes (id, conversacion_id, emisor_id, contenido, fecha_envio) VALUES
  -- Conv 1: JWT (admin ↔ maría)
  (1,  1, 2, 'Hola, ¿pudiste revisar el archivo JwtAuthenticationFilter?',                                              NOW() - INTERVAL '2 HOUR'),
  (2,  1, 1, 'Sí María, lo vi. Deberíamos aumentar la expiración del token a 24hs para mayor comodidad en desarrollo.', NOW() - INTERVAL '1 HOUR'),
  (3,  1, 2, 'Perfecto, ya lo modifiqué y subí al repo. Avísame si ves algo más.',                                      NOW() - INTERVAL '10 MINUTE'),

  -- Conv 2: Docker (admin ↔ lucas)
  (4,  2, 3, 'Hola admin, estoy teniendo un error al levantar el compose: port 5432 already in use.',                  NOW() - INTERVAL '1 DAY' - INTERVAL '2 HOUR'),
  (5,  2, 1, 'Lucas, revisá si tenés una instancia de Postgres local corriendo fuera de Docker. Apagá el servicio local y reintentá.', NOW() - INTERVAL '1 DAY'),

  -- Conv 3: Marketing (admin ↔ valentina — archivada para admin)
  (6,  3, 4, 'Hola admin, ¿ya publicamos el artículo de los 5 tips en el blog?',                                        NOW() - INTERVAL '3 DAY' - INTERVAL '5 HOUR'),
  (7,  3, 1, 'Sí Valentina, quedó programado y se publicó el martes pasado.',                                           NOW() - INTERVAL '3 DAY'),

  -- Conv 4: Microservicios (admin + lucas + maría)
  (8,  4, 1, 'Hola equipo, estuve pensando en que podríamos desacoplar el servicio de notificaciones primero.',         NOW() - INTERVAL '3 HOUR'),
  (9,  4, 3, 'Excelente idea, yo puedo encargarme de diseñar el contrato de la API entre servicios.',                   NOW() - INTERVAL '2 HOUR' - INTERVAL '30 MINUTE'),
  (10, 4, 2, 'Me sumo. Puedo armar la configuración inicial de Spring Cloud y los clients Feign.',                       NOW() - INTERVAL '2 HOUR'),

  -- Conv 5: Kick-off Diseño 1 (admin + sofía + diego)
  (11, 5, 1, 'Bienvenidos al sprint de diseño. Sofía lidera, Diego te da soporte en la parte técnica del design system.', NOW() - INTERVAL '2 HOUR'),
  (12, 5, 5, 'Perfecto! Ya cargué en Figma las referencias. Esta semana arranco con el wireframe del dashboard.',         NOW() - INTERVAL '1 HOUR' - INTERVAL '30 MINUTE'),
  (13, 5, 6, 'Genial Sofía, cuando tengas el prototipo me avisás y lo reviso con el frontend en mente.',                  NOW() - INTERVAL '1 HOUR'),
  (14, 5, 5, 'Dale Diego. También necesito que exportes los design tokens actuales del repo para alinearlos.',             NOW() - INTERVAL '30 MINUTE'),

  -- Conv 6: Bug adjuntos (carlos + admin + maría)
  (15, 6, 7, 'Reportando bug crítico: los adjuntos mayores a 10 MB fallan con 413 sin mensaje de error al usuario.',    NOW() - INTERVAL '3 HOUR'),
  (16, 6, 1, 'Carlos, revisá el parámetro spring.servlet.multipart.max-file-size en application.properties.',            NOW() - INTERVAL '2 HOUR' - INTERVAL '30 MINUTE'),
  (17, 6, 7, 'Efectivamente estaba en 1MB. Lo subí a 50MB y agregué una validación con mensaje claro al frontend.',       NOW() - INTERVAL '2 HOUR'),
  (18, 6, 2, 'Bien! Hice una prueba con un PDF de 25 MB y funciona. Cerramos el ticket de la Fix Release.',              NOW() - INTERVAL '45 MINUTE'),

  -- Conv 7: Prototipo dashboard (sofía ↔ lucas)
  (19, 7, 5, 'Lucas, te comparto el prototipo del nuevo dashboard: [Figma link]. ¿Podés revisar si la distribución es viable?', NOW() - INTERVAL '4 HOUR'),
  (20, 7, 3, 'Lo vi Sofía, está muy bueno. El widget de métricas va a quedar perfecto con el grid de Bootstrap 5.',           NOW() - INTERVAL '3 HOUR' - INTERVAL '30 MINUTE'),
  (21, 7, 5, 'Gracias! Voy a afinar los espaciados y lo mando a revisión con admin.',                                         NOW() - INTERVAL '3 HOUR'),

  -- Conv 8: Performance tabla (maría ↔ carlos)
  (22, 8, 2, 'Carlos, estoy viendo que con 200+ tickets el re-render es muy lento. ¿Revisaste los logs de performance?', NOW() - INTERVAL '2 HOUR'),
  (23, 8, 7, 'Sí, el problema está en el componente TableRow. Cada hover triggerea un render de toda la tabla.',          NOW() - INTERVAL '1 HOUR' - INTERVAL '30 MINUTE'),
  (24, 8, 2, 'Exacto. Voy a implementar React.memo en TableRow y useCallback en los handlers. Lo pruebo y te aviso.',     NOW() - INTERVAL '1 HOUR');

INSERT INTO gestortareas.usuario_conversacion (usuario_id, conversacion_id, leida, archivada, eliminada, destacada) VALUES
  -- Conv 1: admin ↔ maría  (admin no leyó, admin la destacó)
  (1, 1, false, false, false, true),
  (2, 1, true,  false, false, false),

  -- Conv 2: admin ↔ lucas
  (1, 2, true,  false, false, false),
  (3, 2, true,  false, false, false),

  -- Conv 3: admin ↔ valentina  (admin archivó)
  (1, 3, true,  true,  false, false),
  (4, 3, true,  false, false, false),

  -- Conv 4: admin + lucas + maría  (admin la destacó)
  (1, 4, true,  false, false, true),
  (2, 4, true,  false, false, false),
  (3, 4, true,  false, false, false),

  -- Conv 5: admin + sofía + diego  (sofía no leyó el último)
  (1, 5, true,  false, false, false),
  (5, 5, false, false, false, false),
  (6, 5, true,  false, false, false),

  -- Conv 6: carlos + admin + maría
  (7, 6, true,  false, false, false),
  (1, 6, true,  false, false, false),
  (2, 6, true,  false, false, false),

  -- Conv 7: sofía ↔ lucas
  (5, 7, true,  false, false, false),
  (3, 7, true,  false, false, false),

  -- Conv 8: maría ↔ carlos  (carlos no leyó)
  (2, 8, true,  false, false, false),
  (7, 8, false, false, false, false);

-- ── 15. Actualizar secuencias ─────────────────────────────────
SELECT setval('gestortareas.usuarios_id_seq',            (SELECT MAX(id) FROM gestortareas.usuarios));
SELECT setval('gestortareas.tableros_id_seq',            (SELECT MAX(id) FROM gestortareas.tableros));
SELECT setval('gestortareas.estados_tablero_id_seq',     (SELECT MAX(id) FROM gestortareas.estados_tablero));
SELECT setval('gestortareas.etiquetas_id_seq',           (SELECT MAX(id) FROM gestortareas.etiquetas));
SELECT setval('gestortareas.versiones_id_seq',           (SELECT MAX(id) FROM gestortareas.versiones));
SELECT setval('gestortareas.tickets_id_seq',             (SELECT MAX(id) FROM gestortareas.tickets));
SELECT setval('gestortareas.checklist_items_id_seq',     (SELECT MAX(id) FROM gestortareas.checklist_items));
SELECT setval('gestortareas.notas_tarea_id_seq',         (SELECT MAX(id) FROM gestortareas.notas_tarea));
SELECT setval('gestortareas.reuniones_id_seq',           (SELECT COALESCE(MAX(id), 1) FROM gestortareas.reuniones));
SELECT setval('gestortareas.conversaciones_id_seq',      (SELECT MAX(id) FROM gestortareas.conversaciones));
SELECT setval('gestortareas.mensajes_id_seq',            (SELECT MAX(id) FROM gestortareas.mensajes));

COMMIT;

-- ================================================================
-- Resultado:
--   4 tableros · 16 estados · 16 etiquetas · 9 versiones
--   28 tickets · 7 usuarios · 29 checklist items · 9 notas
--   8 conversaciones · 24 mensajes
--
-- Tableros:
--   1 · Desarrollo Web App   — sin fondo
--   2 · Marketing Q3 2026    — fondo: angostura
--   3 · Diseño UI/UX         — fondo: bariloche
--   4 · Soporte & QA         — fondo: purmamarca
--
-- Versiones por tablero:
--   T1: v0.9.0(CERRADO) · v1.0.0(FINALIZADO) · v1.1.0(EN_CURSO) · v2.0.0(POR_HACER)
--   T2: Campaña Julio 2026 (EN_CURSO)
--   T3: Sprint Diseño 1 (EN_CURSO) · Sprint Diseño 2 (POR_HACER)
--   T4: Fix Release 1.0 (FINALIZADO) · Hotfix 1.1 (EN_CURSO)
--
-- Usuarios nuevos:
--   sofia.mendez (#ec4899) — Diseño UI/UX + Dev
--   diego.ferrari (#f97316) — Diseño UI/UX + Dev
--   carlos.vega (#06b6d4)  — Soporte & QA + Dev
-- ================================================================
