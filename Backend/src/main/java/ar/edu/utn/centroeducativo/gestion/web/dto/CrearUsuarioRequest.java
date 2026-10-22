package ar.edu.utn.centroeducativo.gestion.web.dto;

public record CrearUsuarioRequest(
        String username,
        String email,
        String tipoDocumento,
        String numeroDocumento,
        String rol
) {

    public CrearUsuarioRequest {
        username = recortar(username);
        email = recortar(email);
        tipoDocumento = recortar(tipoDocumento);
        numeroDocumento = recortar(numeroDocumento);
        rol = recortar(rol);
    }

    private static String recortar(String valor) {
        return valor == null ? null : valor.strip();
    }
}
