CREATE SCHEMA IF NOT EXISTS gestortareas;
SET SCHEMA gestortareas;

-- La tabla "usuarios" la crea Hibernate (ddl-auto=update) a partir de la entidad Usuario,
-- y DataSeeder inserta el usuario "admin" vía JPA — así el esquema real nunca queda
-- desincronizado con este fixture cuando la entidad cambia (columnas nuevas, NOT NULL, etc).

CREATE TABLE IF NOT EXISTS tableros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    imagen_fondo_url VARCHAR(500),
    creador_id BIGINT,
    archivado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tablero_miembros (
    tablero_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (tablero_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS estados_tablero (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    orden INT NOT NULL,
    color_hex VARCHAR(20),
    tablero_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS etiquetas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    color_hex VARCHAR(20),
    tablero_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion CLOB,
    prioridad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    tablero_id BIGINT NOT NULL,
    estado_id BIGINT NOT NULL,
    archivado BOOLEAN NOT NULL DEFAULT FALSE,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_archivado TIMESTAMP,
    fecha_vencimiento TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket_etiquetas (
    ticket_id BIGINT NOT NULL,
    etiqueta_id BIGINT NOT NULL,
    PRIMARY KEY (ticket_id, etiqueta_id)
);

CREATE TABLE IF NOT EXISTS checklist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contenido VARCHAR(255) NOT NULL,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    ticket_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS notas_tarea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contenido CLOB NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    autor_id BIGINT,
    ticket_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS recordatorios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora TIMESTAMP NOT NULL,
    tipo_recordatorio VARCHAR(20) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    ticket_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS adjuntos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    tipo_adjunto VARCHAR(20) NOT NULL,
    tamanio_bytes BIGINT,
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ticket_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS vinculos_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origen_ticket_id BIGINT NOT NULL,
    destino_ticket_id BIGINT NOT NULL,
    tipo_vinculo VARCHAR(50) NOT NULL DEFAULT 'BLOQUEA'
);

CREATE TABLE IF NOT EXISTS versiones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    fecha_vencimiento TIMESTAMP NOT NULL,
    tablero_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS conversaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asunto VARCHAR(255) NOT NULL,
    fecha_ultima_actividad TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversacion_id BIGINT NOT NULL,
    emisor_id BIGINT NOT NULL,
    contenido CLOB NOT NULL,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuario_conversacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    conversacion_id BIGINT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    archivada BOOLEAN NOT NULL DEFAULT FALSE,
    eliminada BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT unique_usuario_conversacion UNIQUE (usuario_id, conversacion_id)
);
