-- Academia de Belleza - Script de inicialización de base de datos (PostgreSQL)
-- Crea el esquema principal y algunos datos semilla para desarrollo

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- =========================================================
-- 1. TABLAS DE SEGURIDAD (USUARIOS, ROLES, PERMISOS)
-- =========================================================

CREATE TABLE usuario (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    nombre_completo  VARCHAR(100) NOT NULL,
    email            VARCHAR(100),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos INT         NOT NULL DEFAULT 0,
    ultimo_acceso    TIMESTAMP
);

CREATE TABLE rol (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE permiso (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    modulo      VARCHAR(50)
);

CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_usuario_rol_rol     FOREIGN KEY (rol_id)     REFERENCES rol (id)
);

CREATE TABLE rol_permiso (
    rol_id     BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permiso_rol     FOREIGN KEY (rol_id)     REFERENCES rol (id),
    CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES permiso (id)
);

-- =========================================================
-- 2. TABLAS DE PERSONAS (ESTUDIANTES, MAESTROS)
-- =========================================================

CREATE TABLE estudiante (
    id               BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(50)  NOT NULL,
    apellido         VARCHAR(50)  NOT NULL,
    tipo_documento   VARCHAR(20)  NOT NULL,
    numero_documento VARCHAR(30)  NOT NULL UNIQUE,
    telefono         VARCHAR(20),
    email            VARCHAR(100),
    direccion        VARCHAR(150),
    fecha_registro   DATE         NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_baja       DATE
);

CREATE TABLE maestro (
    id                   BIGSERIAL PRIMARY KEY,
    nombre               VARCHAR(50)  NOT NULL,
    apellido             VARCHAR(50)  NOT NULL,
    tipo_documento       VARCHAR(20)  NOT NULL,
    numero_documento     VARCHAR(30)  NOT NULL UNIQUE,
    telefono             VARCHAR(20),
    email                VARCHAR(100),
    direccion            VARCHAR(150),
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    tipo_pago_profesor   VARCHAR(20)  NOT NULL,
    tarifa_hora          NUMERIC(12,2),
    salario_mensual      NUMERIC(12,2),
    tarifa_por_curso     NUMERIC(12,2),
    porcentaje_por_curso NUMERIC(5,2),
    CONSTRAINT chk_maestro_porcentaje CHECK (
        porcentaje_por_curso IS NULL OR (porcentaje_por_curso >= 0 AND porcentaje_por_curso <= 100)
    )
);

-- =========================================================
-- 3. PERIODOS Y MOVIMIENTOS FINANCIEROS
-- =========================================================

