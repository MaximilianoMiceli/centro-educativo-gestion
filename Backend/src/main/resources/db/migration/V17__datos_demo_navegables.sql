-- Datos de demostración para navegar con las cuentas de acceso público.
-- Alumno ALU001 (usuario alumno1 y padre1), ALU002 (padre1) y docente DOC002 (usuario profesor2).

-- Materias del profesor demo en 1°A Secundario, el curso de ALU001
INSERT INTO dictado (docente_id, materia_id, curso_id)
SELECT doc.id, m.id, c.id
FROM docente doc
JOIN materia m ON m.nombre IN ('Historia', 'Geografía')
JOIN alumno a ON a.legajo = 'ALU001'
JOIN curso c ON c.id = a.curso_id
WHERE doc.legajo = 'DOC002';

-- Horarios en lunes, un día que no tiene otras materias en ese curso
INSERT INTO horario (dictado_id, dia_semana, hora_inicio, hora_fin)
SELECT d.id, 1, v.inicio::time, v.fin::time
FROM dictado d
JOIN docente doc ON doc.id = d.docente_id
JOIN materia m ON m.id = d.materia_id
JOIN (VALUES ('Historia', '08:00', '09:30'), ('Geografía', '10:00', '11:30')) AS v(materia, inicio, fin)
  ON v.materia = m.nombre
WHERE doc.legajo = 'DOC002'
  AND d.curso_id = (SELECT a.curso_id FROM alumno a WHERE a.legajo = 'ALU001');

-- Evaluaciones de esas materias, con los mismos títulos que V15
INSERT INTO evaluacion (dictado_id, titulo, tipo, periodo, fecha)
SELECT d.id, v.titulo, v.tipo, v.periodo, v.fecha
FROM dictado d
JOIN docente doc ON doc.id = d.docente_id
JOIN materia m ON m.id = d.materia_id
CROSS JOIN (VALUES
    ('Trabajo práctico 1', 'TRABAJO_PRACTICO', 1, DATE '2026-04-20'),
    ('Examen parcial', 'EXAMEN', 1, DATE '2026-05-15'),
    ('Examen final', 'EXAMEN', 2, DATE '2026-08-10')
) AS v(titulo, tipo, periodo, fecha)
WHERE doc.legajo = 'DOC002'
  AND m.nombre IN ('Historia', 'Geografía')
  AND d.curso_id = (SELECT a.curso_id FROM alumno a WHERE a.legajo = 'ALU001');

-- Notas de todo el curso para esas evaluaciones, entre 4,0 y 10,0
INSERT INTO calificacion (evaluacion_id, alumno_id, nota)
SELECT e.id, a.id, 4 + 0.5 * MOD(a.id * 7 + e.dictado_id * 3 + e.id * 5, 13)
FROM evaluacion e
JOIN dictado d ON d.id = e.dictado_id
JOIN alumno a ON a.curso_id = d.curso_id
JOIN docente doc ON doc.id = d.docente_id
JOIN materia m ON m.id = d.materia_id
WHERE doc.legajo = 'DOC002'
  AND m.nombre IN ('Historia', 'Geografía');

-- Servicios. ALU001 (alumno1) queda con transporte y comedor, y puede inscribirse a deportes desde la app.
-- ALU002 (hijo de padre1) tiene deporte, idioma, transporte y una atención de enfermería.
INSERT INTO inscripcion_transporte (alumno_id, recorrido_id)
SELECT a.id, r.id FROM alumno a, recorrido_transporte r
WHERE a.legajo = 'ALU001' AND r.nombre = 'Recorrido 1 - Norte'
ON CONFLICT (alumno_id) DO NOTHING;

INSERT INTO inscripcion_comedor (alumno_id)
SELECT a.id FROM alumno a
WHERE a.legajo = 'ALU001'
ON CONFLICT (alumno_id) DO NOTHING;

INSERT INTO inscripcion_deporte (alumno_id, grupo_id)
SELECT a.id, g.id
FROM alumno a
JOIN grupo_deporte g ON TRUE
JOIN deporte d ON d.id = g.deporte_id
JOIN nivel_educativo n ON n.id = g.nivel_id
WHERE a.legajo = 'ALU002' AND d.nombre = 'Natación' AND n.nombre = 'SECUNDARIO'
ON CONFLICT (alumno_id, grupo_id) DO NOTHING;

INSERT INTO inscripcion_idioma (alumno_id, idioma_id)
SELECT a.id, i.id FROM alumno a, idioma i
WHERE a.legajo = 'ALU002' AND i.nombre = 'Inglés'
ON CONFLICT (alumno_id, idioma_id) DO NOTHING;

INSERT INTO inscripcion_transporte (alumno_id, recorrido_id)
SELECT a.id, r.id FROM alumno a, recorrido_transporte r
WHERE a.legajo = 'ALU002' AND r.nombre = 'Recorrido 2 - Sur'
ON CONFLICT (alumno_id) DO NOTHING;

INSERT INTO atencion_enfermeria (alumno_id, fecha_hora, motivo, observaciones)
SELECT a.id, TIMESTAMP '2026-09-14 10:30:00', 'Dolor de cabeza', 'Se le dio agua y descanso. Sin novedades.'
FROM alumno a
WHERE a.legajo = 'ALU002';
