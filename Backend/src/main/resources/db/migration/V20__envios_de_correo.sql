-- Parte 3: registro de los correos mensuales a los padres (recordatorio de cuotas y aviso de deuda).
-- Mientras no haya un servicio de correo configurado, el sistema arma y guarda el correo con sus adjuntos
-- pero no lo envía (estado SIMULADO).
CREATE TABLE envio_correo (
    id            BIGSERIAL PRIMARY KEY,
    tipo          VARCHAR(20) NOT NULL CHECK (tipo IN ('RECORDATORIO', 'DEUDA')),
    padre_id      BIGINT NOT NULL REFERENCES padre (id),
    -- Mes en el que se envía (AAAA-MM). Con tipo y padre evita mandar dos veces el mismo correo.
    periodo       VARCHAR(7) NOT NULL CHECK (periodo ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    destinatario  VARCHAR(120),
    asunto        VARCHAR(200) NOT NULL,
    cuerpo        TEXT NOT NULL,
    estado        VARCHAR(20) NOT NULL CHECK (estado IN ('SIMULADO', 'ENVIADO', 'ERROR')),
    detalle_error VARCHAR(500),
    creado_en     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (tipo, padre_id, periodo)
);

CREATE TABLE envio_correo_adjunto (
    id        BIGSERIAL PRIMARY KEY,
    envio_id  BIGINT NOT NULL REFERENCES envio_correo (id) ON DELETE CASCADE,
    nombre    VARCHAR(120) NOT NULL,
    contenido BYTEA NOT NULL
);

CREATE INDEX idx_envio_correo_padre ON envio_correo (padre_id);
CREATE INDEX idx_envio_adjunto_envio ON envio_correo_adjunto (envio_id);
