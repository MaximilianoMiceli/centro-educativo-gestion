package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;

public record ResultadoFacturacionDTO(String periodo, int generadas, int omitidas, BigDecimal totalFacturado) {
}
