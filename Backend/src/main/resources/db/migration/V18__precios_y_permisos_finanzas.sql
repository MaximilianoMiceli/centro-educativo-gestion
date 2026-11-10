-- Parte 3: valores vigentes de la cuota base, los deportes, los recorridos y el comedor.
CREATE TABLE precio (
    id             BIGSERIAL PRIMARY KEY,
    concepto       VARCHAR(20) NOT NULL CHECK (concepto IN ('CUOTA_BASE', 'DEPORTE', 'TRANSPORTE', 'COMEDOR')),
    deporte_id     BIGINT REFERENCES deporte (id),
    recorrido_id   SMALLINT REFERENCES recorrido_transporte (id),
    monto          NUMERIC(12, 2) NOT NULL CHECK (monto >= 0),
    actualizado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (
        (concepto = 'DEPORTE' AND deporte_id IS NOT NULL AND recorrido_id IS NULL)
        OR (concepto = 'TRANSPORTE' AND recorrido_id IS NOT NULL AND deporte_id IS NULL)
        OR (concepto IN ('CUOTA_BASE', 'COMEDOR') AND deporte_id IS NULL AND recorrido_id IS NULL)
    )
);

CREATE UNIQUE INDEX uq_precio_general ON precio (concepto) WHERE concepto IN ('CUOTA_BASE', 'COMEDOR');
CREATE UNIQUE INDEX uq_precio_deporte ON precio (deporte_id) WHERE deporte_id IS NOT NULL;
CREATE UNIQUE INDEX uq_precio_recorrido ON precio (recorrido_id) WHERE recorrido_id IS NOT NULL;

-- Valores de ejemplo para poder usar el sistema; el administrador los cambia desde la aplicación.
INSERT INTO precio (concepto, monto) VALUES
    ('CUOTA_BASE', 150000.00),
    ('COMEDOR', 45000.00);

INSERT INTO precio (concepto, deporte_id, monto)
SELECT 'DEPORTE', d.id, 15000 + 2500 * ((d.id - 1) % 5)
FROM deporte d;

INSERT INTO precio (concepto, recorrido_id, monto)
SELECT 'TRANSPORTE', r.id, 28000 + 4000 * (r.id - 1)
FROM recorrido_transporte r;

INSERT INTO permiso (codigo, descripcion) VALUES
    ('FINANZAS_ADMINISTRAR', 'Definir los valores de la cuota, deportes, transporte y comedor, y emitir la facturación'),
    ('FINANZAS_CONSULTAR', 'Consultar los listados de ingresos y pagos');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'FINANZAS_ADMINISTRAR'
WHERE r.nombre = 'ADMIN';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'FINANZAS_CONSULTAR'
WHERE r.nombre IN ('ADMIN', 'DIRECCION');
