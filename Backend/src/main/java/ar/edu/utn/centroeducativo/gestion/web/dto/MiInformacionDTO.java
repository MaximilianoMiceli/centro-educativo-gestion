package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record MiInformacionDTO(
        AlumnoFichaDTO ficha,
        List<AlumnoMateriaDTO> materias,
        AlumnoServiciosDTO servicios
) {
}
