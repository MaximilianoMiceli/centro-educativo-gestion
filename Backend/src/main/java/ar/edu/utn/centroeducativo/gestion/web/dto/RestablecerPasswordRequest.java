package ar.edu.utn.centroeducativo.gestion.web.dto;

public record RestablecerPasswordRequest(
        String password,
        Boolean debeCambiarPassword
) {
}
