ALTER TABLE auth_app.usuarios
ADD COLUMN IF NOT EXISTS email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS token_verificacion_email VARCHAR(255),
ADD COLUMN IF NOT EXISTS fecha_expiracion_verificacion TIMESTAMP,
ADD COLUMN IF NOT EXISTS token_recuperacion_password VARCHAR(255),
ADD COLUMN IF NOT EXISTS fecha_expiracion_password TIMESTAMP;

-- Marcar usuarios existentes como verificados para conservar operatividad inmediata
UPDATE auth_app.usuarios SET email_verificado = TRUE WHERE email_verificado IS FALSE;
