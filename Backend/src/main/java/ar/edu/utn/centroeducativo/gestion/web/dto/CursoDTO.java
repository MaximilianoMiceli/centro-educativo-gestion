package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CursoDTO(
        Long id,
        Short nivelId,
        String nivel,
        Short anio,
        String seccion,
        String descripcion
) {
}
