#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../../.." &>/dev/null && pwd)"   # raíz del repo — donde vive pom.xml

echo "=== Construyendo Imagen Docker de gestortareas-api con Jib ==="
cd "$REPO_ROOT"
mvn clean compile jib:dockerBuild -DskipTests

echo "=== Construcción de API finalizada con éxito ==="
exit 0

