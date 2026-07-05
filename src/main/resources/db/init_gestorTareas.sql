-- Ejecutado por Postgres al inicializar el contenedor (docker-entrypoint-initdb.d).
-- El datasource de la API usa ?currentSchema=gestortareas, que sólo fija el
-- search_path de la sesión: el schema debe existir de antemano.
CREATE SCHEMA IF NOT EXISTS gestortareas;
