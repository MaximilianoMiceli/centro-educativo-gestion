package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record DocenteAgendaDTO(
        Long docenteId,
        String docente,
        List<CursoAgendaDTO> cursos
) {

    public record CursoAgendaDTO(
            Long cursoId,
            String curso,
            String nivel,
            List<MateriaAgendaDTO> materias
    ) {
    }

    public record MateriaAgendaDTO(
            String materia,
            List<String> horarios
    ) {
    }
}
