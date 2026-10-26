package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inscripcion_apoyo")
@Getter
@Setter
@NoArgsConstructor
public class InscripcionApoyo {

    @Id
    private Long alumnoId;

    private LocalDate fechaInicio;

    private String observaciones;
}
