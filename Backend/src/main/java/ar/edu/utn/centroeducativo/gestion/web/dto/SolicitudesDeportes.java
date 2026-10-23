package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalTime;

public final class SolicitudesDeportes {

    private SolicitudesDeportes() {
    }

    public record CrearDeporte(String nombre, Long docenteResponsableId) {
    }

    public record AsignarResponsable(Long docenteId) {
    }

    public record CrearGrupo(Short nivelId, Short diaSemana, LocalTime horaInicio, LocalTime horaFin) {
    }

    public record InscribirDeporte(Long grupoId) {
    }
}
