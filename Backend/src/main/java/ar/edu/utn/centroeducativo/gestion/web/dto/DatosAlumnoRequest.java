package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalDate;

public record DatosAlumnoRequest(
        String legajo,
        String dni,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String domicilio,
        String telefono,
        String email,
        Long cursoId,
        String estado
) {

    public DatosAlumnoRequest {
        legajo = recortar(legajo);
        dni = recortar(dni);
        nombre = recortar(nombre);
        apellido = recortar(apellido);
        domicilio = recortar(domicilio);
        telefono = recortar(telefono);
        email = recortar(email);
        estado = recortar(estado);
    }

    private static String recortar(String valor) {
        return valor == null ? null : valor.strip();
    }
}
