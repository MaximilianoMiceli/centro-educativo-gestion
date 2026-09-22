# centro-educativo-gestion

Sistema de Gestión Educativa del Centro Educativo "Educar para Transformar".
Proyecto de la asignatura Metodología de Sistemas II (UTN FRRe).

## Estructura del repositorio

| Carpeta | Contenido |
|---|---|
| `Documentacion/` | Análisis del proyecto: requerimientos, arquitectura, clasificación, casos de uso |
| `Diseño/` | Diagramas (ERD, casos de uso, capas) y wireframes de la interfaz |
| `Backend/` | API REST en Java 17 + Spring Boot 3 (Maven) |
| `Frontend/` | Aplicación web en React + TypeScript (Vite) |
| `Database/` | Documentación y scripts auxiliares de la base de datos |
| `docker-compose.yml` | PostgreSQL 15 y pgAdmin para desarrollo |

## Stack

- **Backend:** Java 21, Spring Boot 4.1.1, Spring Data JPA (Hibernate), Spring Security, OAuth2 Resource Server, JWT (jjwt 0.13.0), Flyway, springdoc (Swagger/OpenAPI 3.1.1), JUnit 5 + Mockito.
- **Frontend:** React 19, TypeScript 6.0, Vite 8, React Router 7, Redux Toolkit, Material-UI 9, Axios + React Query.
- **Base de datos:** PostgreSQL 15. Las migraciones viven en `Backend/src/main/resources/db/migration` y las aplica Flyway al iniciar el backend.
- **Contenedorización:** Docker + Docker Compose (PostgreSQL + pgAdmin).

## Puesta en marcha

Requisitos: JDK 17 o superior, Node.js 20+, Docker Desktop.

1. Levantar la base de datos (con Docker Desktop abierto):

   ```bash
   docker compose up -d
   ```

   PostgreSQL queda en `localhost:5434` (se evita el 5432/5433 por posibles conflictos con otras instancias) (base `centro_educativo`, usuario y clave `centro`). pgAdmin en http://localhost:5050 (`admin@centro.local` / `admin`).

2. Iniciar el backend (no requiere Maven instalado, usa el wrapper):

   ```bash
   cd Backend
   ./mvnw spring-boot:run
   ```

   - Health: http://localhost:8080/actuator/health
   - Swagger UI: http://localhost:8080/swagger-ui.html

3. Iniciar el frontend:

   ```bash
   cd Frontend
   npm install
   npm run dev
   ```

   Disponible en http://localhost:5173. Las llamadas a `/api` se redirigen al backend.

Las credenciales de la base se pueden sobrescribir con las variables `CENTRO_DB_URL`, `CENTRO_DB_USER` y `CENTRO_DB_PASSWORD`.

## Flujo de trabajo (Git)

Ramas: `main` (producción) → `develop` (integración) → `feature/*`.
Convención de commits: `feat(RF-MOD-ALU-002): implementar GET /api/v1/alumnos`.

## Sprints

| Sprint | Módulo | Período | Estado | Documento |
|---|---|---|---|---|
| 1 | Consulta de Alumnos (RF-MOD-ALU-002) | 21/09 - 22/09 | ✅ Backend completo | [SPRINT_1.md](SPRINT_1.md) |
| 2 | Edición de Docentes (RF-MOD-PROF-004) | 23/09 - 06/10 | 📋 Próximo | - |
| 3 | Administración de Usuarios y Permisos (RF-MOD-ADM-008) | 07/10 - 20/10 | 📋 Próximo | - |
| Cierre | Pruebas y despliegue | 21/10 - 27/10 | 📋 Próximo | - |

## Sprint 1: Estado y Próximas Tareas

### ✅ Completado
- **Backend API:** 4 endpoints REST de búsqueda de alumnos
- **Seguridad:** JWT + OAuth2 Resource Server configurado
- **Base de datos:** Migraciones Flyway (V1 schema, V2 datos de prueba)
- **Auditoría:** Registro de cada consulta en `log_auditoria`
- **Documentación:** Swagger/OpenAPI con todos los endpoints

### 📋 Próximo (Frontend + Completar Sprint 1)
- Pantalla React para búsqueda de alumnos
- Endpoint `/login` para generación de tokens JWT
- Tests unitarios (JUnit 5 + Mockito)
- Validación de RBAC (profesor solo ve sus alumnos)

Ver [SPRINT_1.md](SPRINT_1.md) para detalles completos, endpoints, datos de prueba y cómo testear.
