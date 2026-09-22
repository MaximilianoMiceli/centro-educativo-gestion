package ar.edu.utn.centroeducativo.gestion.exception;

public class NoAutorizadoException extends RuntimeException {

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }

    public NoAutorizadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