CREATE TABLE periodo_nomina (
    id           BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL,
    fecha_inicio DATE        NOT NULL,
    fecha_fin    DATE        NOT NULL,
    estado       VARCHAR(20) NOT NULL,
    CONSTRAINT chk_periodo_nomina_fechas CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE movimiento_financiero (
    id        BIGSERIAL PRIMARY KEY,
    fecha     TIMESTAMP    NOT NULL,
    tipo      VARCHAR(20)  NOT NULL, -- INGRESO, EGRESO, AJUSTE
    monto     NUMERIC(12,2) NOT NULL,
    concepto  VARCHAR(200) NOT NULL,
    origen    VARCHAR(20)  NOT NULL, -- PAGO_ESTUDIANTE, NOMINA, OTRO
    id_origen BIGINT,
    estado    VARCHAR(20)  NOT NULL, -- VIGENTE, ANULADO
    CONSTRAINT chk_movimiento_monto_pos CHECK (monto > 0)
);

-- =========================================================
-- 4. TABLAS ACADÉMICAS (CURSOS, MATRÍCULAS)
-- =========================================================

CREATE TABLE curso (
    id           BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  TEXT,
    precio_base  NUMERIC(12,2) NOT NULL,
    cupo_maximo  INT          NOT NULL,
    estado       VARCHAR(20)  NOT NULL,
    fecha_inicio DATE         NOT NULL,
    fecha_fin    DATE         NOT NULL,
    maestro_id   BIGINT,
    CONSTRAINT fk_curso_maestro FOREIGN KEY (maestro_id) REFERENCES maestro (id),
    CONSTRAINT chk_curso_valores CHECK (precio_base > 0 AND cupo_maximo > 0),
    CONSTRAINT chk_curso_fechas  CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE matricula (
    id            BIGSERIAL PRIMARY KEY,
    fecha         DATE         NOT NULL,
    estado        VARCHAR(20)  NOT NULL,
    valor_base    NUMERIC(12,2) NOT NULL,
    descuento     NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor_final   NUMERIC(12,2) NOT NULL,
    observaciones TEXT,
    estudiante_id BIGINT       NOT NULL,
    curso_id      BIGINT       NOT NULL,
    CONSTRAINT fk_matricula_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiante (id),
    CONSTRAINT fk_matricula_curso      FOREIGN KEY (curso_id)      REFERENCES curso (id),
    CONSTRAINT uq_matricula_estudiante_curso UNIQUE (estudiante_id, curso_id),
    CONSTRAINT chk_matricula_montos CHECK (
        valor_base >= 0 AND descuento >= 0 AND valor_final >= 0
    )
);

-- =========================================================
-- 5. PAGOS DE ESTUDIANTES Y NÓMINA DE PROFESORES
-- =========================================================

CREATE TABLE pago_estudiante (
    id           BIGSERIAL PRIMARY KEY,
    fecha        TIMESTAMP    NOT NULL,
    monto        NUMERIC(12,2) NOT NULL,
    metodo_pago  VARCHAR(20)  NOT NULL,
    estado       VARCHAR(20)  NOT NULL, -- VIGENTE, ANULADO
    referencia   VARCHAR(50),
    observaciones TEXT,
    matricula_id BIGINT       NOT NULL,
    CONSTRAINT fk_pago_matricula FOREIGN KEY (matricula_id) REFERENCES matricula (id),
    CONSTRAINT chk_pago_monto_pos CHECK (monto > 0)
);

CREATE TABLE nomina_profesor (
    id                     BIGSERIAL PRIMARY KEY,
    periodo_nomina_id      BIGINT       NOT NULL,
    maestro_id             BIGINT       NOT NULL,
    estado                 VARCHAR(20)  NOT NULL, -- BORRADOR, APROBADA, PAGADA, ANULADA
    monto_total            NUMERIC(12,2) NOT NULL,
    fecha_calculo          TIMESTAMP,
    fecha_aprobacion       TIMESTAMP,
    fecha_pago             TIMESTAMP,
    movimiento_financiero_id BIGINT,
    CONSTRAINT fk_nomina_periodo FOREIGN KEY (periodo_nomina_id)      REFERENCES periodo_nomina (id),
    CONSTRAINT fk_nomina_maestro FOREIGN KEY (maestro_id)             REFERENCES maestro (id),
    CONSTRAINT fk_nomina_movfin FOREIGN KEY (movimiento_financiero_id) REFERENCES movimiento_financiero (id),
    CONSTRAINT chk_nomina_monto_pos CHECK (monto_total >= 0)
);

CREATE TABLE detalle_nomina_profesor (
    id                   BIGSERIAL PRIMARY KEY,
    nomina_profesor_id   BIGINT       NOT NULL,
    tipo_concepto        VARCHAR(20)  NOT NULL,
    descripcion          VARCHAR(200),
    cantidad             NUMERIC(12,2) NOT NULL,
    tarifa               NUMERIC(12,2) NOT NULL,
    monto                NUMERIC(12,2) NOT NULL,
    referencia_externa   BIGINT,
    CONSTRAINT fk_detalle_nomina FOREIGN KEY (nomina_profesor_id) REFERENCES nomina_profesor (id),
    CONSTRAINT chk_detalle_nomina_valores CHECK (
        cantidad >= 0 AND tarifa >= 0
    )
);

-- =========================================================
-- 6. AUDITORÍA
-- =========================================================

CREATE TABLE auditoria (
    id             BIGSERIAL PRIMARY KEY,
    fecha          TIMESTAMP   NOT NULL,
    usuario_id     BIGINT,
    accion         VARCHAR(50) NOT NULL,
    entidad        VARCHAR(50) NOT NULL,
    id_entidad     BIGINT,
    detalle_antes  TEXT,
    detalle_despues TEXT,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- =========================================================
-- 7. ÍNDICES ADICIONALES
-- =========================================================

CREATE INDEX idx_curso_estado_fecha_inicio
    ON curso (estado, fecha_inicio);

CREATE INDEX idx_pago_estudiante_matricula_estado
    ON pago_estudiante (matricula_id, estado);

CREATE INDEX idx_movimiento_financiero_fecha
    ON movimiento_financiero (fecha);

CREATE INDEX idx_movimiento_financiero_origen_idorigen
    ON movimiento_financiero (origen, id_origen);

CREATE INDEX idx_auditoria_entidad_identidad
    ON auditoria (entidad, id_entidad);

-- =========================================================
-- 8. DATOS SEMILLA BÁSICOS (ROLES, PERMISOS, USUARIO ADMIN)
-- =========================================================

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

-- Asignación de permisos a roles
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

-- Usuario administrador inicial
-- password_hash corresponde a la contraseña "password" con BCrypt (ejemplo típico).
-- Se recomienda cambiarla en cuanto se implemente PasswordEncoder.
INSERT INTO usuario (username, password_hash, nombre_completo, email, activo)
VALUES (
    'admin',
    '$2a$10$7EqJtq98hPqEX7fNZaFWo.5IvC0.7Ay1lZasJ5K8Gac0ewsG3K5e.',
    'Administrador del sistema',
    'admin@example.com',
    TRUE
)
ON CONFLICT (username) DO NOTHING;

-- Relación usuario admin -> rol ADMIN
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuario u
JOIN rol r ON r.nombre = 'ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

-- =========================================================
-- 9. DATOS DE PRUEBA BÁSICOS (ESTUDIANTES, MAESTROS, CURSOS, MATRÍCULAS, PAGOS)
--    Pensados para pruebas locales; ajustar o eliminar en producción.
-- =========================================================

-- Estudiantes de prueba
INSERT INTO estudiante (nombre, apellido, tipo_documento, numero_documento, telefono, email, direccion, fecha_registro, activo)
VALUES
    ('Juan', 'Pérez',  'CC', '100000001', '3000000001', 'juan.perez@example.com',  'Calle 1 #10-01', CURRENT_DATE, TRUE),
    ('Ana',  'Gómez',  'CC', '100000002', '3000000002', 'ana.gomez@example.com',   'Calle 2 #20-02', CURRENT_DATE, TRUE)
ON CONFLICT (numero_documento) DO NOTHING;

-- Maestros de prueba
INSERT INTO maestro (nombre, apellido, tipo_documento, numero_documento, telefono, email, direccion, activo, tipo_pago_profesor,
                     tarifa_hora, salario_mensual, tarifa_por_curso, porcentaje_por_curso)
VALUES
    ('Carlos', 'Ruiz',  'CC', '200000001', '3100000001', 'carlos.ruiz@example.com',  'Carrera 5 #50-01', TRUE,
     'FIJO_MENSUAL', NULL, 2000000.00, NULL, NULL),
    ('Laura',  'Díaz',  'CC', '200000002', '3100000002', 'laura.diaz@example.com',   'Carrera 6 #60-02', TRUE,
     'POR_CURSO', NULL, NULL, 500000.00, NULL)
ON CONFLICT (numero_documento) DO NOTHING;

-- Cursos de prueba (usando maestros anteriores)
INSERT INTO curso (nombre, descripcion, precio_base, cupo_maximo, estado, fecha_inicio, fecha_fin, maestro_id)
VALUES
    ('Curso de Corte Básico', 'Introducción al corte de cabello.', 800000.00, 15, 'ABIERTO', CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days',
        (SELECT id FROM maestro WHERE numero_documento = '200000001')),
    ('Curso de Maquillaje Profesional', 'Técnicas básicas de maquillaje.', 900000.00, 12, 'PLANIFICADO', CURRENT_DATE + INTERVAL '15 days', CURRENT_DATE + INTERVAL '45 days',
        (SELECT id FROM maestro WHERE numero_documento = '200000002'))
ON CONFLICT DO NOTHING;

-- Matrícula de prueba: Juan inscrito en Curso de Corte Básico
INSERT INTO matricula (fecha, estado, valor_base, descuento, valor_final, observaciones, estudiante_id, curso_id)
VALUES (
    CURRENT_DATE,
    'ACTIVA',
    800000.00,
    0.00,
    800000.00,
    'Matrícula de prueba para Juan en Corte Básico',
    (SELECT id FROM estudiante WHERE numero_documento = '100000001'),
    (SELECT id FROM curso WHERE nombre = 'Curso de Corte Básico')
)
ON CONFLICT (estudiante_id, curso_id) DO NOTHING;

-- Pago de prueba sobre la matrícula anterior
INSERT INTO pago_estudiante (fecha, monto, metodo_pago, estado, referencia, observaciones, matricula_id)
VALUES (
    NOW(),
    400000.00,
    'EFECTIVO',
    'VIGENTE',
    'PAGO-PRUEBA-001',
    'Pago inicial de prueba',
    (SELECT id FROM matricula
     WHERE estudiante_id = (SELECT id FROM estudiante WHERE numero_documento = '100000001')
       AND curso_id      = (SELECT id FROM curso WHERE nombre = 'Curso de Corte Básico')
    )
)
ON CONFLICT DO NOTHING;

