CREATE TABLE evaluacion (
    id         BIGSERIAL PRIMARY KEY,
    dictado_id BIGINT NOT NULL REFERENCES dictado (id),
    titulo     VARCHAR(100) NOT NULL,
    tipo       VARCHAR(20) NOT NULL CHECK (tipo IN ('EXAMEN', 'TRABAJO_PRACTICO', 'ORAL', 'OTRA')),
    periodo    SMALLINT NOT NULL CHECK (periodo BETWEEN 1 AND 3),
    fecha      DATE NOT NULL,
    UNIQUE (dictado_id, titulo)
);

CREATE TABLE calificacion (
    id            BIGSERIAL PRIMARY KEY,
    evaluacion_id BIGINT NOT NULL REFERENCES evaluacion (id),
    alumno_id     BIGINT NOT NULL REFERENCES alumno (id),
    nota          NUMERIC(4, 2) NOT NULL CHECK (nota BETWEEN 1 AND 10),
    UNIQUE (evaluacion_id, alumno_id)
);

CREATE INDEX idx_calificacion_alumno ON calificacion (alumno_id);

INSERT INTO permiso (codigo, descripcion) VALUES
    ('CALIFICACIONES_CARGAR', 'Crear evaluaciones y cargar calificaciones de las materias a cargo');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'CALIFICACIONES_CARGAR'
WHERE r.nombre IN ('ADMIN', 'PROFESOR');

-- Datos de demostración: tres evaluaciones por materia dictada
INSERT INTO evaluacion (dictado_id, titulo, tipo, periodo, fecha)
SELECT d.id, v.titulo, v.tipo, v.periodo, v.fecha
FROM dictado d
CROSS JOIN (VALUES
    ('Trabajo práctico 1', 'TRABAJO_PRACTICO', 1, DATE '2026-04-20'),
    ('Examen parcial', 'EXAMEN', 1, DATE '2026-05-15'),
    ('Examen final', 'EXAMEN', 2, DATE '2026-08-10')
) AS v(titulo, tipo, periodo, fecha);

-- Notas de demostración entre 4,0 y 10,0; algunos alumnos quedan sin nota a propósito
INSERT INTO calificacion (evaluacion_id, alumno_id, nota)
SELECT e.id, a.id, 4 + 0.5 * MOD(a.id * 7 + e.dictado_id * 3 + e.id * 5, 13)
FROM evaluacion e
JOIN dictado d ON d.id = e.dictado_id
JOIN alumno a ON a.curso_id = d.curso_id
WHERE MOD(a.id + e.id, 11) <> 0;
