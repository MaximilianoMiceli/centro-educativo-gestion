package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record ActualizarPermisosRequest(
        List<String> permisos
) {
}
