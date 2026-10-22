package ar.edu.utn.centroeducativo.gestion.web.dto;

public record DocenteUpdateRequest(
        String dni,
        String nombre,
        String apellido,
        String especialidad,
        String email,
        String telefono,
        String estado
) {

    public DocenteUpdateRequest {
        dni = recortar(dni);
        nombre = recortar(nombre);
        apellido = recortar(apellido);
        especialidad = recortar(especialidad);
        email = recortar(email);
        telefono = recortar(telefono);
        estado = recortar(estado);
    }

    private static String recortar(String valor) {
        return valor == null ? null : valor.strip();
    }
}
