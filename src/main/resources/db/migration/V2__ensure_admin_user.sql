-- Asegura que exista el usuario admin con contraseña 'password' (hash BCrypt)

INSERT INTO usuario (username, password_hash, nombre_completo, email, activo)
SELECT 'admin',
       '$2a$10$7EqJtq98hPqEX7fNZaFWo.5IvC0.7Ay1lZasJ5K8Gac0ewsG3K5e.',
       'Administrador del sistema',
       'admin@example.com',
       TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuario WHERE username = 'admin'
);

