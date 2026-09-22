#!/bin/bash
# Script para testear Sprint 1
# Uso: ./test-sprint1.sh

set -e

echo "================================"
echo "Sprint 1: Consulta de Alumnos"
echo "================================"
echo ""

# 1. Verificar que Docker está corriendo
echo "1️⃣  Verificando Docker..."
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker no está corriendo. Abre Docker Desktop."
    exit 1
fi
echo "✅ Docker está corriendo"
echo ""

# 2. Levantar PostgreSQL si no está corriendo
echo "2️⃣  Levantando PostgreSQL..."
if ! docker ps | grep -q centro-postgres; then
    docker compose up -d postgres
    echo "⏳ Esperando a que PostgreSQL esté listo..."
    sleep 5
fi
echo "✅ PostgreSQL está listo en localhost:5434"
echo ""

# 3. Compilar el backend
echo "3️⃣  Compilando backend..."
cd Backend
./mvnw -q clean compile
echo "✅ Backend compilado"
echo ""

# 4. Información sobre endpoints
echo "4️⃣  Endpoints disponibles:"
echo "   • GET /api/v1/alumnos/{id}"
echo "   • GET /api/v1/alumnos/buscar/legajo?legajo=ALU001"
echo "   • GET /api/v1/alumnos/buscar/dni?dni=45678901"
echo "   • GET /api/v1/alumnos/buscar/nombre?texto=Carlos"
echo ""

# 5. Mostrar cómo generar token
echo "5️⃣  Para generar un token JWT de prueba:"
echo "   java -cp target/classes:Backend/target/classes io.jsonwebtoken.Jwts"
echo ""
echo "   O usa este comando en otra terminal:"
echo "   cd Backend && ./mvnw spring-boot:run"
echo "   Luego abre http://localhost:8080/swagger-ui.html"
echo ""

# 6. Mostrar datos de prueba
echo "6️⃣  Datos de prueba disponibles:"
docker exec centro-postgres psql -U centro -d centro_educativo <<EOF
\echo '   Alumnos:'
SELECT '     - ' || id || ': ' || nombre || ' ' || apellido || ' (DNI: ' || dni || ', Legajo: ' || legajo || ')' as alumno FROM alumno ORDER BY id;
\echo '   Usuarios:'
SELECT '     - ' || username || ' (Rol: ' || COALESCE(r.nombre, '?') || ')' as usuario FROM usuario u LEFT JOIN rol r ON u.rol_id = r.id ORDER BY u.id;
EOF
echo ""

echo "================================"
echo "✅ Listo para testear Sprint 1"
echo "================================"
echo ""
echo "Próximos pasos:"
echo "1. Abre otra terminal y corre: cd Backend && ./mvnw spring-boot:run"
echo "2. Espera a que el backend arranque (30-60 segundos)"
echo "3. Abre http://localhost:8080/swagger-ui.html"
echo "4. Genera un token en http://localhost:8080/swagger-ui.html > Authorize"
echo "5. Prueba los endpoints"
echo ""
