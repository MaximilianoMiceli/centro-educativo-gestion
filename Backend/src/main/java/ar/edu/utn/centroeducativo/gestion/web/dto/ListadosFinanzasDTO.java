package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Respuestas de los listados de finanzas que consulta la administración desde la web. */
public final class ListadosFinanzasDTO {

    private ListadosFinanzasDTO() {
    }

    /** Pagos recibidos entre dos fechas, con el subtotal de cada mes. */
    public record Ingresos(LocalDate desde, LocalDate hasta, BigDecimal total, int cantidad, List<Mes> porMes,
            List<Fila> filas) {

        public record Mes(String mes, BigDecimal total, int cantidad) {
        }

        public record Fila(Long pagoId, String numeroComprobante, LocalDate fechaPago, Long alumnoId, String alumno,
                String legajo, String factura, String periodo, BigDecimal monto) {
        }
    }

    /** Facturas pagadas por completo o con pagos parciales; el año es el del último pago. */
    public record PagosPorFactura(LocalDate desde, LocalDate hasta, BigDecimal totalFacturado, BigDecimal totalPagado,
            BigDecimal totalSaldo, List<Fila> filas) {

        public record Fila(int anio, Long alumnoId, String alumno, String legajo, Long facturaId, String factura,
                String periodo, BigDecimal total, BigDecimal pagado, BigDecimal saldo, LocalDate ultimoPago) {
        }
    }

    /** Ítems de deporte de las facturas, con el grupo actual del alumno y el profesor a cargo. */
    public record PagosPorDeporte(LocalDate desde, LocalDate hasta, BigDecimal totalPagado, BigDecimal totalPendiente,
            List<Fila> filas) {

        /** El estado es PAGADO o PENDIENTE. */
        public record Fila(String deporte, String nivel, String horario, String profesor, Long alumnoId, String alumno,
                String legajo, String curso, String periodo, String factura, BigDecimal monto, String estado,
                LocalDate fechaPago) {
        }
    }

    /** Ítems de transporte de las facturas, por recorrido. */
    public record PagosPorRecorrido(LocalDate desde, LocalDate hasta, BigDecimal totalPagado, BigDecimal totalPendiente,
            List<Fila> filas) {

        /** El estado es PAGADO o PENDIENTE. */
        public record Fila(String recorrido, Long alumnoId, String alumno, String legajo, String nivel, String curso,
                String periodo, String factura, BigDecimal monto, String estado, LocalDate fechaPago) {
        }
    }
}
