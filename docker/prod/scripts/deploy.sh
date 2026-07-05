#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
COMPOSE_DIR="$(cd "$SCRIPT_DIR/.." &>/dev/null && pwd)"        # docker/prod — donde vive docker-compose.yaml
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." &>/dev/null && pwd)"    # raíz del repo — donde vive pom.xml

if [ ! -f "$COMPOSE_DIR/.env" ]; then
  echo "ERROR: falta $COMPOSE_DIR/.env"
  echo "       Copiá .env.example a .env y completá los valores antes de desplegar."
  exit 1
fi

echo "=================================================="
echo "=== DEPLOY DE PRODUCCIÓN ==="
echo "=================================================="

echo ""
echo ">>> Paso 1: Construyendo imagen Docker de la API con Jib..."
cd "$REPO_ROOT"
mvn clean compile jib:dockerBuild -DskipTests

echo ""
echo ">>> Paso 2: Desplegando servicios (DB, API, Web/nginx)..."
cd "$COMPOSE_DIR"
docker compose --env-file .env up -d --build

echo ""
echo "=== Estado de los contenedores desplegados ==="
docker compose ps

echo ""
echo "=================================================="
echo "=== DEPLOY FINALIZADO ==="
echo "=================================================="
exit 0
