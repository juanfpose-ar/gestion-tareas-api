#!/bin/bash
# Backup de la base de datos de producción vía pg_dump dentro del contenedor.
# Pensado para correr por cron en el VPS (ver PRODUCCION.md). Guarda dumps comprimidos
# y borra los que superen la retención. IMPORTANTE: copiar los dumps FUERA del VPS
# (rclone/scp a otro storage) — un backup en el mismo disco no sobrevive a un desastre del disco.
#
# Uso:  ./backup-db.sh [directorio_destino]   (default: ./backups)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"
BACKUP_DIR="${1:-$SCRIPT_DIR/../backups}"
CONTAINER="gestortareas-db-prod"
RETENTION_DAYS=14

if [[ ! -f "$ENV_FILE" ]]; then
    echo "ERROR: no existe $ENV_FILE (se necesita para leer POSTGRES_USER/POSTGRES_DB)" >&2
    exit 1
fi

# Lee solo las 2 variables necesarias del .env (sin exportar todo el archivo)
POSTGRES_USER=$(grep -E '^POSTGRES_USER=' "$ENV_FILE" | cut -d= -f2-)
POSTGRES_DB=$(grep -E '^POSTGRES_DB=' "$ENV_FILE" | cut -d= -f2-)

mkdir -p "$BACKUP_DIR"
FECHA=$(date +%Y%m%d_%H%M%S)
DESTINO="$BACKUP_DIR/gestortareas_${FECHA}.sql.gz"

echo ">>> pg_dump de '$POSTGRES_DB' desde el contenedor $CONTAINER..."
docker exec "$CONTAINER" pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" | gzip > "$DESTINO"

# Un dump vacío o de pocos bytes casi seguro es un fallo silencioso — mejor avisar acá.
if [[ ! -s "$DESTINO" ]] || [[ $(stat -c%s "$DESTINO") -lt 1024 ]]; then
    echo "ERROR: el dump quedó vacío o sospechosamente chico: $DESTINO" >&2
    exit 1
fi

echo ">>> Backup OK: $DESTINO ($(du -h "$DESTINO" | cut -f1))"

echo ">>> Borrando backups locales con más de $RETENTION_DAYS días..."
find "$BACKUP_DIR" -name "gestortareas_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo ">>> Listo. Recordatorio: sincronizá $BACKUP_DIR a un storage externo (rclone/scp)."
