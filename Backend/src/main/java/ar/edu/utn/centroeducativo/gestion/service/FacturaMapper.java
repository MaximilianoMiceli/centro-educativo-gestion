package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.domain.FacturaItem;
import ar.edu.utn.centroeducativo.gestion.web.dto.DeudaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.FacturaDTO;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FacturaMapper {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String PARCIAL = "PARCIAL";
    public static final String PAGADA = "PAGADA";

    private final Clock reloj;

    public static String numero(Factura factura) {
        return "F-" + factura.getPeriodo().replace("-", "") + "-" + String.format("%06d", factura.getId());
    }

    public static BigDecimal pagado(Factura factura) {
        return factura.getItems().stream()
                .filter(i -> i.getPago() != null)
                .map(FacturaItem::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static String estado(Factura factura) {
        BigDecimal pagado = pagado(factura);
        if (pagado.compareTo(factura.getTotal()) >= 0) {
            return PAGADA;
        }
        return pagado.signum() == 0 ? PENDIENTE : PARCIAL;
    }

    public boolean vencida(Factura factura) {
        return !PAGADA.equals(estado(factura)) && factura.getVencimiento().isBefore(LocalDate.now(reloj));
    }

    public FacturaDTO aDTO(Factura factura) {
        BigDecimal pagado = pagado(factura);
        List<FacturaDTO.Item> items = factura.getItems().stream()
                .map(i -> new FacturaDTO.Item(i.getId(), i.getConcepto().name(), i.getDescripcion(), i.getMonto(),
                        i.getPago() != null, i.getPago() == null ? null : i.getPago().getId()))
                .toList();
        List<FacturaDTO.PagoResumen> pagos = factura.getPagos().stream()
                .map(p -> new FacturaDTO.PagoResumen(p.getId(), p.getNumeroComprobante(), p.getMonto(),
                        p.getFechaPago(), p.getRegistradoEn(), p.getNombreArchivo(),
                        p.getItems().stream().map(FacturaItem::getId).toList()))
                .toList();
        return new FacturaDTO(factura.getId(), numero(factura), factura.getAlumno().getId(),
                factura.getAlumno().getApellido() + ", " + factura.getAlumno().getNombre(),
                factura.getPeriodo(), factura.getEmision(), factura.getVencimiento(), factura.getTotal(), pagado,
                factura.getTotal().subtract(pagado), estado(factura), vencida(factura), items, pagos);
    }

    public DeudaDTO.Item aItemDeuda(Factura factura, FacturaItem item) {
        String estado = item.getPago() != null ? "PAGADO" : (vencida(factura) ? "VENCIDO" : "PENDIENTE");
        return new DeudaDTO.Item(factura.getId(), numero(factura), factura.getPeriodo(), factura.getVencimiento(),
                item.getConcepto().name(), item.getDescripcion(), item.getMonto(), estado);
    }
}
