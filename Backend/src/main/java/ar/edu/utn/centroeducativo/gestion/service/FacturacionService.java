package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.ConceptoPrecio;
import ar.edu.utn.centroeducativo.gestion.domain.EstadoAlumno;
import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.domain.FacturaItem;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.FacturaRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.PreciosDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ResultadoFacturacionDTO;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Emite la factura mensual de cada alumno activo a partir de sus inscripciones y de los precios vigentes. */
@Service
@RequiredArgsConstructor
public class FacturacionService {

    private static final Pattern PERIODO = Pattern.compile("\\d{4}-(0[1-9]|1[0-2])");
    private static final int DIA_VENCIMIENTO = 20;

    private final AlumnoRepository alumnoRepository;
    private final FacturaRepository facturaRepository;
    private final PrecioService precioService;
    private final AuditoriaService auditoriaService;
    private final EntityManager entityManager;
    private final Clock reloj;

    /** Genera las facturas del período AAAA-MM. Los alumnos que ya tienen factura en ese mes se omiten. */
    @Transactional
    public ResultadoFacturacionDTO generar(String periodo) {
        YearMonth mes = validarPeriodo(periodo);
        PreciosDTO precios = precioService.obtener();
        Map<String, String> errores = new LinkedHashMap<>();
        if (precios.cuotaBase() == null) {
            errores.put("cuotaBase", "Falta definir el valor de la cuota base");
        }
        if (precios.comedor() == null) {
            errores.put("comedor", "Falta definir el valor del comedor");
        }
        Map<Long, BigDecimal> precioDeporte = new HashMap<>();
        precios.deportes().forEach(d -> {
            if (d.monto() != null) {
                precioDeporte.put(d.deporteId(), d.monto());
            }
        });
        Map<Short, BigDecimal> precioRecorrido = new HashMap<>();
        precios.recorridos().forEach(r -> {
            if (r.monto() != null) {
                precioRecorrido.put(r.recorridoId(), r.monto());
            }
        });

        Map<Long, Map<Long, String>> deportesPorAlumno = deportesPorAlumno();
        Map<Long, Object[]> transportePorAlumno = transportePorAlumno();
        Set<Long> conComedor = alumnosConComedor();
        Set<Long> yaFacturados = facturaRepository.findAlumnoIdsConFactura(periodo);

        List<Factura> nuevas = new ArrayList<>();
        int omitidas = 0;
        for (Alumno alumno : alumnoRepository.findAll()) {
            if (alumno.getEstado() != EstadoAlumno.ACTIVO) {
                continue;
            }
            if (yaFacturados.contains(alumno.getId())) {
                omitidas++;
                continue;
            }
            Factura factura = new Factura();
            factura.setAlumno(alumno);
            factura.setPeriodo(periodo);
            factura.setEmision(LocalDate.now(reloj));
            factura.setVencimiento(mes.atDay(DIA_VENCIMIENTO));
            if (precios.cuotaBase() != null) {
                agregar(factura, ConceptoPrecio.CUOTA_BASE, "Cuota base", precios.cuotaBase());
            }
            deportesPorAlumno.getOrDefault(alumno.getId(), Map.of()).forEach((deporteId, nombre) -> {
                BigDecimal monto = precioDeporte.get(deporteId);
                if (monto == null) {
                    errores.put("deporte_" + deporteId, "Falta el valor del deporte " + nombre);
                } else {
                    agregar(factura, ConceptoPrecio.DEPORTE, "Deporte: " + nombre, monto);
                }
            });
            Object[] transporte = transportePorAlumno.get(alumno.getId());
            if (transporte != null) {
                BigDecimal monto = precioRecorrido.get((Short) transporte[0]);
                if (monto == null) {
                    errores.put("recorrido_" + transporte[0], "Falta el valor del recorrido " + transporte[1]);
                } else {
                    agregar(factura, ConceptoPrecio.TRANSPORTE, "Transporte: " + transporte[1], monto);
                }
            }
            if (conComedor.contains(alumno.getId()) && precios.comedor() != null) {
                agregar(factura, ConceptoPrecio.COMEDOR, "Servicio de comedor", precios.comedor());
            }
            factura.setTotal(factura.getItems().stream().map(FacturaItem::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            nuevas.add(factura);
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se puede facturar: faltan valores en los precios", errores);
        }

        facturaRepository.saveAll(nuevas);
        BigDecimal total = nuevas.stream().map(Factura::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!nuevas.isEmpty()) {
            auditoriaService.registrar("ALTA", "FACTURACION", null,
                    "Período " + periodo + ": " + nuevas.size() + " facturas por " + total);
        }
        return new ResultadoFacturacionDTO(periodo, nuevas.size(), omitidas, total);
    }

    private static YearMonth validarPeriodo(String periodo) {
        if (periodo == null || !PERIODO.matcher(periodo).matches()) {
            throw new ValidacionException("El período no es válido",
                    Map.of("periodo", "Usá el formato AAAA-MM, por ejemplo 2026-12"));
        }
        try {
            return YearMonth.parse(periodo);
        } catch (DateTimeException e) {
            throw new ValidacionException("El período no es válido", Map.of("periodo", "El mes no existe"));
        }
    }

    private static void agregar(Factura factura, ConceptoPrecio concepto, String descripcion, BigDecimal monto) {
        FacturaItem item = new FacturaItem();
        item.setFactura(factura);
        item.setConcepto(concepto);
        item.setDescripcion(descripcion);
        item.setMonto(monto);
        factura.getItems().add(item);
    }

    /** Un alumno inscripto en dos grupos del mismo deporte lo paga una sola vez. */
    private Map<Long, Map<Long, String>> deportesPorAlumno() {
        List<Object[]> filas = entityManager.createQuery(
                "select i.alumno.id, g.deporte.id, g.deporte.nombre from InscripcionDeporte i join i.grupo g "
                        + "order by g.deporte.nombre", Object[].class).getResultList();
        Map<Long, Map<Long, String>> resultado = new HashMap<>();
        for (Object[] fila : filas) {
            resultado.computeIfAbsent((Long) fila[0], k -> new LinkedHashMap<>()).put((Long) fila[1], (String) fila[2]);
        }
        return resultado;
    }

    private Map<Long, Object[]> transportePorAlumno() {
        List<Object[]> filas = entityManager.createQuery(
                "select t.alumnoId, t.recorrido.id, t.recorrido.nombre from InscripcionTransporte t",
                Object[].class).getResultList();
        Map<Long, Object[]> resultado = new HashMap<>();
        for (Object[] fila : filas) {
            resultado.put((Long) fila[0], new Object[] {fila[1], fila[2]});
        }
        return resultado;
    }

    private Set<Long> alumnosConComedor() {
        return new LinkedHashSet<>(entityManager.createQuery("select c.alumnoId from InscripcionComedor c", Long.class)
                .getResultList());
    }
}
