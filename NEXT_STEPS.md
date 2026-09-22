# Próximos Pasos - Sprint 1 Continuación

Checklist para completar Sprint 1 y avanzar hacia Sprint 2.

---

## ✅ Lo que YA está listo

- [x] Backend API completo (4 endpoints REST)
- [x] Seguridad JWT configurada
- [x] Base de datos con datos de prueba
- [x] Documentación (Swagger/OpenAPI)
- [x] Auditoría funcionando
- [x] Compilación exitosa

**¿Qué falta?**
- [ ] Frontend (React)
- [ ] Endpoint `/login`
- [ ] Tests unitarios
- [ ] Commits a Git

---

## 📋 Checklist - Hoy/Mañana

### Opción A: Verificar lo Existente (15 min)
- [ ] Lee [TESTING.md](TESTING.md)
- [ ] Levanta Docker: `docker compose up -d`
- [ ] Levanta Backend: `cd Backend && ./mvnw spring-boot:run`
- [ ] Abre Swagger: http://localhost:8080/swagger-ui.html
- [ ] Prueba un endpoint (GET /api/v1/alumnos/1)
- [ ] Verifica auditoría: `docker exec centro-postgres psql -U centro -d centro_educativo -c "SELECT * FROM log_auditoria LIMIT 5;"`

### Opción B: Commitear Cambios (30 min)
- [ ] `git checkout -b develop` (crear rama develop)
- [ ] `git checkout -b feature/RF-MOD-ALU-002-consulta-alumnos` (feature branch)
- [ ] `git add Backend/ SPRINT_1.md TESTING.md NEXT_STEPS.md pom.xml ...`
- [ ] `git commit -m "feat(RF-MOD-ALU-002): Entidades y repositorios de alumnos, cursos, usuarios"`
- [ ] Repetir para: Servicios, Controllers, Seguridad, Datos de Prueba (5 commits)
- [ ] `git push origin feature/RF-MOD-ALU-002-consulta-alumnos`

### Opción C: Agregar Frontend (2-3 horas)
Comenzar la pantalla React:
- [ ] Crea componente `Frontend/src/components/AlumnoSearch.tsx`
- [ ] Formulario con inputs: legajo, DNI, nombre
- [ ] Botones de búsqueda
- [ ] Tabla para mostrar resultado
- [ ] Manejo de errores (404, 401)
- [ ] Integración con servicio HTTP

### Opción D: Implementar /login (1-2 horas)
Endpoint de autenticación:
- [ ] Crear `LoginController.java`
- [ ] Endpoint: `POST /api/login` → recibe username/password
- [ ] Validar credenciales contra BD
- [ ] Generar token con `JwtTokenProvider`
- [ ] Retornar token en respuesta
- [ ] Tests unitarios

---

## 🔧 Tareas Detalladas

### Si Opción B (Commits)

```bash
# 1. Crear ramas
git checkout -b develop
git checkout -b feature/RF-MOD-ALU-002-consulta-alumnos

# 2. Commit 1: Entidades y Repositorios
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/domain/
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/repository/
git commit -m "feat(RF-MOD-ALU-002): Entidades y repositorios

- Entidades JPA: Alumno, Curso, NivelEducativo, Usuario, Docente, LogAuditoria, Rol
- Repositorios con especificaciones JPA para búsquedas dinámicas
- Índices en campos de búsqueda (apellido, nombre, DNI, curso_id)"

# 3. Commit 2: Servicios y DTOs
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/service/
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/web/dto/
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/exception/
git commit -m "feat(RF-MOD-ALU-002): Servicios de auditoría y búsqueda

- AlumnoService: búsquedas por legajo, DNI, nombre
- AuditoriaService: registro de operaciones
- DTOs para transferencia de datos
- Excepciones de dominio"

# 4. Commit 3: Controlador REST
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/web/controller/
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/web/exception/ExceptionHandler.java
git commit -m "feat(RF-MOD-ALU-002): Controlador REST con 4 endpoints

- GET /api/v1/alumnos/{id}
- GET /api/v1/alumnos/buscar/legajo
- GET /api/v1/alumnos/buscar/dni
- GET /api/v1/alumnos/buscar/nombre
- Documentación OpenAPI/Swagger completa
- Manejo centralizado de errores"

# 5. Commit 4: Seguridad JWT
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/security/
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/config/SecurityConfig.java
git add Backend/pom.xml
git add Backend/src/main/resources/application.yml
git commit -m "feat(RF-MOD-ALU-002): Seguridad JWT + OAuth2 Resource Server

- OAuth2 Resource Server con JWT (HS512)
- Generación y validación de tokens
- Dependencia jjwt 0.13.0
- Configuración de expiración y secreto"

# 6. Commit 5: Datos de Prueba y Documentación
git add Backend/src/main/resources/db/migration/V2__datos_prueba.sql
git add Backend/src/main/java/ar/edu/utn/centroeducativo/gestion/util/
git add SPRINT_1.md TESTING.md
git add README.md
git commit -m "feat(RF-MOD-ALU-002): Datos de prueba y documentación

- Migración V2 con usuarios, alumnos, cursos, docentes
- Documentación técnica en SPRINT_1.md
- Guía de testing en TESTING.md
- Actualización de README.md con stack real"

# 7. Push a remoto (si tienes GitHub configurado)
git push origin feature/RF-MOD-ALU-002-consulta-alumnos
```

