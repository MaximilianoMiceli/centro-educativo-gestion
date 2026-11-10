-- Parte 3: cada ítem de deporte o de transporte guarda a qué deporte o recorrido corresponde,
-- para poder listar los pagos por deporte y por recorrido sin interpretar el texto de la descripción.
ALTER TABLE factura_item ADD COLUMN referencia_id BIGINT;

-- Las facturas que ya existían se completan a partir de su descripción.
UPDATE factura_item fi
SET referencia_id = d.id
FROM deporte d
WHERE fi.concepto = 'DEPORTE' AND fi.descripcion = 'Deporte: ' || d.nombre;

UPDATE factura_item fi
SET referencia_id = r.id
FROM recorrido_transporte r
WHERE fi.concepto = 'TRANSPORTE' AND fi.descripcion = 'Transporte: ' || r.nombre;

CREATE INDEX idx_factura_item_concepto ON factura_item (concepto, referencia_id);
