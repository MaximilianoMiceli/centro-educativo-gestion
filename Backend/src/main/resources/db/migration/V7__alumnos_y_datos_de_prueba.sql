INSERT INTO permiso (codigo, descripcion) VALUES
    ('ALUMNOS_ADMINISTRAR', 'Dar de alta y modificar alumnos');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'ALUMNOS_ADMINISTRAR'
WHERE r.nombre = 'ADMIN';

INSERT INTO curso (nivel_id, anio, seccion) VALUES
    (1, 1, 'A'), (1, 2, 'A'),
    (2, 1, 'A'), (2, 2, 'A'), (2, 3, 'A'), (2, 4, 'A'), (2, 5, 'A'), (2, 6, 'A'),
    (3, 3, 'A'), (3, 4, 'A'), (3, 5, 'A')
ON CONFLICT (nivel_id, anio, seccion) DO NOTHING;

INSERT INTO docente (legajo, dni, nombre, apellido, especialidad, email, telefono, estado) VALUES
    ('DOC003', '22333444', 'Laura', 'Gómez', 'Lengua', 'laura.gomez@centro.local', '3624100003', 'ACTIVO'),
    ('DOC004', '23444555', 'Martín', 'Sosa', 'Ciencias Sociales', 'martin.sosa@centro.local', '3624100004', 'ACTIVO'),
    ('DOC005', '24555666', 'Carolina', 'Vera', 'Inglés', 'carolina.vera@centro.local', '3624100005', 'ACTIVO'),
    ('DOC006', '25666777', 'Diego', 'Romero', 'Educación Física', 'diego.romero@centro.local', '3624100006', 'ACTIVO'),
    ('DOC007', '26777888', 'Valeria', 'Ponce', 'Ciencias Naturales', 'valeria.ponce@centro.local', '3624100007', 'ACTIVO'),
    ('DOC008', '27888999', 'Sergio', 'Luna', 'Matemática', 'sergio.luna@centro.local', '3624100008', 'LICENCIA'),
    ('DOC009', '28999000', 'Natalia', 'Ruiz', 'Artes', 'natalia.ruiz@centro.local', '3624100009', 'ACTIVO'),
    ('DOC010', '29000111', 'Hernán', 'Ibarra', 'Matemática', 'hernan.ibarra@centro.local', '3624100010', 'ACTIVO'),
    ('DOC011', '29111222', 'Gabriela', 'Molina', 'Lengua', 'gabriela.molina@centro.local', '3624100011', 'ACTIVO'),
    ('DOC012', '29222333', 'Pablo', 'Herrera', 'Ciencias Naturales', 'pablo.herrera@centro.local', '3624100012', 'ACTIVO')
ON CONFLICT (legajo) DO NOTHING;

INSERT INTO materia (nombre) VALUES
    ('Matemática'), ('Lengua'), ('Ciencias Sociales'), ('Ciencias Naturales'), ('Inglés'),
    ('Educación Física'), ('Artes'), ('Historia'), ('Geografía'), ('Biología')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO dictado (docente_id, materia_id, curso_id)
