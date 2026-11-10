package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record PreciosDTO(
        BigDecimal cuotaBase,
        BigDecimal comedor,
        List<ItemDeporte> deportes,
        List<ItemRecorrido> recorridos
) {

    public record ItemDeporte(Long deporteId, String deporte, BigDecimal monto) {
    }

    public record ItemRecorrido(Short recorridoId, String recorrido, BigDecimal monto) {
    }
}
