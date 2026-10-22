package ar.edu.utn.centroeducativo.gestion.exception;

import java.time.LocalDateTime;

public class EdicionBloqueadaException extends RuntimeException {

    private final LocalDateTime bloqueadaHasta;

    public EdicionBloqueadaException(String mensaje, LocalDateTime bloqueadaHasta) {
        super(mensaje);
        this.bloqueadaHasta = bloqueadaHasta;
    }

    public LocalDateTime getBloqueadaHasta() {
        return bloqueadaHasta;
    }
}
