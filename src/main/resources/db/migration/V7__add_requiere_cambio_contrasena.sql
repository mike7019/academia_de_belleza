-- Agrega columna para forzar cambio de contraseña tras recuperación
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS requiere_cambio_contrasena BOOLEAN NOT NULL DEFAULT FALSE;

