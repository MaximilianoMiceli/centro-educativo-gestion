-- El hash de V2 no corresponde a 'password'. Se reemplaza por un bcrypt válido de 'password'.
UPDATE usuario
SET password_hash = '$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.'
WHERE username IN ('admin', 'profesor1');
