# Setup para Notebook - Centro Educativo Gestión

## ✅ Prerequisites (Instalaciones previas)

### 1. **Java 21**
- Descargar de: https://www.oracle.com/java/technologies/downloads/#java21
- Verificar: `java -version` (debe mostrar Java 21)
- Configurar JAVA_HOME en variables de entorno del sistema

### 2. **Docker Desktop**
- Descargar de: https://www.docker.com/products/docker-desktop
- Iniciar Docker Desktop
- Verificar: `docker --version`

### 3. **Node.js 22+**
- Descargar de: https://nodejs.org/
- Verificar: `node --version` y `npm --version`

### 4. **Git**
- Descargar de: https://git-scm.com/
- Verificar: `git --version`

## 🚀 Pasos de Setup

### 1. Clonar el repositorio
```bash
git clone <repo-url>
cd centro-educativo-gestion
```

### 2. Backend - Levantarla base de datos

```bash
# En la carpeta raíz del proyecto
docker-compose up -d
```

Verificar que PostgreSQL está corriendo:
```bash
docker-compose ps
```

Debería mostrar `centro-db` corriendo en puerto `5434`

### 3. Backend - Ejecutar Spring Boot

```bash
cd Backend
mvn clean compile spring-boot:run -DskipTests
```

**Esperar hasta ver:**
```
Started Application in X seconds
```

El backend estará disponible en: `http://localhost:8080`

**Verificar que funciona:**
- Abrir: http://localhost:8080/swagger-ui.html (debería ver la documentación API)
- Verificar Flyway migró: `docker exec centro-db psql -U centro -d centro_educativo -c "SELECT * FROM alumno LIMIT 1;"`

### 4. Frontend - Instalar dependencias

```bash
cd Frontend
npm install
```

### 5. Frontend - Ejecutar Vite dev server

```bash
npm run dev
```

Debería ver algo como:
```
  ➜  Local:   http://localhost:5173/
```

Abrir http://localhost:5173 en el navegador

## 🔐 Credenciales de Demo

**Login:**
- Usuario: `admin`
- Contraseña: `password` (también `profesor1` / `password`)

**Datos de Prueba:**
- Legajos: `ALU001`, `ALU002`, `ALU003`, `ALU004`
- DNIs: `45678901`, `56789012`, `67890123`, `78901234`
- IDs: `1`, `2`, `3`, `4`

## 📝 Variables de Entorno (Backend)

Si quieres cambiar la configuración, edita `Backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/centro_educativo
    username: centro
    password: centro

app:
  jwt:
    secret: esta-es-una-clave-secreta-para-desarrollo-solamente-cambiar-en-produccion
    expiration: 3600000  # 1 hora en milisegundos
```

## 🐛 Troubleshooting

### El backend no inicia
```bash
# Verificar que Java 21 está configurado
java -version

# Limpiar cache Maven
mvn clean

# Ver error completo
mvn spring-boot:run -DskipTests -e
```

### PostgreSQL no inicia
```bash
# Ver logs
docker logs centro-db

# Reiniciar
docker-compose down
docker-compose up -d
```

### Frontend no conecta al backend
- Verificar que backend está en http://localhost:8080
- Abrir DevTools (F12) → Network
- El header `Authorization` debe tener un Bearer token válido
- Si ves error CORS: el frontend debe correr en uno de los orígenes de `app.cors.allowed-origins` (por defecto `http://localhost:5173`)
- Si el login dice "No se pudo conectar": el backend no está corriendo o no responde en `http://localhost:8080`; si usas otra URL, define `VITE_API_URL` en `Frontend/.env.local`

### Puerto en uso
```bash
# Si 5173 está en uso (frontend)
npm run dev -- --port 3000

# Si 8080 está en uso (backend)
# Editar Backend/src/main/resources/application.yml: 
# server:
#   port: 8081
```

## 📋 Checklist Final

- [ ] Java 21 instalado y configurado
- [ ] Docker Desktop corriendo
- [ ] Node.js 22+ instalado
- [ ] `docker-compose up -d` ejecutado (PostgreSQL corriendo)
- [ ] Backend compiló y está en http://localhost:8080
- [ ] Frontend está en http://localhost:5173
- [ ] Puedes hacer login
- [ ] Puedes buscar alumnos
- [ ] Logs de auditoría se registran en BD

## 🎯 Accesos rápidos

- **Backend Swagger:** http://localhost:8080/swagger-ui.html
- **Frontend:** http://localhost:5173
- **PostgreSQL:** `localhost:5434` (usuario: `centro`, password: `centro`)

## 📞 Notas importantes

1. **Primer startup es lento** - Flyway migra la BD automáticamente (solo primera vez)
2. **Token expira cada 1 hora** - Logout y vuelve a login para obtener uno nuevo
3. **Los datos de prueba se insertan automáticamente** en la primera migración
4. **No tocar la carpeta `target/`** del Backend - se regenera con cada build

---

¡Listo! Debería funcionar sin problemas 🚀
