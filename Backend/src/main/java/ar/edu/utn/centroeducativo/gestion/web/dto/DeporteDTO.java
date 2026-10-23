package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record DeporteDTO(
        Long id,
        String nombre,
        Long docenteResponsableId,
        String docenteResponsable,
        List<GrupoDTO> grupos
) {

    public record GrupoDTO(
            Long id,
            String nivel,
            String horario
    ) {
    }
}
