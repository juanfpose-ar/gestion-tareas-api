-- El usuario "admin" (id=1) lo crea DataSeeder vía JPA al arrancar el contexto, no acá
-- (ver nota en schema.sql). creador_id de abajo asume ese id=1.

MERGE INTO tableros (id, titulo, descripcion, creador_id, archivado, fecha_creacion) KEY (id) VALUES (1, 'Tablero Test', 'Descripción de prueba', 1, false, CURRENT_TIMESTAMP);

MERGE INTO estados_tablero (id, nombre, orden, color_hex, tablero_id) KEY (id) VALUES (1, 'Por Hacer', 1, '#0ea5e9', 1);
MERGE INTO estados_tablero (id, nombre, orden, color_hex, tablero_id) KEY (id) VALUES (2, 'En Curso', 2, '#f59e0b', 1);

MERGE INTO etiquetas (id, nombre, color_hex, tablero_id) KEY (id) VALUES (1, 'TestTag', '#3b82f6', 1);

MERGE INTO tickets (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, completado, fecha_creacion) KEY (id) VALUES (1, 'Ticket Test 1', 'Descripcion 1', 'MEDIA', 1, 1, false, false, CURRENT_TIMESTAMP);
MERGE INTO tickets (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, completado, fecha_creacion) KEY (id) VALUES (2, 'Ticket Test 2', 'Descripcion 2', 'ALTA', 1, 2, false, false, CURRENT_TIMESTAMP);
