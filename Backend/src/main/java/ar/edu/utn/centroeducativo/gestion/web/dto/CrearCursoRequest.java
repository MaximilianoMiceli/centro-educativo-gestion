package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CrearCursoRequest(
        Short nivelId,
        Short anio,
        String seccion
) {

    public CrearCursoRequest {
        seccion = seccion == null ? null : seccion.strip().toUpperCase();
    }
}
