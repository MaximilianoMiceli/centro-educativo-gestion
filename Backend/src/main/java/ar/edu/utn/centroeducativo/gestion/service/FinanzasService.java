package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.ConceptoPrecio;
import ar.edu.utn.centroeducativo.gestion.domain.Deporte;
import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.domain.FacturaItem;
import ar.edu.utn.centroeducativo.gestion.domain.GrupoDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.Pago;
import ar.edu.utn.centroeducativo.gestion.domain.RecorridoTransporte;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosFinanzasDTO;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Listados de ingresos y pagos para la administración. Todos filtran por un rango de fechas opcional. */
@Service
@RequiredArgsConstructor
public class FinanzasService {

    private static final LocalDate DESDE_MINIMO = LocalDate.of(2000, 1, 1);
    private static final LocalDate HASTA_MAXIMO = LocalDate.of(2999, 12, 31);

    private final EntityManager entityManager;

    /** Pagos recibidos entre las dos fechas (por fecha de la transferencia). */
    @Transactional(readOnly = true)
    public ListadosFinanzasDTO.Ingresos ingresos(LocalDate desde, LocalDate hasta) {
        LocalDate d = desdeOMinimo(desde, hasta);
        LocalDate h = hasta == null ? HASTA_MAXIMO : hasta;
        List<Pago> pagos = entityManager.createQuery(
                "select p from Pago p join fetch p.factura f join fetch f.alumno "
                        + "where p.fechaPago between :d and :h order by p.fechaPago, p.id", Pago.class)
                .setParameter("d", d).setParameter("h", h).getResultList();

        List<ListadosFinanzasDTO.Ingresos.Fila> filas = pagos.stream()
                .map(p -> new ListadosFinanzasDTO.Ingresos.Fila(p.getId(), p.getNumeroComprobante(), p.getFechaPago(),
                        p.getFactura().getAlumno().getId(), nombre(p.getFactura()), p.getFactura().getAlumno().getLegajo(),
                        FacturaMapper.numero(p.getFactura()), p.getFactura().getPeriodo(), p.getMonto()))
                .toList();
        Map<String, List<Pago>> porMes = pagos.stream().collect(Collectors.groupingBy(
                p -> YearMonth.from(p.getFechaPago()).toString(), TreeMap::new, Collectors.toList()));
        List<ListadosFinanzasDTO.Ingresos.Mes> meses = porMes.entrySet().stream()
                .map(e -> new ListadosFinanzasDTO.Ingresos.Mes(e.getKey(), sumaPagos(e.getValue()), e.getValue().size()))
                .toList();
        return new ListadosFinanzasDTO.Ingresos(desde, hasta, sumaPagos(pagos), pagos.size(), meses, filas);
    }

    /** Facturas pagadas por completo cuyo último pago cae en el rango, agrupables por año y alumno. */
    @Transactional(readOnly = true)
    public ListadosFinanzasDTO.PagosPorFactura pagosCompletos(LocalDate desde, LocalDate hasta) {
        return pagosPorFactura(desde, hasta, FacturaMapper.PAGADA);
    }

    /** Facturas con pagos parciales cuyo último pago cae en el rango, agrupables por año y alumno. */
    @Transactional(readOnly = true)
    public ListadosFinanzasDTO.PagosPorFactura pagosIncompletos(LocalDate desde, LocalDate hasta) {
        return pagosPorFactura(desde, hasta, FacturaMapper.PARCIAL);
    }

