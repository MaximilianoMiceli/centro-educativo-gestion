package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record RolPermisosDTO(
        String nombre,
        List<String> permisos
) {
}
