package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalTime;

public record HorarioDTO(
        Long id,
        Long dictadoId,
        String materia,
        String curso,
        String docente,
        Short diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin
) {
}
