INSERT INTO permiso (codigo, descripcion) VALUES
    ('MI_INFORMACION_CONSULTAR', 'Consultar la información propia (alumno) o la de los hijos vinculados (padre)');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'MI_INFORMACION_CONSULTAR'
WHERE r.nombre IN ('ADMIN', 'ALUMNO', 'PADRE');

CREATE TABLE padre_alumno (
    padre_id  BIGINT NOT NULL REFERENCES padre (id),
    alumno_id BIGINT NOT NULL REFERENCES alumno (id),
    PRIMARY KEY (padre_id, alumno_id)
);

INSERT INTO padre_alumno (padre_id, alumno_id)
SELECT p.id, a.id FROM padre p, alumno a
WHERE p.dni = '40000097' AND a.legajo IN ('ALU001', 'ALU002');

INSERT INTO usuario (username, email, password_hash, rol_id, activo, creado_en, tipo_documento, numero_documento,
                     padre_id, alumno_id, debe_cambiar_password)
SELECT 'padre1', 'usuario.padre1@centro.local',
       '$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.',
       r.id, TRUE, NOW(), 'DNI', '40000097', p.id, NULL, FALSE
FROM rol r, padre p
WHERE r.nombre = 'PADRE' AND p.dni = '40000097';

INSERT INTO usuario (username, email, password_hash, rol_id, activo, creado_en, tipo_documento, numero_documento,
                     padre_id, alumno_id, debe_cambiar_password)
SELECT 'alumno1', 'usuario.alumno1@centro.local',
       '$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.',
       r.id, TRUE, NOW(), 'DNI', '45678901', NULL, a.id, FALSE
FROM rol r, alumno a
WHERE r.nombre = 'ALUMNO' AND a.legajo = 'ALU001';
