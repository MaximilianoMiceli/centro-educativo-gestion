package ar.edu.utn.centroeducativo.gestion.web.dto;

public record DictadoDTO(
        Long id,
        Long docenteId,
        String docente,
        Long materiaId,
        String materia,
        Long cursoId,
        String curso
) {
}
