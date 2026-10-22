package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalTime;

public record CrearHorarioRequest(
        Long dictadoId,
        Short diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin
) {
}