SELECT d.id, m.id, c.id
FROM (VALUES
    ('DOC010', 'Matemática',         3, 'SECUNDARIO', 1, 'A'),
    ('DOC003', 'Lengua',             3, 'SECUNDARIO', 1, 'A'),
    ('DOC004', 'Ciencias Sociales',  3, 'SECUNDARIO', 1, 'A'),
    ('DOC007', 'Ciencias Naturales', 3, 'SECUNDARIO', 1, 'A'),
    ('DOC005', 'Inglés',             3, 'SECUNDARIO', 1, 'A'),
    ('DOC006', 'Educación Física',   3, 'SECUNDARIO', 1, 'A'),
    ('DOC009', 'Artes',              3, 'SECUNDARIO', 1, 'A'),
    ('DOC011', 'Lengua',             3, 'SECUNDARIO', 2, 'A'),
    ('DOC010', 'Matemática',         3, 'SECUNDARIO', 2, 'A'),
    ('DOC004', 'Historia',           3, 'SECUNDARIO', 2, 'A'),
    ('DOC012', 'Biología',           3, 'SECUNDARIO', 2, 'A'),
    ('DOC005', 'Inglés',             3, 'SECUNDARIO', 2, 'A'),
    ('DOC006', 'Educación Física',   2, 'PRIMARIO',   1, 'A'),
    ('DOC003', 'Lengua',             2, 'PRIMARIO',   1, 'A'),
    ('DOC010', 'Matemática',         2, 'PRIMARIO',   1, 'A'),
    ('DOC009', 'Artes',              2, 'PRIMARIO',   3, 'A'),
    ('DOC007', 'Ciencias Naturales', 2, 'PRIMARIO',   3, 'A'),
    ('DOC004', 'Geografía',          2, 'PRIMARIO',   3, 'A')
) AS v(dni_legajo, materia, anio, nivel, anio_curso, seccion)
JOIN docente d ON d.legajo = v.dni_legajo
JOIN materia m ON m.nombre = v.materia
JOIN nivel_educativo n ON n.nombre = v.nivel
JOIN curso c ON c.nivel_id = n.id AND c.anio = v.anio_curso AND c.seccion = v.seccion
ON CONFLICT (materia_id, curso_id) DO NOTHING;

-- Cada asignación sin horario recibe un bloque único (día y hora de inicio),
-- así no hay solapamientos entre docentes ni entre cursos.
WITH sin_horario AS (
    SELECT d.id, (row_number() OVER (ORDER BY d.id) - 1) AS idx
    FROM dictado d
    WHERE NOT EXISTS (SELECT 1 FROM horario h WHERE h.dictado_id = d.id)
)
INSERT INTO horario (dictado_id, dia_semana, hora_inicio, hora_fin)
SELECT id,
       (((idx / 8) % 5) + 1)::smallint,
       make_time((8 + 2 * (idx % 8))::int, 0, 0),
       make_time((9 + 2 * (idx % 8))::int, 30, 0)
FROM sin_horario;

INSERT INTO padre (dni, nombre, apellido, email, telefono)
SELECT (40000000 + n * 97)::text,
       (ARRAY['Jorge', 'Mariana', 'Ricardo', 'Silvia', 'Oscar', 'Patricia', 'Javier', 'Claudia', 'Rubén', 'Laura'])[1 + n % 10],
       (ARRAY['Fernández', 'Acosta', 'Benítez', 'Castro', 'Domínguez', 'Escobar', 'Flores', 'Giménez', 'Heredia', 'Juárez'])[1 + (n * 3) % 10],
       'padre' || n || '@familia.local',
       '3624' || lpad((500000 + n)::text, 6, '0')
FROM generate_series(1, 20) AS n;

INSERT INTO alumno (legajo, dni, nombre, apellido, fecha_nacimiento, domicilio, telefono, email, curso_id, estado)
SELECT 'ALU' || lpad((n + 4)::text, 3, '0'),
       (30100000 + n * 71)::text,
       (ARRAY['Sofía', 'Lucas', 'Valentina', 'Mateo', 'Julieta', 'Tomás', 'Camila', 'Santiago', 'Martina', 'Bautista', 'Agustina', 'Joaquín'])[1 + n % 12],
       (ARRAY['Pérez', 'Gómez', 'Díaz', 'Torres', 'Álvarez', 'Ramírez', 'Suárez', 'Ortiz', 'Navarro', 'Medina', 'Ibáñez', 'Correa'])[1 + (n * 5) % 12],
       DATE '2026-03-01' - (INTERVAL '1 year' * (4 + n % 14)) - (n % 300) * INTERVAL '1 day',
       'Calle ' || (10 + n) || ' ' || (100 + n * 7),
       '3624' || lpad((400000 + n)::text, 6, '0'),
       'alumno' || n || '@estudiante.local',
       (SELECT c.id FROM curso c ORDER BY c.nivel_id, c.anio, c.seccion
          LIMIT 1 OFFSET (n % (SELECT count(*) FROM curso))),
       'ACTIVO'
FROM generate_series(1, 60) AS n;
