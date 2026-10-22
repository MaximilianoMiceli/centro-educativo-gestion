package ar.edu.utn.centroeducativo.gestion.web.dto;

public record UsuarioCreadoDTO(
        Long id,
        String username,
        String email,
        String rol,
        String passwordTemporal
) {
}
