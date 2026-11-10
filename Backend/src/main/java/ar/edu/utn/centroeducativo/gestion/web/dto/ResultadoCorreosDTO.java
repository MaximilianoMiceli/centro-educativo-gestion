package ar.edu.utn.centroeducativo.gestion.web.dto;

/**
 * Resumen de una ejecución. Los padres sin correo cargado no reciben nada y se cuentan aparte,
 * y los que no tienen deuda tampoco.
 */
public record ResultadoCorreosDTO(
        String tipo,
        String fecha,
        String modo,
        int facturasGeneradas,
        int enviados,
        int conError,
        int yaEnviados,
        int sinCorreo,
        int sinDeuda
) {
}
