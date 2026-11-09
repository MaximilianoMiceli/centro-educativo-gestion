package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Los campos que vienen en null no se modifican. */
public record ActualizarPreciosRequest(
        BigDecimal cuotaBase,
        BigDecimal comedor,
        List<ItemDeporte> deportes,
        List<ItemRecorrido> recorridos
) {

    public record ItemDeporte(Long deporteId, BigDecimal monto) {
    }

    public record ItemRecorrido(Short recorridoId, BigDecimal monto) {
    }
}
