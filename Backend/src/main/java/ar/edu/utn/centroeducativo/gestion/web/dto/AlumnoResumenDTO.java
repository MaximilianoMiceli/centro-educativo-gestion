package ar.edu.utn.centroeducativo.gestion.web.dto;

public record AlumnoResumenDTO(
        Long id,
        String legajo,
        String alumno,
        String curso,
        String nivel
) {
}
