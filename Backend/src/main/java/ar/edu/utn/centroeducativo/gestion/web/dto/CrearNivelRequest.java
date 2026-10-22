package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CrearNivelRequest(
        String nombre
) {

    public CrearNivelRequest {
        nombre = nombre == null ? null : nombre.strip();
    }
}
