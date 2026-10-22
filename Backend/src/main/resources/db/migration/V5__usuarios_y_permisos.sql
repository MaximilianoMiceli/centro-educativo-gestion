CREATE TABLE padre (
    id        BIGSERIAL PRIMARY KEY,
    dni       VARCHAR(12) NOT NULL UNIQUE,
    nombre    VARCHAR(80) NOT NULL,
    apellido  VARCHAR(80) NOT NULL,
    email     VARCHAR(120),
    telefono  VARCHAR(20)
);

ALTER TABLE usuario
    ADD COLUMN tipo_documento VARCHAR(20) NOT NULL DEFAULT 'DNI',
    ADD COLUMN numero_documento VARCHAR(12) UNIQUE,
    ADD COLUMN alumno_id BIGINT REFERENCES alumno (id),
    ADD COLUMN padre_id BIGINT REFERENCES padre (id),
    ADD COLUMN debe_cambiar_password BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE usuario u
SET numero_documento = d.dni
FROM docente d
WHERE u.docente_id = d.id;

CREATE TABLE permiso (
    id           SMALLSERIAL PRIMARY KEY,
    codigo       VARCHAR(50) NOT NULL UNIQUE,
    descripcion  VARCHAR(200) NOT NULL
);

CREATE TABLE rol_permiso (
    rol_id      SMALLINT NOT NULL REFERENCES rol (id),
    permiso_id  SMALLINT NOT NULL REFERENCES permiso (id),
    PRIMARY KEY (rol_id, permiso_id)
);

INSERT INTO permiso (codigo, descripcion) VALUES
    ('ALUMNOS_CONSULTAR', 'Consultar información de alumnos'),
    ('DOCENTES_CONSULTAR', 'Consultar información de docentes'),
    ('DOCENTES_EDITAR', 'Modificar datos de docentes'),
    ('USUARIOS_ADMINISTRAR', 'Crear usuarios y asignar roles'),
    ('PERMISOS_ADMINISTRAR', 'Configurar permisos por rol'),
    ('REPORTES_GENERAR', 'Generar y exportar reportes');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p WHERE r.nombre = 'ADMIN';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo IN ('ALUMNOS_CONSULTAR', 'DOCENTES_CONSULTAR', 'REPORTES_GENERAR')
WHERE r.nombre = 'DIRECCION';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo IN ('ALUMNOS_CONSULTAR', 'DOCENTES_CONSULTAR')
WHERE r.nombre = 'PROFESOR';
