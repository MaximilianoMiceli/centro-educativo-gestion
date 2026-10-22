package ar.edu.utn.centroeducativo.gestion.exception;

import java.util.Map;

public class ValidacionException extends RuntimeException {

    private final Map<String, String> errores;

    public ValidacionException(String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.errores = Map.copyOf(errores);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
