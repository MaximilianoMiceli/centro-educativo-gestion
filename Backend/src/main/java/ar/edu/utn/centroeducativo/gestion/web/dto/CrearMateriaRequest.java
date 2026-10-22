package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CrearMateriaRequest(
        String nombre
) {

    public CrearMateriaRequest {
        nombre = nombre == null ? null : nombre.strip();
    }
}
