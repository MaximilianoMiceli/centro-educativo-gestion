package ar.edu.utn.centroeducativo.gestion.correo;

import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.domain.FacturaItem;
import ar.edu.utn.centroeducativo.gestion.domain.Padre;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/** Redacta el asunto y el cuerpo de cada tipo de correo. */
public final class TextoCorreos {

    private static final Locale AR = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TextoCorreos() {
    }

    public static String moneda(BigDecimal monto) {
        return NumberFormat.getCurrencyInstance(AR).format(monto);
    }

    /** De AAAA-MM a "Diciembre 2026". */
    public static String mes(String periodo) {
        YearMonth ym = YearMonth.parse(periodo);
        String nombre = ym.getMonth().getDisplayName(TextStyle.FULL, AR);
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1) + " " + ym.getYear();
    }

    public static BigDecimal total(List<Factura> facturas, Predicate<FacturaItem> incluir) {
        return facturas.stream().flatMap(f -> f.getItems().stream()).filter(incluir)
                .map(FacturaItem::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static String asuntoRecordatorio(String periodo, BigDecimal total) {
        return "Cuotas de " + mes(periodo) + ": total a abonar " + moneda(total);
    }

    public static String cuerpoRecordatorio(Padre padre, String periodo, List<Factura> facturas) {
        StringBuilder texto = new StringBuilder();
        texto.append("Hola ").append(padre.getNombre()).append(":\n\n");
        texto.append("Te recordamos el total a abonar por las cuotas de ").append(mes(periodo))
                .append(". Vencen el ").append(FECHA.format(facturas.get(0).getVencimiento())).append(".\n");
        for (Factura factura : facturas) {
            texto.append('\n').append(factura.getAlumno().getApellido()).append(", ")
                    .append(factura.getAlumno().getNombre())
                    .append(" (legajo ").append(factura.getAlumno().getLegajo()).append("):\n");
            factura.getItems().forEach(i -> texto.append("  - ").append(i.getDescripcion()).append(": ")
                    .append(moneda(i.getMonto())).append('\n'));
            texto.append("  Subtotal: ").append(moneda(factura.getTotal())).append('\n');
        }
        texto.append("\nTotal a abonar: ").append(moneda(total(facturas, i -> true))).append("\n\n");
        texto.append(PIE_PAGO).append("\n\nAdjuntamos la factura de cada alumno.\n\n").append(FIRMA);
        return texto.toString();
    }

    public static String asuntoDeuda(BigDecimal total) {
        return "Aviso de deuda: saldo pendiente " + moneda(total);
    }

    /** Solo cuenta lo que falta pagar de cada factura. */
    public static String cuerpoDeuda(Padre padre, List<Factura> facturas) {
        StringBuilder texto = new StringBuilder();
        texto.append("Hola ").append(padre.getNombre()).append(":\n\n");
        texto.append("Según nuestros registros, tenés cuotas pendientes de pago:\n");
        for (Factura factura : facturas) {
            texto.append('\n').append(factura.getAlumno().getApellido()).append(", ")
                    .append(factura.getAlumno().getNombre()).append(" - ").append(mes(factura.getPeriodo()))
                    .append(" (vencida el ").append(FECHA.format(factura.getVencimiento())).append("):\n");
            factura.getItems().stream().filter(i -> i.getPago() == null)
                    .forEach(i -> texto.append("  - ").append(i.getDescripcion()).append(": ")
                            .append(moneda(i.getMonto())).append('\n'));
            texto.append("  Subtotal adeudado: ").append(moneda(total(List.of(factura), i -> i.getPago() == null)))
                    .append('\n');
        }
        texto.append("\nTotal adeudado: ").append(moneda(total(facturas, i -> i.getPago() == null))).append("\n\n");
        texto.append("Si ya hiciste la transferencia, cargá el comprobante desde la aplicación para que quede registrado.\n\n");
        texto.append(PIE_PAGO).append("\n\n").append(FIRMA);
        return texto.toString();
    }

    private static final String PIE_PAGO = "Los pagos se hacen únicamente por transferencia bancaria. Después de transferir, "
            + "cargá el comprobante desde la aplicación móvil, eligiendo los ítems que pagaste.";

    private static final String FIRMA = "Educar Para Transformar\nCentro Educativo Privado";
}
