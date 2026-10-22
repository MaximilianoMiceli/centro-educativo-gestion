package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CambiarPasswordRequest(
        String passwordActual,
        String passwordNueva
) {
}
