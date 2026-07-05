#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." &>/dev/null && pwd)"

echo "=== Construyendo Imagen Docker de gestortareas-api con Jib ==="
cd "$PROJECT_ROOT"
mvn clean compile jib:dockerBuild -DskipTests

echo "=== Construcción de API finalizada con éxito ==="
exit 0

