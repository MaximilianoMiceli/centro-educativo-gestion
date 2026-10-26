package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Filas de los listados globales de servicios (una fila por alumno o por atención). */
public final class ListadosServiciosDTO {

    private ListadosServiciosDTO() {
    }

    public record InscriptoIdiomaDTO(
            Long alumnoId,
            String legajo,
            String alumno,
            String nivel,
            String curso,
            String idioma,
            LocalDate fechaInscripcion
    ) {
    }

    public record InscriptoApoyoDTO(
            Long alumnoId,
            String legajo,
            String alumno,
            String nivel,
            String curso,
            LocalDate fechaInicio,
            String observaciones
    ) {
    }

    public record AtencionListadoDTO(
            Long id,
            Long alumnoId,
            String legajo,
            String alumno,
            String nivel,
            String curso,
            LocalDateTime fechaHora,
            String motivo,
            String observaciones,
            String registradoPor
    ) {
    }
}
