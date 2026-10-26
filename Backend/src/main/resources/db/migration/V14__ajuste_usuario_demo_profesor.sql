-- El DNI 87654321 pertenece al docente DOC002, que ya queda vinculado a profesor2 por docente_id.
-- Sin número de documento, el usuario de demo no compite con el alta de profesores (la vinculación se hace por docente_id).
UPDATE usuario SET numero_documento = NULL WHERE username = 'profesor2';
