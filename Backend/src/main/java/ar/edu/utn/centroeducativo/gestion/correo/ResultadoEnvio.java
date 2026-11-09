package ar.edu.utn.centroeducativo.gestion.correo;

import ar.edu.utn.centroeducativo.gestion.domain.EstadoCorreo;

public record ResultadoEnvio(EstadoCorreo estado, String detalleError) {

    public static ResultadoEnvio simulado() {
        return new ResultadoEnvio(EstadoCorreo.SIMULADO, null);
    }
}
