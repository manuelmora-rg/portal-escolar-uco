-- Versión de bloqueo optimista para entidades que la requieren.
-- Se agrega en una migración nueva porque V1 ya fue aplicada.

ALTER TABLE schedule_block    ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE submission        ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE material          ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE attendance_record ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE observation       ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE event             ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE meeting           ADD COLUMN version bigint NOT NULL DEFAULT 0;
