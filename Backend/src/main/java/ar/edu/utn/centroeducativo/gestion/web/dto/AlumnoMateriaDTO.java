package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record AlumnoMateriaDTO(
        String materia,
        String docente,
        List<String> horarios
) {
}
