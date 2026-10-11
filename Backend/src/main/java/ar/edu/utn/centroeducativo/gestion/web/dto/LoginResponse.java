package ar.edu.utn.centroeducativo.gestion.web.dto;

public record LoginResponse(
        String token,
        String username,
        String rol
) {
}
