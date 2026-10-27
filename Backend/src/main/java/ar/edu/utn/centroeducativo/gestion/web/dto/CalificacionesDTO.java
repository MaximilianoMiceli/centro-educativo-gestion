package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CalificacionesDTO {

    private CalificacionesDTO() {
    }

    /** Materia que el usuario puede calificar (a cargo suyo, o todas si es administrador). */
    public record DictadoCalificableDTO(Long id, String materia, String curso, String nivel, String docente) {
    }

    public record EvaluacionDTO(Long id, String titulo, String tipo, Short periodo, LocalDate fecha) {
    }

    public record CrearEvaluacionRequest(String titulo, String tipo, Short periodo, LocalDate fecha) {
    }

    public record NotaAlumnoDTO(Long alumnoId, String legajo, String alumno, BigDecimal nota) {
    }

    public record PlanillaDTO(Long evaluacionId, String titulo, String tipo, String materia, String curso,
                              LocalDate fecha, List<NotaAlumnoDTO> alumnos) {
    }

    public record NotaRequest(Long alumnoId, BigDecimal nota) {
    }

    public record GuardarNotasRequest(List<NotaRequest> notas) {
    }

    public record EvaluacionNotaDTO(String titulo, String tipo, Short periodo, LocalDate fecha, BigDecimal nota) {
    }

    public record MateriaCalificacionesDTO(String materia, String docente, BigDecimal promedio,
                                           List<EvaluacionNotaDTO> evaluaciones) {
    }
}
