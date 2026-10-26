-- Docentes responsables de los deportes que todavía no tenían (RN-005)
INSERT INTO docente (legajo, dni, nombre, apellido, especialidad, email, telefono, estado) VALUES
    ('DOC013', '27111222', 'Lucía', 'Cabrera', 'Educación Física', 'lucia.cabrera@centro.local', '3624100013', 'ACTIVO'),
    ('DOC014', '27222333', 'Martín', 'Peralta', 'Educación Física', 'martin.peralta@centro.local', '3624100014', 'ACTIVO'),
    ('DOC015', '27333444', 'Florencia', 'Acosta', 'Artes', 'florencia.acosta@centro.local', '3624100015', 'ACTIVO'),
    ('DOC016', '27444555', 'Esteban', 'Benítez', 'Matemática', 'esteban.benitez@centro.local', '3624100016', 'ACTIVO');

-- Natación ya existía sin responsable: se le asigna ahora
UPDATE deporte
SET docente_responsable_id = (SELECT id FROM docente WHERE legajo = 'DOC013')
WHERE nombre = 'Natación';

-- Los cinco deportes que faltaban del listado institucional (RD-005)
INSERT INTO deporte (nombre, docente_responsable_id) VALUES
    ('Atletismo', (SELECT id FROM docente WHERE legajo = 'DOC014')),
    ('Vóleibol', (SELECT id FROM docente WHERE legajo = 'DOC013')),
    ('Artes Marciales', (SELECT id FROM docente WHERE legajo = 'DOC014')),
    ('Danza', (SELECT id FROM docente WHERE legajo = 'DOC015')),
    ('Ajedrez', (SELECT id FROM docente WHERE legajo = 'DOC016'));

-- Grupos de los deportes nuevos. Cada responsable tiene horarios sin superposición.
INSERT INTO grupo_deporte (deporte_id, nivel_id, dia_semana, hora_inicio, hora_fin)
SELECT d.id, n.id, v.dia, v.inicio::time, v.fin::time
FROM (VALUES
    ('Atletismo', 'PRIMARIO', 2, '16:00', '17:30'),
    ('Atletismo', 'SECUNDARIO', 4, '16:00', '17:30'),
    ('Vóleibol', 'PRIMARIO', 3, '16:00', '17:30'),
    ('Vóleibol', 'SECUNDARIO', 4, '18:00', '19:30'),
    ('Artes Marciales', 'PRIMARIO', 5, '16:00', '17:30'),
    ('Artes Marciales', 'SECUNDARIO', 5, '18:00', '19:30'),
    ('Danza', 'PRIMARIO', 1, '16:00', '17:30'),
    ('Danza', 'SECUNDARIO', 3, '18:00', '19:30'),
    ('Ajedrez', 'PRIMARIO', 2, '18:00', '19:30'),
    ('Ajedrez', 'SECUNDARIO', 1, '18:00', '19:30')
) AS v(deporte, nivel, dia, inicio, fin)
JOIN deporte d ON d.nombre = v.deporte
JOIN nivel_educativo n ON n.nombre = v.nivel;

-- Usuarios de demostración, uno por rol. Contraseña: password (mismo hash que el resto de las cuentas de prueba).
INSERT INTO usuario (username, email, password_hash, rol_id, activo, creado_en, tipo_documento, numero_documento,
                     docente_id, debe_cambiar_password)
SELECT 'direccion1', 'direccion1@centro.local',
       '$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.',
       r.id, TRUE, NOW(), 'DNI', '20111222', NULL, FALSE
FROM rol r
WHERE r.nombre = 'DIRECCION';

INSERT INTO usuario (username, email, password_hash, rol_id, activo, creado_en, tipo_documento, numero_documento,
                     docente_id, debe_cambiar_password)
SELECT 'profesor2', 'profesor2@centro.local',
       '$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.',
       r.id, TRUE, NOW(), 'DNI', '87654321', d.id, FALSE
FROM rol r, docente d
WHERE r.nombre = 'PROFESOR' AND d.legajo = 'DOC002';
