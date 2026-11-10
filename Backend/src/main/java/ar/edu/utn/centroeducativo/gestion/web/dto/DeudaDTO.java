package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Historial de deuda de un alumno, con cada ítem de pago por separado. */
public record DeudaDTO(
        Long alumnoId,
        String alumno,
        BigDecimal totalFacturado,
        BigDecimal totalPagado,
        BigDecimal totalAdeudado,
        List<Item> items
) {

    /** El estado es PAGADO, PENDIENTE o VENCIDO. */
    public record Item(
            Long facturaId,
            String numeroFactura,
            String periodo,
            LocalDate vencimiento,
            String concepto,
            String descripcion,
            BigDecimal monto,
            String estado
    ) {
    }
}
