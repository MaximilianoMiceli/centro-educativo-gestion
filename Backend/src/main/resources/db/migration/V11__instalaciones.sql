CREATE TABLE instalacion (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    tipo        VARCHAR(30) NOT NULL CHECK (tipo IN ('PILETA', 'CANCHA', 'PISTA', 'GIMNASIO', 'COMEDOR', 'LABORATORIO', 'ENFERMERIA', 'OTRA')),
    ubicacion   VARCHAR(120),
    capacidad   INTEGER CHECK (capacidad IS NULL OR capacidad > 0),
    estado      VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA', 'EN_MANTENIMIENTO', 'INACTIVA')),
    descripcion VARCHAR(500)
);

INSERT INTO instalacion (nombre, tipo, estado) VALUES
    ('Pileta de natación', 'PILETA', 'ACTIVA'),
    ('Cancha de fútbol 1', 'CANCHA', 'ACTIVA'),
    ('Cancha de fútbol 2', 'CANCHA', 'ACTIVA'),
    ('Pista de atletismo', 'PISTA', 'ACTIVA'),
    ('Gimnasio cubierto', 'GIMNASIO', 'ACTIVA'),
    ('Comedor', 'COMEDOR', 'ACTIVA'),
    ('Laboratorio de computación', 'LABORATORIO', 'ACTIVA'),
    ('Laboratorio de física', 'LABORATORIO', 'ACTIVA'),
    ('Laboratorio de química', 'LABORATORIO', 'ACTIVA'),
    ('Enfermería', 'ENFERMERIA', 'ACTIVA');
