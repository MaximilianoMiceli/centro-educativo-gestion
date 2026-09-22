-- Usuarios de prueba (contraseñas hasheadas con bcrypt, hash de 'password')
-- usuario: admin / password: password
-- usuario: profesor / password: password
INSERT INTO usuario (username, email, password_hash, rol_id, activo, creado_en) VALUES
    ('admin', 'admin@centro.local', '$2a$12$GKhYm2n1qIzxLCPtqx2TN.Lm5TbwcO1X6vQqxD2BZX1H2mN7O6WGm', 1, true, NOW()),
    ('profesor1', 'prof1@centro.local', '$2a$12$GKhYm2n1qIzxLCPtqx2TN.Lm5TbwcO1X6vQqxD2BZX1H2mN7O6WGm', 3, true, NOW());

-- Docentes de prueba
INSERT INTO docente (legajo, dni, nombre, apellido, especialidad, email, telefono, estado) VALUES
    ('DOC001', '12345678', 'Juan', 'Rodríguez', 'Matemática', 'juan@centro.local', '3624123456', 'ACTIVO'),
    ('DOC002', '87654321', 'María', 'López', 'Lengua', 'maria@centro.local', '3624654321', 'ACTIVO');

-- Relación usuario-docente para profesor1
UPDATE usuario SET docente_id = 1 WHERE username = 'profesor1';

-- Cursos de prueba
INSERT INTO curso (nivel_id, anio, seccion) VALUES
    (3, 1, 'A'),
    (3, 1, 'B'),
    (3, 2, 'A');

-- Relación docente-curso
INSERT INTO docente_curso (docente_id, curso_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3);

-- Alumnos de prueba
INSERT INTO alumno (legajo, dni, nombre, apellido, fecha_nacimiento, domicilio, telefono, email, curso_id, estado) VALUES
    ('ALU001', '45678901', 'Carlos', 'García', '2007-05-15', 'Calle 1 123', '3624111111', 'carlos@estudiante.local', 1, 'ACTIVO'),
    ('ALU002', '56789012', 'Ana', 'Martínez', '2007-08-22', 'Calle 2 456', '3624222222', 'ana@estudiante.local', 1, 'ACTIVO'),
    ('ALU003', '67890123', 'Luis', 'Fernández', '2008-01-10', 'Calle 3 789', '3624333333', 'luis@estudiante.local', 2, 'ACTIVO'),
    ('ALU004', '78901234', 'Paula', 'González', '2008-03-18', 'Calle 4 000', '3624444444', 'paula@estudiante.local', 3, 'ACTIVO');
