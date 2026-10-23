package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record VincularHijosRequest(
        List<Long> alumnoIds
) {
}
