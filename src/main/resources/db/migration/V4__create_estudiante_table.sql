-- V4: Crear tabla estudiante (solo si no existe)
CREATE TABLE IF NOT EXISTS estudiante (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(150),
    fecha_registro DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_baja DATE
);

-- Índice/constraint de unicidad para número de documento
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_estudiante_numero_documento'
    ) THEN
        ALTER TABLE estudiante ADD CONSTRAINT uq_estudiante_numero_documento UNIQUE (numero_documento);
    END IF;
END$$;

