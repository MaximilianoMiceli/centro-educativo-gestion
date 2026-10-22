package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalDate;

public record AlumnoFichaDTO(
        Long id,
        String legajo,
        String dni,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String domicilio,
        String telefono,
        String email,
        Long cursoId,
        String cursoDescripcion,
        String nivelNombre,
        String estado
) {
}
