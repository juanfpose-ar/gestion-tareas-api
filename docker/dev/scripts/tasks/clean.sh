#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." &>/dev/null && pwd)"

echo "=== Limpiando Contenedores, Volúmenes e Imágenes de Backend ==="
cd "$PROJECT_ROOT"
docker compose down -v --remove-orphans
docker rmi gestortareas/api:latest 2>/dev/null || true

echo "=== Limpieza de backend finalizada ==="
exit 0

