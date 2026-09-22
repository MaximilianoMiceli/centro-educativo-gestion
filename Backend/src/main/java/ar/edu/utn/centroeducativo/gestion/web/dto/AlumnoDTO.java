package ar.edu.utn.centroeducativo.gestion.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoDTO {

    private Long id;

    private String legajo;

    private String dni;

    private String nombre;

    private String apellido;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private String domicilio;

    private String telefono;

    private String email;

    private Long cursoId;

    private String cursoDescripcion;

    private String nivelNombre;

    private String estado;
}
