-- Añade permiso ESTUDIANTE_LISTAR y lo asigna a roles ADMIN y ACADEMICO
-- Idempotente: no falla si ya existe

INSERT INTO permiso (codigo, descripcion, modulo) VALUES
    ('ESTUDIANTE_LISTAR', 'Listar estudiantes (compatibilidad)', 'ESTUDIANTES')
ON CONFLICT (codigo) DO NOTHING;

-- Asignar el permiso ESTUDIANTE_LISTAR al rol ADMIN
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'ESTUDIANTE_LISTAR'
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Asignar el permiso ESTUDIANTE_LISTAR al rol ACADEMICO (si existe)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'ESTUDIANTE_LISTAR'
WHERE r.nombre = 'ACADEMICO'
ON CONFLICT DO NOTHING;

-- Nota: el seed V5 ya inserta ESTUDIANTE_VER; esta migración añade un permiso adicional
-- para compatibilidad con código que pueda pedir ESTUDIANTE_LISTAR.

