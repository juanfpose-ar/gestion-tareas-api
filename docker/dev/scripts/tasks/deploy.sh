#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." &>/dev/null && pwd)"

echo "=== Desplegando Backend (DB, Mailhog y API) con Docker Compose ==="
cd "$PROJECT_ROOT"
docker compose up -d gestortareas-db gestortareas-mailhog gestortareas-api

echo "=== Estado de los contenedores desplegados ==="
docker compose ps
exit 0

