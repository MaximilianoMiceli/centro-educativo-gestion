package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalDate;
import java.util.List;

public record AlumnoServiciosDTO(
        Long alumnoId,
        String alumno,
        String nivel,
        List<InscripcionDeporteDTO> deportes,
        RecorridoDTO transporte,
        boolean comedor,
        List<IdiomaInscriptoDTO> idiomas,
        ApoyoDTO apoyo
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

    public record IdiomaInscriptoDTO(
            Long id,
            Short idiomaId,
            String idioma,
            LocalDate fechaInscripcion
    ) {
    }

    public record ApoyoDTO(
            LocalDate fechaInicio,
            String observaciones
    ) {
    }
}
