package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.FacturaRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.DeudaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.FacturaDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas de facturas y deuda para el alumno o sus padres. */
@Service
@RequiredArgsConstructor
public class FacturasFamiliaService {

    private static final LocalDate DESDE_MINIMO = LocalDate.of(2000, 1, 1);
    private static final LocalDate HASTA_MAXIMO = LocalDate.of(2999, 12, 31);

    private final AccesoFamilia acceso;
    private final FacturaRepository facturaRepository;
    private final FacturaMapper mapper;
    private final DocumentoPdf documentoPdf;

    /** Facturas del alumno emitidas entre las dos fechas, con sus pagos y comprobantes. */
    @Transactional(readOnly = true)
    public List<FacturaDTO> facturas(Long alumnoId, LocalDate desde, LocalDate hasta) {
        Alumno alumno = acceso.alumnoVisible(alumnoId);
        return facturasDelAlumno(alumno, desde, hasta).stream().map(mapper::aDTO).toList();
    }

    /** Historial de deuda del alumno, con cada ítem de pago por separado. */
    @Transactional(readOnly = true)
    public DeudaDTO deuda(Long alumnoId, LocalDate desde, LocalDate hasta) {
        Alumno alumno = acceso.alumnoVisible(alumnoId);
        List<Factura> facturas = facturasDelAlumno(alumno, desde, hasta);
        List<DeudaDTO.Item> items = facturas.stream()
                .flatMap(f -> f.getItems().stream().map(i -> mapper.aItemDeuda(f, i)))
                .toList();
        BigDecimal facturado = facturas.stream().map(Factura::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pagado = facturas.stream().map(FacturaMapper::pagado).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DeudaDTO(alumno.getId(), alumno.getApellido() + ", " + alumno.getNombre(), facturado, pagado,
                facturado.subtract(pagado), items);
    }

    @Transactional(readOnly = true)
    public FacturaDTO factura(Long facturaId) {
        return mapper.aDTO(facturaVisible(facturaId));
    }

    @Transactional(readOnly = true)
    public byte[] facturaPdf(Long facturaId) {
        return documentoPdf.factura(mapper.aDTO(facturaVisible(facturaId)));
    }

    private Factura facturaVisible(Long facturaId) {
        Factura factura = facturaRepository.findWithDetalleById(facturaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura con ID " + facturaId + " no encontrada"));
        acceso.alumnoVisible(factura.getAlumno().getId());
        return factura;
    }

    private List<Factura> facturasDelAlumno(Alumno alumno, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("El rango de fechas no es válido",
                    Map.of("desde", "La fecha desde no puede ser posterior a la fecha hasta"));
        }
        return facturaRepository.findDelAlumno(alumno.getId(), desde == null ? DESDE_MINIMO : desde,
                hasta == null ? HASTA_MAXIMO : hasta);
    }
}
