INSERT INTO usuarios (id, username, nombre, email, password, rol) VALUES
(1, 'admin', 'Admin Test', 'admin@test.com', '$2a$10$eD8mPqT4x2jFk9.wJ.7v/e5lM6uWvR8b.k.j8gJ/3Z8g9/H3Z.6G.', 'ADMIN');

INSERT INTO tableros (id, nombre, descripcion, creador_id, fecha_creacion) VALUES
(1, 'Tablero Test', 'Descripción de prueba', 1, CURRENT_TIMESTAMP);

INSERT INTO estados_tablero (id, nombre, orden, color_hex, tablero_id) VALUES
(1, 'Por Hacer', 1, '#0ea5e9', 1),
(2, 'En Curso', 2, '#f59e0b', 1);

INSERT INTO etiquetas (id, nombre, color_hex, tablero_id) VALUES
(1, 'TestTag', '#3b82f6', 1);

INSERT INTO tickets (id, titulo, descripcion, prioridad, tablero_id, estado_id, archivado, fecha_creacion) VALUES
(1, 'Ticket Test 1', 'Descripcion 1', 'MEDIA', 1, 1, false, CURRENT_TIMESTAMP),
(2, 'Ticket Test 2', 'Descripcion 2', 'ALTA', 1, 2, false, CURRENT_TIMESTAMP);
