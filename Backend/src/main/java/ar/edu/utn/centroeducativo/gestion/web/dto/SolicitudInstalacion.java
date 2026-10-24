package ar.edu.utn.centroeducativo.gestion.web.dto;

/** Tipo y estado llegan como texto para validarlos con mensajes en español, no con un error de deserialización. */
public record SolicitudInstalacion(
        String nombre,
        String tipo,
        String ubicacion,
        Integer capacidad,
        String estado,
        String descripcion
) {
    public SolicitudInstalacion {
        nombre = nombre == null ? null : nombre.strip();
        ubicacion = blankAVacio(ubicacion);
        descripcion = blankAVacio(descripcion);
    }

    private static String blankAVacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
