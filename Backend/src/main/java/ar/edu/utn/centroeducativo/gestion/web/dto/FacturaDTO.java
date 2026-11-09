package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaDTO(
        Long id,
        String numero,
        Long alumnoId,
        String alumno,
        String periodo,
        LocalDate emision,
        LocalDate vencimiento,
        BigDecimal total,
        BigDecimal pagado,
        BigDecimal saldo,
        /** PENDIENTE, PARCIAL o PAGADA. */
        String estado,
        boolean vencida,
        List<Item> items,
        List<PagoResumen> pagos
) {

    public record Item(Long id, String concepto, String descripcion, BigDecimal monto, boolean pagado, Long pagoId) {
    }

    public record PagoResumen(
            Long id,
            String numeroComprobante,
            BigDecimal monto,
            LocalDate fechaPago,
            LocalDateTime registradoEn,
            String nombreArchivo,
            List<Long> itemIds
    ) {
    }
}
