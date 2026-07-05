#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." &>/dev/null && pwd)"

# ── Opciones ─────────────────────────────────────────────────────
# Pasá --no-seed para omitir la carga de datos de prueba
SEED=true
for arg in "$@"; do
  [ "$arg" = "--no-seed" ] && SEED=false
done

echo "=================================================="
echo "=== REINICIO COMPLETO Y RECONSTRUCCIÓN BACKEND ==="
echo "=================================================="

echo ""
echo ">>> Paso 1: Limpiando contenedores, volúmenes e imágenes..."
cd "$PROJECT_ROOT"
docker compose down -v --remove-orphans
docker rmi gestortareas/api:latest 2>/dev/null || true

echo ""
echo ">>> Paso 2: Construyendo imagen Docker de la API con Jib..."
cd "$PROJECT_ROOT"
mvn clean compile jib:dockerBuild -DskipTests

echo ""
echo ">>> Paso 3: Desplegando servicios de Backend (DB y API)..."
cd "$PROJECT_ROOT"
docker compose up -d gestortareas-db gestortareas-api

echo ""
echo "=== Estado de los contenedores desplegados ==="
docker compose ps

# ── Seed de datos de prueba ──────────────────────────────────────
if [ "$SEED" = true ]; then
  echo ""
  echo ">>> Paso 4: Esperando que PostgreSQL esté listo..."
  until docker exec gestortareas-db pg_isready -U postgres -d gestortareasdb -q 2>/dev/null; do
    printf "."
    sleep 1
  done
  echo " listo."

  echo ""
  echo ">>> Paso 5: Esperando que la API esté disponible..."
  intentos=0
  max_intentos=40
  until curl -sf --max-time 3 http://localhost:8081/actuator/health >/dev/null 2>&1 \
     || curl -s  --max-time 3 -o /dev/null -w "%{http_code}" http://localhost:8081 2>/dev/null | grep -qvE "^(0|)$"; do
    intentos=$((intentos + 1))
    if [ "$intentos" -ge "$max_intentos" ]; then
      echo ""
      echo "    ADVERTENCIA: La API no respondió en $((max_intentos * 3))s."
      echo "    Esperando 10s adicionales antes del seed..."
      sleep 10
      break
    fi
    printf "."
    sleep 3
  done
  echo " lista."

  echo ""
  echo "    Esperando 5s para que Hibernate complete ddl-auto:update..."
  sleep 5

  echo ""
  echo ">>> Paso 6: Cargando datos de prueba..."
  docker exec -i gestortareas-db psql \
    -U postgres \
    -d gestortareasdb \
    -v ON_ERROR_STOP=1 \
    < "$SCRIPT_DIR/tasks/seed-data.sql"
  echo "    Datos de prueba cargados exitosamente."
  echo "    Usuarios: admin / maria.garcia / lucas.torres / valentina.ruiz"
  echo "    Contraseña de todos: admin"
fi

echo ""
echo "=================================================="
echo "=== REINICIO DE BACKEND FINALIZADO EXITOSAMENTE ==="
echo "=================================================="
exit 0
