INSERT INTO permiso (codigo, descripcion) VALUES
    ('DATOS_MAESTROS_ADMINISTRAR', 'Administrar niveles, cursos, materias, asignaciones y horarios');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'DATOS_MAESTROS_ADMINISTRAR'
WHERE r.nombre = 'ADMIN';

CREATE TABLE materia (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE dictado (
    id          BIGSERIAL PRIMARY KEY,
    docente_id  BIGINT NOT NULL REFERENCES docente (id),
    materia_id  BIGINT NOT NULL REFERENCES materia (id),
    curso_id    BIGINT NOT NULL REFERENCES curso (id),
    UNIQUE (materia_id, curso_id)
);

CREATE INDEX idx_dictado_docente ON dictado (docente_id);

CREATE TABLE horario (
    id           BIGSERIAL PRIMARY KEY,
    dictado_id   BIGINT NOT NULL REFERENCES dictado (id),
    dia_semana   SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio  TIME NOT NULL,
    hora_fin     TIME NOT NULL,
    CHECK (hora_inicio < hora_fin)
);

CREATE INDEX idx_horario_dictado ON horario (dictado_id);
