CREATE TABLE rol (
    id      SMALLINT PRIMARY KEY,
    nombre  VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO rol (id, nombre) VALUES
    (1, 'ADMIN'),
    (2, 'DIRECCION'),
    (3, 'PROFESOR'),
    (4, 'PADRE'),
    (5, 'ALUMNO');

CREATE TABLE nivel_educativo (
    id      SMALLINT PRIMARY KEY,
    nombre  VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO nivel_educativo (id, nombre) VALUES
    (1, 'INICIAL'),
    (2, 'PRIMARIO'),
    (3, 'SECUNDARIO');

CREATE TABLE curso (
    id        BIGSERIAL PRIMARY KEY,
    nivel_id  SMALLINT NOT NULL REFERENCES nivel_educativo (id),
    anio      SMALLINT NOT NULL CHECK (anio > 0),
    seccion   VARCHAR(5) NOT NULL,
    UNIQUE (nivel_id, anio, seccion)
);

CREATE TABLE docente (
    id            BIGSERIAL PRIMARY KEY,
    legajo        VARCHAR(20) NOT NULL UNIQUE,
    dni           VARCHAR(12) NOT NULL UNIQUE,
    nombre        VARCHAR(80) NOT NULL,
    apellido      VARCHAR(80) NOT NULL,
    especialidad  VARCHAR(80),
    email         VARCHAR(120) UNIQUE,
    telefono      VARCHAR(20),
    estado        VARCHAR(15) NOT NULL DEFAULT 'ACTIVO'
        CHECK (estado IN ('ACTIVO', 'INACTIVO', 'LICENCIA'))
);

CREATE TABLE docente_curso (
    docente_id  BIGINT NOT NULL REFERENCES docente (id),
    curso_id    BIGINT NOT NULL REFERENCES curso (id),
    PRIMARY KEY (docente_id, curso_id)
);

CREATE TABLE alumno (
    id                BIGSERIAL PRIMARY KEY,
    legajo            VARCHAR(20) NOT NULL UNIQUE,
    dni               VARCHAR(12) NOT NULL UNIQUE,
    nombre            VARCHAR(80) NOT NULL,
    apellido          VARCHAR(80) NOT NULL,
    fecha_nacimiento  DATE NOT NULL,
    domicilio         VARCHAR(200),
    telefono          VARCHAR(20),
    email             VARCHAR(120),
    curso_id          BIGINT NOT NULL REFERENCES curso (id),
    estado            VARCHAR(15) NOT NULL DEFAULT 'ACTIVO'
        CHECK (estado IN ('ACTIVO', 'INACTIVO', 'EGRESADO'))
);

CREATE INDEX idx_alumno_apellido_nombre ON alumno (LOWER(apellido), LOWER(nombre));
CREATE INDEX idx_alumno_curso ON alumno (curso_id);

CREATE TABLE usuario (
    id             BIGSERIAL PRIMARY KEY,
    username       VARCHAR(20) NOT NULL UNIQUE,
    email          VARCHAR(120) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    rol_id         SMALLINT NOT NULL REFERENCES rol (id),
    docente_id     BIGINT REFERENCES docente (id),
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE log_auditoria (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT REFERENCES usuario (id),
    accion      VARCHAR(50) NOT NULL,
    entidad     VARCHAR(50) NOT NULL,
    entidad_id  BIGINT,
    detalle     TEXT,
    fecha       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_log_usuario ON log_auditoria (usuario_id);
CREATE INDEX idx_log_entidad ON log_auditoria (entidad, entidad_id);
