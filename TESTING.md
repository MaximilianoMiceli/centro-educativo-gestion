# Guía de Testing - Sprint 1

Instrucciones para probar el sistema de consulta de alumnos.

---

## Opción 1: Testing rápido con Swagger UI (Recomendado)

Esta es la forma más fácil. No requiere generar tokens manualmente.

### Paso 1: Levantar la BD
```bash
docker compose up -d
```

Espera 5 segundos a que PostgreSQL esté listo. Deberías ver:
```
✓ Container centro-postgres Healthy
```

### Paso 2: Levantar el Backend
```bash
cd Backend
./mvnw spring-boot:run
```

Espera a ver:
```
  2026-09-21 21:00:00.000 INFO  [...] Started GestionApplication in 15.234 seconds
```

### Paso 3: Abrir Swagger UI
- Abre en el navegador: **http://localhost:8080/swagger-ui.html**
- Deberías ver la lista de endpoints bajo **Alumnos**

### Paso 4: Autorización
1. Haz clic en el botón **"Authorize"** (arriba a la derecha)
2. En el modal que aparece, pega este token:
   ```
   eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJvbCI6IkFETUluIiwiaWF0IjoxNjk0Nzk2MjAwLCJleHAiOjE2OTQ4Mzk0MDB9.s-k1LjX9nHcKGFW2Jv4KP5nUcLQXc5z8mZpVq9rR3X-8sT7pHjL4Q1kOJGpT1A_9KpQ2fFhR0e7vQwY1Z2BqXw
   ```
3. Haz clic en **"Authorize"** → **"Close"**

### Paso 5: Probar Endpoints

#### 5a. Obtener alumno por ID
1. Expande **GET /api/v1/alumnos/{id}**
2. Haz clic en **"Try it out"**
3. En el campo `id`, ingresa: `1`
4. Haz clic en **"Execute"**

Deberías ver una respuesta **200 OK** con los datos de Carlos García.

#### 5b. Buscar por Legajo
1. Expande **GET /api/v1/alumnos/buscar/legajo**
2. Haz clic en **"Try it out"**
3. En el campo `legajo`, ingresa: `ALU001`
4. Haz clic en **"Execute"**

Respuesta esperada: Carlos García con DNI 45678901

#### 5c. Buscar por DNI
1. Expande **GET /api/v1/alumnos/buscar/dni**
2. Haz clic en **"Try it out"**
3. En el campo `dni`, ingresa: `45678901`
4. Haz clic en **"Execute"**

Respuesta esperada: Carlos García con legajo ALU001

#### 5d. Buscar por Nombre
1. Expande **GET /api/v1/alumnos/buscar/nombre**
2. Haz clic en **"Try it out"**
3. En el campo `texto`, ingresa: `Ana`
4. Haz clic en **"Execute"**

Respuesta esperada: Ana Martínez

---

## Opción 2: Testing con curl (Terminal)

### Prerequisitos
- Terminal PowerShell o cmd
- El backend corriendo (paso 2 de arriba)

### Paso 1: Generar Token
En PowerShell:
```powershell
$TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJvbCI6IkFETUluIiwiaWF0IjoxNjk0Nzk2MjAwLCJleHAiOjE2OTQ4Mzk0MDB9.s-k1LjX9nHcKGFW2Jv4KP5nUcLQXc5z8mZpVq9rR3X-8sT7pHjL4Q1kOJGpT1A_9KpQ2fFhR0e7vQwY1Z2BqXw"
```

### Paso 2: Consultar alumno por ID
```powershell
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/alumnos/1 | ConvertFrom-Json | ConvertTo-Json
```

Respuesta esperada:
```json
{
  "id": 1,
  "legajo": "ALU001",
  "dni": "45678901",
  "nombre": "Carlos",
  "apellido": "García",
  "cursoDescripcion": "1° A",
  "nivelNombre": "SECUNDARIO",
  "estado": "ACTIVO"
}
```

### Paso 3: Buscar por legajo
```powershell
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/alumnos/buscar/legajo?legajo=ALU002" | ConvertFrom-Json | ConvertTo-Json
```

### Paso 4: Verificar auditoría
```powershell
docker exec centro-postgres psql -U centro -d centro_educativo -c "SELECT usuario_id, accion, entidad, entidad_id, detalle FROM log_auditoria ORDER BY fecha DESC LIMIT 5;"
```

Deberías ver registros como:
```
 usuario_id | accion  | entidad | entidad_id |    detalle
            | CONSULTA | ALUMNO  |          1 | Búsqueda por ID: 1
```

---

## Datos Disponibles para Testear

### Usuarios
- `admin` (rol ADMIN)
- `profesor1` (rol PROFESOR)

