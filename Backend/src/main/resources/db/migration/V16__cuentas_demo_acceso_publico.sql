-- Cuentas de demostración para compartir el acceso público.
-- Todas usan la contraseña 'password' (hash bcrypt ya verificado en V3) y no obligan a cambiarla al ingresar.
UPDATE usuario
SET password_hash = '$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.',
    activo = TRUE,
    debe_cambiar_password = FALSE
WHERE username IN ('admin', 'direccion1', 'profesor2', 'padre1', 'alumno1');
