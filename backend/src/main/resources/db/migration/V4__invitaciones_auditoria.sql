-- Invitaciones de cuenta y auditoría general del sistema.
-- Las invitaciones permiten crear cuentas sin registro público libre.
-- El token se guarda únicamente como hash SHA-256; el valor original se muestra una sola vez.

CREATE TABLE invitation (
    id          uuid PRIMARY KEY,
    email       varchar(160) NOT NULL,
    role        varchar(20)  NOT NULL,
    token_hash  varchar(64)  NOT NULL,
    expires_at  timestamptz  NOT NULL,
    used_at     timestamptz,
    revoked_at  timestamptz,
    created_by  uuid REFERENCES app_user (id),
    created_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_invitation_role CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT', 'PARENT'))
);

CREATE UNIQUE INDEX ux_invitation_token ON invitation (token_hash);
CREATE INDEX ix_invitation_email ON invitation (lower(email));
CREATE INDEX ix_invitation_pending ON invitation (email, used_at, revoked_at);
