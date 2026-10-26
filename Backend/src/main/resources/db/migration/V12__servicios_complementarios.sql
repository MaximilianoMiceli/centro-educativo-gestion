CREATE TABLE idioma (
    id     SMALLSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO idioma (nombre) VALUES ('Inglés'), ('Portugués'), ('Francés');

CREATE TABLE inscripcion_idioma (
    id                BIGSERIAL PRIMARY KEY,
    alumno_id         BIGINT NOT NULL REFERENCES alumno (id),
    idioma_id         SMALLINT NOT NULL REFERENCES idioma (id),
    fecha_inscripcion DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (alumno_id, idioma_id)
);

CREATE TABLE inscripcion_apoyo (
    alumno_id     BIGINT PRIMARY KEY REFERENCES alumno (id),
    fecha_inicio  DATE NOT NULL DEFAULT CURRENT_DATE,
    observaciones VARCHAR(500)
);

CREATE TABLE atencion_enfermeria (
    id             BIGSERIAL PRIMARY KEY,
    alumno_id      BIGINT NOT NULL REFERENCES alumno (id),
    fecha_hora     TIMESTAMP NOT NULL DEFAULT NOW(),
    motivo         VARCHAR(200) NOT NULL,
    observaciones  VARCHAR(500),
    registrado_por BIGINT REFERENCES usuario (id)
);

CREATE INDEX idx_enfermeria_alumno ON atencion_enfermeria (alumno_id);
