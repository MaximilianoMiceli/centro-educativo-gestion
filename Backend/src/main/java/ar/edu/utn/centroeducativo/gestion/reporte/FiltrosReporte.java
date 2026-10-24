package ar.edu.utn.centroeducativo.gestion.reporte;

/**
 * Filtros opcionales. Cada reporte usa solo los que le corresponden.
 */
public record FiltrosReporte(
        Long alumnoId,
        Long docenteId,
        Short nivelId,
        Long cursoId,
        Long materiaId,
        Long deporteId,
        Short recorridoId
) {
}
