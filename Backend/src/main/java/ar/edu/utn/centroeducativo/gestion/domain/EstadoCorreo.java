package ar.edu.utn.centroeducativo.gestion.domain;

public enum EstadoCorreo {
    /** El correo se armó y se guardó, pero no se envió porque el sistema está en modo sin envío. */
    SIMULADO,
    ENVIADO,
    ERROR
}
