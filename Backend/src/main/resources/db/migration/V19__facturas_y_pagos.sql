-- Parte 3: facturación mensual por alumno y pagos informados por los padres.
CREATE TABLE factura (
    id          BIGSERIAL PRIMARY KEY,
    alumno_id   BIGINT NOT NULL REFERENCES alumno (id),
    periodo     VARCHAR(7) NOT NULL CHECK (periodo ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    emision     DATE NOT NULL,
    vencimiento DATE NOT NULL,
    total       NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    UNIQUE (alumno_id, periodo)
);

CREATE TABLE pago (
    id                 BIGSERIAL PRIMARY KEY,
    factura_id         BIGINT NOT NULL REFERENCES factura (id),
    numero_comprobante VARCHAR(20) UNIQUE,
    monto              NUMERIC(12, 2) NOT NULL CHECK (monto > 0),
    fecha_pago         DATE NOT NULL,
    registrado_en      TIMESTAMP NOT NULL DEFAULT NOW(),
    registrado_por     BIGINT REFERENCES usuario (id),
    nombre_archivo     VARCHAR(120) NOT NULL,
    tipo_contenido     VARCHAR(30) NOT NULL
);

-- Un ítem se paga una sola vez: pago_id queda asignado cuando un pago lo cubre.
CREATE TABLE factura_item (
    id          BIGSERIAL PRIMARY KEY,
    factura_id  BIGINT NOT NULL REFERENCES factura (id) ON DELETE CASCADE,
    concepto    VARCHAR(20) NOT NULL CHECK (concepto IN ('CUOTA_BASE', 'DEPORTE', 'TRANSPORTE', 'COMEDOR')),
    descripcion VARCHAR(150) NOT NULL,
    monto       NUMERIC(12, 2) NOT NULL CHECK (monto >= 0),
    pago_id     BIGINT REFERENCES pago (id)
);

-- La imagen o el PDF de la transferencia va aparte para no cargarlo al listar pagos.
CREATE TABLE pago_archivo (
    pago_id   BIGINT PRIMARY KEY REFERENCES pago (id) ON DELETE CASCADE,
    contenido BYTEA NOT NULL
);

CREATE INDEX idx_factura_alumno ON factura (alumno_id);
CREATE INDEX idx_factura_periodo ON factura (periodo);
CREATE INDEX idx_factura_item_factura ON factura_item (factura_id);
CREATE INDEX idx_pago_factura ON pago (factura_id);
CREATE INDEX idx_pago_fecha ON pago (fecha_pago);