### Alumnos
| ID | Legajo | DNI | Nombre | Apellido | Curso |
|----|--------|-----|--------|----------|-------|
| 1 | ALU001 | 45678901 | Carlos | García | 1° A |
| 2 | ALU002 | 56789012 | Ana | Martínez | 1° A |
| 3 | ALU003 | 67890123 | Luis | Fernández | 1° B |
| 4 | ALU004 | 78901234 | Paula | González | 2° A |

### Búsquedas para Probar
- ID: 1, 2, 3, 4
- Legajo: ALU001, ALU002, ALU003, ALU004
- DNI: 45678901, 56789012, 67890123, 78901234
- Nombres: Carlos, Ana, Luis, Paula, García, Martínez, Fernández, González

---

## Validaciones Esperadas

### ✅ Caso Exitoso (200 OK)
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/alumnos/1
```
→ Retorna JSON con los datos del alumno

### ❌ Sin Token (401 Unauthorized)
```bash
curl http://localhost:8080/api/v1/alumnos/1
```
→ Error 401: "WWW-Authenticate: Bearer"

### ❌ Token Inválido (401 Unauthorized)
```bash
curl -H "Authorization: Bearer INVALID" http://localhost:8080/api/v1/alumnos/1
```
→ Error 401

### ❌ Alumno No Existe (404 Not Found)
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/alumnos/999
```
→ Error 404: "Alumno con ID 999 no encontrado"

---

## Health Check

### Verificar que el backend está corriendo
```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### Ver información de la aplicación
```bash
curl http://localhost:8080/actuator/info
```

---

## Auditoría

Cada búsqueda se registra en la tabla `log_auditoria`. Para ver los logs:

```bash
docker exec centro-postgres psql -U centro -d centro_educativo <<EOF
SELECT 
  id,
  usuario_id,
  accion,
  entidad,
  entidad_id,
  detalle,
  fecha
FROM log_auditoria
ORDER BY fecha DESC
LIMIT 10;
EOF
```

---

## Solución de Problemas

### "Connection refused" al conectar a PostgreSQL
→ PostgreSQL no está corriendo. Ejecutá: `docker compose up -d`

### "401 Unauthorized" en Swagger
→ El token está mal copiado o es antiguo. Copia y pega nuevamente.

### "404 Not Found" para alumno 1
→ La base de datos no tiene datos. Ejecutá: `./mvnw test` (aplica las migraciones)

### Backend no arrancar
→ Ejecutá: `cd Backend && ./mvnw clean compile`

### Swagger no carga (404)
→ El backend no está corriendo. Ejecutá: `cd Backend && ./mvnw spring-boot:run`

---

## Logs

### Ver logs del backend en tiempo real
Si ejecutaste `./mvnw spring-boot:run`, verás logs en la terminal:
```
2026-09-21 21:05:23.123 INFO  [...] Starting GestionApplication
2026-09-21 21:05:24.456 INFO  [...] Creating new datasource connection
2026-09-21 21:05:25.789 DEBUG [...] GET /api/v1/alumnos/1 - 200 OK in 45ms
```

### Errores comunes en logs
- `FATAL: password authentication failed` → Variables de entorno con `DB_` conflictivas
- `Connection refused` → PostgreSQL no está corriendo
- `No qualifying bean of type 'AlumnoRepository'` → Falta una anotación `@Repository`

---

## Checklist de Pruebas

- [ ] PostgreSQL levantado (`docker ps | grep centro-postgres`)
- [ ] Backend compilado (`./mvnw compile`)
- [ ] Backend corriendo (`./mvnw spring-boot:run`)
- [ ] Swagger UI accesible (http://localhost:8080/swagger-ui.html)
- [ ] Autorización con token funcionando
- [ ] GET /api/v1/alumnos/1 retorna 200 OK
- [ ] GET /api/v1/alumnos/buscar/legajo?legajo=ALU001 retorna 200 OK
- [ ] GET /api/v1/alumnos/buscar/dni?dni=45678901 retorna 200 OK
- [ ] GET /api/v1/alumnos/buscar/nombre?texto=Carlos retorna 200 OK
- [ ] Auditoría registra cada búsqueda (`log_auditoria`)
- [ ] Error 404 cuando no existe alumno
- [ ] Error 401 cuando no hay token

---

## Próximos Pasos

Una vez que verificaste que todo funciona:
1. Revisa [SPRINT_1.md](SPRINT_1.md) para entender la arquitectura
2. Explora el código en `Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/`
3. Prepárate para el frontend (React) en Sprint 1 (próximas tareas)
4. Descarga los tokens de prueba en un `.env` para no copiarlos cada vez

---

## Dudas o Errores

Si algo no funciona como se describe:
1. Verificá que compilaste: `cd Backend && ./mvnw compile`
2. Verificá que Docker está corriendo: `docker ps`
3. Verificá que las tablas existen: `docker exec centro-postgres psql -U centro -d centro_educativo -c "\dt"`
4. Revisa los logs del backend (donde ejecutaste `./mvnw spring-boot:run`)

¡Buenas pruebas! 🚀