### Si Opción C (Frontend)

**Crear `Frontend/src/services/alumnoService.ts`:**
```typescript
import axios from 'axios';

const API_URL = '/api/v1/alumnos';

export interface AlumnoDTO {
  id: number;
  legajo: string;
  dni: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  domicilio: string;
  telefono: string;
  email: string;
  cursoId: number;
  cursoDescripcion: string;
  nivelNombre: string;
  estado: string;
}

export const alumnoService = {
  obtenerPorId: (id: number, token: string) =>
    axios.get<AlumnoDTO>(`${API_URL}/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    }),
  
  buscarPorLegajo: (legajo: string, token: string) =>
    axios.get<AlumnoDTO>(`${API_URL}/buscar/legajo?legajo=${legajo}`, {
      headers: { Authorization: `Bearer ${token}` }
    }),
  
  buscarPorDni: (dni: string, token: string) =>
    axios.get<AlumnoDTO>(`${API_URL}/buscar/dni?dni=${dni}`, {
      headers: { Authorization: `Bearer ${token}` }
    }),
  
  buscarPorNombre: (texto: string, token: string) =>
    axios.get<AlumnoDTO>(`${API_URL}/buscar/nombre?texto=${texto}`, {
      headers: { Authorization: `Bearer ${token}` }
    }),
};
```

**Crear `Frontend/src/components/AlumnoSearch.tsx`:**
```typescript
import React, { useState } from 'react';
import { Box, Button, TextField, Card, CircularProgress, Alert } from '@mui/material';
import { alumnoService, AlumnoDTO } from '../services/alumnoService';

export const AlumnoSearch: React.FC<{ token: string }> = ({ token }) => {
  const [legajo, setLegajo] = useState('');
  const [resultado, setResultado] = useState<AlumnoDTO | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const buscar = async () => {
    setCargando(true);
    setError('');
    try {
      const res = await alumnoService.buscarPorLegajo(legajo, token);
      setResultado(res.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al buscar');
    } finally {
      setCargando(false);
    }
  };

  return (
    <Box>
      <TextField
        label="Legajo"
        value={legajo}
        onChange={(e) => setLegajo(e.target.value)}
      />
      <Button onClick={buscar} disabled={cargando}>
        {cargando ? <CircularProgress size={24} /> : 'Buscar'}
      </Button>
      {error && <Alert severity="error">{error}</Alert>}
      {resultado && (
        <Card>
          <h3>{resultado.nombre} {resultado.apellido}</h3>
          <p>Legajo: {resultado.legajo}</p>
          <p>Curso: {resultado.cursoDescripcion}</p>
        </Card>
      )}
    </Box>
  );
};
```

---

## ⏰ Estimaciones de Tiempo

| Tarea | Tiempo |
|---|---|
| Testing manual (Opción A) | 15 min |
| Commits (Opción B) | 30 min |
| Frontend básico (Opción C) | 2-3 h |
| Endpoint /login (Opción D) | 1-2 h |
| Tests unitarios (bonus) | 1-2 h |

---

## 🎯 Meta para Hito 1 (22/09)

**Mínimo indispensable:**
- [x] Backend con 4 endpoints ✅
- [ ] Frontend con búsqueda funcional (Opción C)
- [ ] Demostración en vivo

**Opcional:**
- [ ] Endpoint /login (Opción D)
- [ ] Tests (bonus)

---

## 📊 Progreso Actual

| Aspecto | Estado |
|---|---|
| Backend | ✅ 100% (31 archivos) |
| BD + Migraciones | ✅ 100% (9 tablas) |
| Documentación | ✅ 100% (SPRINT_1.md, TESTING.md) |
| Seguridad JWT | ✅ 100% (OAuth2 + HS512) |
| Auditoría | ✅ 100% (log_auditoria) |
| Frontend | ⏳ 0% (pendiente) |
| Endpoint /login | ⏳ 0% (pendiente) |
| Tests unitarios | ⏳ 0% (pendiente) |

---

## 💡 Recomendación

1. **Hoy (21/09):** Hazle commits al backend (Opción B: 30 min)
2. **Hoy/Mañana:** Implementa frontend básico (Opción C: 2-3 h)
3. **Mañana (22/09):** Testea todo + prepara demostración

Si hay tiempo:
4. Endpoint /login (Opción D)
5. Tests unitarios (bonus)

---

## 🚨 Evitar

- No hagas commits sin probar que compila
- No cambies `pom.xml` sin entender las dependencias
- No borres las migraciones SQL (son el historial de BD)
- No modifiques `application.yml` sin tener Docker corriendo

---

## Contacto / Dudas

Si algo no sale:
1. Revisa [TESTING.md](TESTING.md)
2. Revisa [SPRINT_1.md](SPRINT_1.md)
3. Verifica que Docker está corriendo: `docker ps`
4. Verifica que compilaste: `cd Backend && ./mvnw clean compile`

¡Adelante! 🚀
