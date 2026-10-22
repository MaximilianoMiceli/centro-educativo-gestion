package ar.edu.utn.centroeducativo.gestion.web.dto;

public record LoginResponse(
        String token,
        String username,
        String rol,
        boolean debeCambiarPassword
) {

    public LoginResponse(String token, String username, String rol) {
        this(token, username, rol, false);
    }
}
