package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record AlumnoServiciosDTO(
        Long alumnoId,
        String alumno,
        String nivel,
        List<InscripcionDeporteDTO> deportes,
        RecorridoDTO transporte,
        boolean comedor
) {

    public record InscripcionDeporteDTO(
            Long id,
            Long deporteId,
            String deporte,
            String horario
    ) {
    }

    public record RecorridoDTO(
            Short id,
            String nombre
    ) {
    }
}
