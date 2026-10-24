package ar.edu.utn.centroeducativo.gestion.web.dto;

public record InstalacionDTO(
        Long id,
        String nombre,
        String tipo,
        String ubicacion,
        Integer capacidad,
        String estado,
        String descripcion
) {
}
