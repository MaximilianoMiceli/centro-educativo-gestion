ALTER TABLE docente
    ADD COLUMN intentos_edicion_fallidos SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN edicion_bloqueada_hasta TIMESTAMP;
