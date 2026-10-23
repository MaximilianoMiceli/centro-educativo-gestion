package ar.edu.utn.centroeducativo.gestion.web.dto;

public record UsuarioListaDTO(
        Long id,
        String username,
        String email,
        String rol,
        String numeroDocumento,
        boolean activo,
        boolean debeCambiarPassword
) {
}
