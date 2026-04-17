-- Seed roles, permisos y asignaciones (idempotente)
-- Este script asegura que existan los roles básicos, permisos y sus relaciones.

-- Roles iniciales
INSERT INTO rol (nombre, descripcion, activo) VALUES
    ('ADMIN',   'Administrador del sistema', TRUE),
    ('CAJA',    'Operador de caja',          TRUE),
    ('ACADEMICO', 'Coordinador académico',  TRUE)
ON CONFLICT (nombre) DO NOTHING;

-- Permisos básicos por módulo/acción
INSERT INTO permiso (codigo, descripcion, modulo) VALUES
    ('ESTUDIANTE_VER',        'Ver estudiantes',                          'ESTUDIANTES'),
    ('ESTUDIANTE_CREAR',      'Crear estudiantes',                        'ESTUDIANTES'),
    ('ESTUDIANTE_EDITAR',     'Editar estudiantes',                       'ESTUDIANTES'),
    ('ESTUDIANTE_INACTIVAR',  'Inactivar estudiantes',                    'ESTUDIANTES'),

    ('MAESTRO_VER',           'Ver maestros',                             'MAESTROS'),
    ('MAESTRO_CREAR',         'Crear maestros',                           'MAESTROS'),
    ('MAESTRO_EDITAR',        'Editar maestros',                          'MAESTROS'),
    ('MAESTRO_INACTIVAR',     'Inactivar maestros',                       'MAESTROS'),

    ('CURSO_VER',             'Ver cursos',                               'CURSOS'),
    ('CURSO_CREAR',           'Crear cursos',                             'CURSOS'),
    ('CURSO_EDITAR',          'Editar cursos',                            'CURSOS'),
    ('CURSO_CAMBIAR_ESTADO',  'Cambiar estado de cursos',                 'CURSOS'),

    ('MATRICULA_VER',         'Ver matrículas',                           'MATRICULAS'),
    ('MATRICULA_CREAR',       'Crear matrículas',                         'MATRICULAS'),
    ('MATRICULA_CANCELAR',    'Cancelar matrículas',                      'MATRICULAS'),

    ('PAGO_VER',              'Ver pagos de estudiantes',                 'PAGOS'),
    ('PAGO_REGISTRAR',        'Registrar pagos de estudiantes',           'PAGOS'),
    ('PAGO_ANULAR',           'Anular pagos de estudiantes',              'PAGOS'),

    ('NOMINA_VER',            'Ver nóminas',                              'NOMINA'),
    ('NOMINA_CALCULAR',       'Calcular nómina',                          'NOMINA'),
    ('NOMINA_APROBAR',        'Aprobar nómina',                           'NOMINA'),
    ('NOMINA_PAGAR',          'Pagar nómina',                             'NOMINA'),

    ('USUARIO_ADMINISTRAR',   'Administrar usuarios, roles y permisos',   'SEGURIDAD')
ON CONFLICT (codigo) DO NOTHING;

-- ADMIN: todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON 1=1
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CAJA: enfoque en pagos y consulta básica
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'ESTUDIANTE_VER',
    'CURSO_VER',
    'MATRICULA_VER',
    'PAGO_VER',
    'PAGO_REGISTRAR',
    'PAGO_ANULAR',
    'NOMINA_VER',
    'NOMINA_PAGAR'
)
WHERE r.nombre = 'CAJA'
ON CONFLICT DO NOTHING;

-- ACADEMICO: foco académico (estudiantes, maestros, cursos, matrículas)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'ESTUDIANTE_VER',
    'ESTUDIANTE_CREAR',
    'ESTUDIANTE_EDITAR',
    'ESTUDIANTE_INACTIVAR',
    'MAESTRO_VER',
    'MAESTRO_CREAR',
    'MAESTRO_EDITAR',
    'MAESTRO_INACTIVAR',
    'CURSO_VER',
    'CURSO_CREAR',
    'CURSO_EDITAR',
    'CURSO_CAMBIAR_ESTADO',
    'MATRICULA_VER',
    'MATRICULA_CREAR',
    'MATRICULA_CANCELAR'
)
WHERE r.nombre = 'ACADEMICO'
ON CONFLICT DO NOTHING;

-- Usuario administrador inicial (idempotente)
INSERT INTO usuario (username, password_hash, nombre_completo, email, activo)
SELECT 'admin',
       '$2a$10$7EqJtq98hPqEX7fNZaFWo.5IvC0.7Ay1lZasJ5K8Gac0ewsG3K5e.',
       'Administrador del sistema',
       'admin@example.com',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'admin');

-- Relación usuario admin -> rol ADMIN
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuario u
JOIN rol r ON r.nombre = 'ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

