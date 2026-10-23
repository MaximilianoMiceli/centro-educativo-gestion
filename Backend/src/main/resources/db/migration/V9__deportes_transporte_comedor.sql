CREATE TABLE deporte (
    id                     BIGSERIAL PRIMARY KEY,
    nombre                 VARCHAR(80) NOT NULL UNIQUE,
    docente_responsable_id BIGINT REFERENCES docente (id)
);

CREATE TABLE grupo_deporte (
    id           BIGSERIAL PRIMARY KEY,
    deporte_id   BIGINT NOT NULL REFERENCES deporte (id),
    nivel_id     SMALLINT NOT NULL REFERENCES nivel_educativo (id),
    dia_semana   SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio  TIME NOT NULL,
    hora_fin     TIME NOT NULL,
    CHECK (hora_inicio < hora_fin)
);

CREATE TABLE inscripcion_deporte (
    id         BIGSERIAL PRIMARY KEY,
    alumno_id  BIGINT NOT NULL REFERENCES alumno (id),
    grupo_id   BIGINT NOT NULL REFERENCES grupo_deporte (id),
    UNIQUE (alumno_id, grupo_id)
);

CREATE TABLE recorrido_transporte (
    id      SMALLSERIAL PRIMARY KEY,
    nombre  VARCHAR(40) NOT NULL UNIQUE
);

INSERT INTO recorrido_transporte (nombre) VALUES
    ('Recorrido 1 - Norte'), ('Recorrido 2 - Sur'), ('Recorrido 3 - Este'), ('Recorrido 4 - Oeste');

CREATE TABLE inscripcion_transporte (
    alumno_id    BIGINT PRIMARY KEY REFERENCES alumno (id),
    recorrido_id SMALLINT NOT NULL REFERENCES recorrido_transporte (id)
);

CREATE TABLE inscripcion_comedor (
    alumno_id BIGINT PRIMARY KEY REFERENCES alumno (id)
);

INSERT INTO deporte (nombre, docente_responsable_id) VALUES
    ('Fútbol', (SELECT id FROM docente WHERE legajo = 'DOC006')),
    ('Básquet', (SELECT id FROM docente WHERE legajo = 'DOC006')),
    ('Natación', NULL);

INSERT INTO grupo_deporte (deporte_id, nivel_id, dia_semana, hora_inicio, hora_fin)
SELECT d.id, n.id, v.dia, v.inicio::time, v.fin::time
FROM (VALUES
    ('Fútbol', 'PRIMARIO', 2, '14:00', '15:30'),
    ('Fútbol', 'SECUNDARIO', 4, '14:00', '15:30'),
    ('Básquet', 'PRIMARIO', 3, '14:00', '15:30'),
    ('Básquet', 'SECUNDARIO', 5, '14:00', '15:30'),
    ('Natación', 'PRIMARIO', 1, '09:00', '10:00'),
    ('Natación', 'SECUNDARIO', 1, '10:30', '11:30')
) AS v(deporte, nivel, dia, inicio, fin)
JOIN deporte d ON d.nombre = v.deporte
JOIN nivel_educativo n ON n.nombre = v.nivel;

INSERT INTO inscripcion_deporte (alumno_id, grupo_id)
SELECT a.id, g.id
FROM alumno a
JOIN curso c ON c.id = a.curso_id
JOIN grupo_deporte g ON g.nivel_id = c.nivel_id
JOIN deporte d ON d.id = g.deporte_id AND d.nombre = 'Fútbol'
WHERE a.legajo IN (SELECT 'ALU' || lpad(n::text, 3, '0') FROM generate_series(5, 30) n);

INSERT INTO inscripcion_transporte (alumno_id, recorrido_id)
SELECT a.id, 1 + (a.id % 4)
FROM alumno a
WHERE a.legajo IN (SELECT 'ALU' || lpad(n::text, 3, '0') FROM generate_series(5, 64) n)
  AND a.id % 2 = 0;

INSERT INTO inscripcion_comedor (alumno_id)
SELECT a.id
FROM alumno a
WHERE a.legajo IN (SELECT 'ALU' || lpad(n::text, 3, '0') FROM generate_series(5, 64) n)
  AND a.id % 3 = 0;