    /** Ítems de deporte de las facturas emitidas en el rango, con grupo, horario y profesor a cargo. */
    @Transactional(readOnly = true)
    public ListadosFinanzasDTO.PagosPorDeporte pagosPorDeporte(LocalDate desde, LocalDate hasta) {
        List<FacturaItem> items = itemsDeFacturas(ConceptoPrecio.DEPORTE, desde, hasta);
        Map<Long, Deporte> deportes = entityManager
                .createQuery("select d from Deporte d left join fetch d.docenteResponsable", Deporte.class)
                .getResultList().stream().collect(Collectors.toMap(Deporte::getId, d -> d));
        Map<String, GrupoDeporte> grupos = gruposActuales(items);

        List<ListadosFinanzasDTO.PagosPorDeporte.Fila> filas = items.stream().map(i -> {
            Factura f = i.getFactura();
            Deporte deporte = deportes.get(i.getReferenciaId());
            GrupoDeporte grupo = grupos.get(f.getAlumno().getId() + ":" + i.getReferenciaId());
            String nivel = grupo != null ? grupo.getNivel().getNombreLegible()
                    : f.getAlumno().getCurso().getNivel().getNombreLegible();
            String horario = grupo != null
                    ? FormatoHorario.rango(grupo.getDiaSemana(), grupo.getHoraInicio(), grupo.getHoraFin())
                    : "Sin grupo actual";
            String profesor = deporte == null || deporte.getDocenteResponsable() == null ? "-"
                    : deporte.getDocenteResponsable().getApellido() + ", " + deporte.getDocenteResponsable().getNombre();
            return new ListadosFinanzasDTO.PagosPorDeporte.Fila(
                    deporte == null ? i.getDescripcion() : deporte.getNombre(), nivel, horario, profesor,
                    f.getAlumno().getId(), nombre(f), f.getAlumno().getLegajo(), f.getAlumno().getCurso().getDescripcion(),
                    f.getPeriodo(), FacturaMapper.numero(f), i.getMonto(), estado(i), fechaPago(i));
        }).sorted(Comparator.comparing(ListadosFinanzasDTO.PagosPorDeporte.Fila::deporte)
                .thenComparing(ListadosFinanzasDTO.PagosPorDeporte.Fila::nivel)
                .thenComparing(ListadosFinanzasDTO.PagosPorDeporte.Fila::horario)
                .thenComparing(ListadosFinanzasDTO.PagosPorDeporte.Fila::alumno)).toList();
        return new ListadosFinanzasDTO.PagosPorDeporte(desde, hasta,
                sumaItems(items, true), sumaItems(items, false), filas);
    }

    /** Ítems de transporte de las facturas emitidas en el rango, por recorrido. */
    @Transactional(readOnly = true)
    public ListadosFinanzasDTO.PagosPorRecorrido pagosPorRecorrido(LocalDate desde, LocalDate hasta) {
        List<FacturaItem> items = itemsDeFacturas(ConceptoPrecio.TRANSPORTE, desde, hasta);
        Map<Long, String> recorridos = entityManager.createQuery("select r from RecorridoTransporte r",
                RecorridoTransporte.class).getResultList().stream()
                .collect(Collectors.toMap(r -> r.getId().longValue(), RecorridoTransporte::getNombre));

        List<ListadosFinanzasDTO.PagosPorRecorrido.Fila> filas = items.stream().map(i -> {
            Factura f = i.getFactura();
            return new ListadosFinanzasDTO.PagosPorRecorrido.Fila(
                    recorridos.getOrDefault(i.getReferenciaId(), i.getDescripcion()), f.getAlumno().getId(), nombre(f),
                    f.getAlumno().getLegajo(), f.getAlumno().getCurso().getNivel().getNombreLegible(),
                    f.getAlumno().getCurso().getDescripcion(), f.getPeriodo(), FacturaMapper.numero(f), i.getMonto(),
                    estado(i), fechaPago(i));
        }).sorted(Comparator.comparing(ListadosFinanzasDTO.PagosPorRecorrido.Fila::recorrido)
                .thenComparing(ListadosFinanzasDTO.PagosPorRecorrido.Fila::alumno)).toList();
        return new ListadosFinanzasDTO.PagosPorRecorrido(desde, hasta,
                sumaItems(items, true), sumaItems(items, false), filas);
    }

