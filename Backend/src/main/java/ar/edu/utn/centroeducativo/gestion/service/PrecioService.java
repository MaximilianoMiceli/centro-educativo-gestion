package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.ConceptoPrecio;
import ar.edu.utn.centroeducativo.gestion.domain.Precio;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.DeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.PrecioRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RecorridoTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.ActualizarPreciosRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.PreciosDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrecioService {

    private static final BigDecimal MAXIMO = new BigDecimal("99999999.99");

    private final PrecioRepository precioRepository;
    private final DeporteRepository deporteRepository;
    private final RecorridoTransporteRepository recorridoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public PreciosDTO obtener() {
        List<Precio> precios = precioRepository.findAll();
        List<PreciosDTO.ItemDeporte> deportes = deporteRepository.findAllByOrderByNombreAsc().stream()
                .map(d -> new PreciosDTO.ItemDeporte(d.getId(), d.getNombre(), montoDeporte(precios, d.getId())))
                .toList();
        List<PreciosDTO.ItemRecorrido> recorridos = recorridoRepository.findAllByOrderByIdAsc().stream()
                .map(r -> new PreciosDTO.ItemRecorrido(r.getId(), r.getNombre(), montoRecorrido(precios, r.getId())))
                .toList();
        return new PreciosDTO(montoGeneral(precios, ConceptoPrecio.CUOTA_BASE),
                montoGeneral(precios, ConceptoPrecio.COMEDOR), deportes, recorridos);
    }

    @Transactional
    public PreciosDTO actualizar(ActualizarPreciosRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarMonto("cuotaBase", request.cuotaBase(), errores);
        validarMonto("comedor", request.comedor(), errores);
        for (ActualizarPreciosRequest.ItemDeporte item : orVacio(request.deportes())) {
            if (item == null || item.deporteId() == null || !deporteRepository.existsById(item.deporteId())) {
                errores.put("deporte_" + (item == null ? null : item.deporteId()), "El deporte no existe");
            } else {
                validarMonto("deporte_" + item.deporteId(), item.monto(), errores);
            }
        }
        for (ActualizarPreciosRequest.ItemRecorrido item : orVacio(request.recorridos())) {
            if (item == null || item.recorridoId() == null || !recorridoRepository.existsById(item.recorridoId())) {
                errores.put("recorrido_" + (item == null ? null : item.recorridoId()), "El recorrido no existe");
            } else {
                validarMonto("recorrido_" + item.recorridoId(), item.monto(), errores);
            }
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay valores inválidos en los precios", errores);
        }

        List<Precio> actuales = new ArrayList<>(precioRepository.findAll());
        List<String> cambios = new ArrayList<>();
        if (request.cuotaBase() != null) {
            fijar(actuales, ConceptoPrecio.CUOTA_BASE, null, null, request.cuotaBase(), "cuota base", cambios);
        }
        if (request.comedor() != null) {
            fijar(actuales, ConceptoPrecio.COMEDOR, null, null, request.comedor(), "comedor", cambios);
        }
        for (ActualizarPreciosRequest.ItemDeporte item : orVacio(request.deportes())) {
            if (item.monto() != null) {
                fijar(actuales, ConceptoPrecio.DEPORTE, item.deporteId(), null, item.monto(),
                        "deporte " + item.deporteId(), cambios);
            }
        }
        for (ActualizarPreciosRequest.ItemRecorrido item : orVacio(request.recorridos())) {
            if (item.monto() != null) {
                fijar(actuales, ConceptoPrecio.TRANSPORTE, null, item.recorridoId(), item.monto(),
                        "recorrido " + item.recorridoId(), cambios);
            }
        }
        if (!cambios.isEmpty()) {
            auditoriaService.registrar("MODIFICACION", "PRECIO", null, String.join("; ", cambios));
        }
        return obtener();
    }

    private void fijar(List<Precio> actuales, ConceptoPrecio concepto, Long deporteId, Short recorridoId,
            BigDecimal montoNuevo, String etiqueta, List<String> cambios) {
        BigDecimal monto = montoNuevo.setScale(2, RoundingMode.HALF_UP);
        Precio precio = actuales.stream()
                .filter(p -> p.getConcepto() == concepto
                        && Objects.equals(p.getDeporteId(), deporteId)
                        && Objects.equals(p.getRecorridoId(), recorridoId))
                .findFirst()
                .orElse(null);
        if (precio == null) {
            precio = new Precio();
            precio.setConcepto(concepto);
            precio.setDeporteId(deporteId);
            precio.setRecorridoId(recorridoId);
            precio.setMonto(monto);
            precioRepository.save(precio);
            actuales.add(precio);
            cambios.add(etiqueta + ": sin precio -> " + monto);
        } else if (precio.getMonto().compareTo(monto) != 0) {
            cambios.add(etiqueta + ": " + precio.getMonto() + " -> " + monto);
            precio.setMonto(monto);
            precio.setActualizadoEn(LocalDateTime.now());
            precioRepository.save(precio);
        }
    }

    private static void validarMonto(String campo, BigDecimal monto, Map<String, String> errores) {
        if (monto == null) {
            return;
        }
        if (monto.signum() < 0) {
            errores.put(campo, "El valor no puede ser negativo");
        } else if (monto.stripTrailingZeros().scale() > 2) {
            errores.put(campo, "El valor admite como máximo dos decimales");
        } else if (monto.compareTo(MAXIMO) > 0) {
            errores.put(campo, "El valor no puede superar " + MAXIMO);
        }
    }

    private static BigDecimal montoGeneral(List<Precio> precios, ConceptoPrecio concepto) {
        return precios.stream().filter(p -> p.getConcepto() == concepto).map(Precio::getMonto).findFirst().orElse(null);
    }

    private static BigDecimal montoDeporte(List<Precio> precios, Long deporteId) {
        return precios.stream()
                .filter(p -> p.getConcepto() == ConceptoPrecio.DEPORTE && deporteId.equals(p.getDeporteId()))
                .map(Precio::getMonto).findFirst().orElse(null);
    }

    private static BigDecimal montoRecorrido(List<Precio> precios, Short recorridoId) {
        return precios.stream()
                .filter(p -> p.getConcepto() == ConceptoPrecio.TRANSPORTE && recorridoId.equals(p.getRecorridoId()))
                .map(Precio::getMonto).findFirst().orElse(null);
    }

    private static <T> List<T> orVacio(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}
