package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CrearDictadoRequest(
        Long docenteId,
        Long materiaId,
        Long cursoId
) {
}