    private ListadosFinanzasDTO.PagosPorFactura pagosPorFactura(LocalDate desde, LocalDate hasta, String estadoBuscado) {
        LocalDate d = desdeOMinimo(desde, hasta);
        LocalDate h = hasta == null ? HASTA_MAXIMO : hasta;
        Map<Long, LocalDate> ultimoPago = new HashMap<>();
        entityManager.createQuery("select p.factura.id, max(p.fechaPago) from Pago p group by p.factura.id",
                Object[].class).getResultList()
                .forEach(fila -> ultimoPago.put((Long) fila[0], (LocalDate) fila[1]));
        List<Long> ids = ultimoPago.entrySet().stream()
                .filter(e -> !e.getValue().isBefore(d) && !e.getValue().isAfter(h)).map(Map.Entry::getKey).toList();
        if (ids.isEmpty()) {
            return new ListadosFinanzasDTO.PagosPorFactura(desde, hasta, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, List.of());
        }

        List<Factura> facturas = entityManager.createQuery(
                "select distinct f from Factura f join fetch f.alumno left join fetch f.items where f.id in :ids",
                Factura.class).setParameter("ids", ids).getResultList().stream()
                .filter(f -> estadoBuscado.equals(FacturaMapper.estado(f))).toList();
        List<ListadosFinanzasDTO.PagosPorFactura.Fila> filas = facturas.stream().map(f -> {
            BigDecimal pagado = FacturaMapper.pagado(f);
            LocalDate ultimo = ultimoPago.get(f.getId());
            return new ListadosFinanzasDTO.PagosPorFactura.Fila(ultimo.getYear(), f.getAlumno().getId(), nombre(f),
                    f.getAlumno().getLegajo(), f.getId(), FacturaMapper.numero(f), f.getPeriodo(), f.getTotal(), pagado,
                    f.getTotal().subtract(pagado), ultimo);
        }).sorted(Comparator.comparing(ListadosFinanzasDTO.PagosPorFactura.Fila::anio)
                .thenComparing(ListadosFinanzasDTO.PagosPorFactura.Fila::alumno)
                .thenComparing(ListadosFinanzasDTO.PagosPorFactura.Fila::periodo)).toList();
        return new ListadosFinanzasDTO.PagosPorFactura(desde, hasta,
                filas.stream().map(ListadosFinanzasDTO.PagosPorFactura.Fila::total).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas.stream().map(ListadosFinanzasDTO.PagosPorFactura.Fila::pagado).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas.stream().map(ListadosFinanzasDTO.PagosPorFactura.Fila::saldo).reduce(BigDecimal.ZERO, BigDecimal::add),
                filas);
    }

    private List<FacturaItem> itemsDeFacturas(ConceptoPrecio concepto, LocalDate desde, LocalDate hasta) {
        LocalDate d = desdeOMinimo(desde, hasta);
        LocalDate h = hasta == null ? HASTA_MAXIMO : hasta;
        return entityManager.createQuery(
                "select i from FacturaItem i join fetch i.factura f join fetch f.alumno a join fetch a.curso c "
                        + "join fetch c.nivel left join fetch i.pago "
                        + "where i.concepto = :concepto and f.emision between :d and :h "
                        + "order by f.periodo, a.apellido, a.nombre", FacturaItem.class)
                .setParameter("concepto", concepto).setParameter("d", d).setParameter("h", h).getResultList();
    }

    /** Grupo actual de cada alumno en cada deporte, con clave alumno:deporte. */
    private Map<String, GrupoDeporte> gruposActuales(List<FacturaItem> items) {
        List<Long> alumnos = items.stream().map(i -> i.getFactura().getAlumno().getId()).distinct().toList();
        Map<String, GrupoDeporte> grupos = new HashMap<>();
        if (alumnos.isEmpty()) {
            return grupos;
        }
        List<InscripcionDeporte> inscripciones = entityManager.createQuery(
                "select i from InscripcionDeporte i join fetch i.grupo g join fetch g.nivel join fetch g.deporte "
                        + "where i.alumno.id in :alumnos", InscripcionDeporte.class)
                .setParameter("alumnos", alumnos).getResultList();
        for (InscripcionDeporte i : inscripciones) {
            grupos.putIfAbsent(i.getAlumno().getId() + ":" + i.getGrupo().getDeporte().getId(), i.getGrupo());
        }
        return grupos;
    }

    private static LocalDate desdeOMinimo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("El rango de fechas no es válido",
                    Map.of("desde", "La fecha desde no puede ser posterior a la fecha hasta"));
        }
        return desde == null ? DESDE_MINIMO : desde;
    }

    private static BigDecimal sumaPagos(List<Pago> pagos) {
        return pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumaItems(List<FacturaItem> items, boolean pagados) {
        return items.stream().filter(i -> (i.getPago() != null) == pagados).map(FacturaItem::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String nombre(Factura factura) {
        return factura.getAlumno().getApellido() + ", " + factura.getAlumno().getNombre();
    }

    private static String estado(FacturaItem item) {
        return item.getPago() != null ? "PAGADO" : "PENDIENTE";
    }

    private static LocalDate fechaPago(FacturaItem item) {
        return item.getPago() != null ? item.getPago().getFechaPago() : null;
    }
}
