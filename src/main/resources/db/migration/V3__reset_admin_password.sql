-- Fuerza que el usuario 'admin' exista y tenga la contraseña 'password' (hash BCrypt)

INSERT INTO usuario (username, password_hash, nombre_completo, email, activo)
VALUES (
    'admin',
    '$2a$10$7EqJtq98hPqEX7fNZaFWo.5IvC0.7Ay1lZasJ5K8Gac0ewsG3K5e.',
    'Administrador del sistema',
    'admin@example.com',
    TRUE
)
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    nombre_completo = EXCLUDED.nombre_completo,
    email = EXCLUDED.email,
    activo = EXCLUDED.activo;

