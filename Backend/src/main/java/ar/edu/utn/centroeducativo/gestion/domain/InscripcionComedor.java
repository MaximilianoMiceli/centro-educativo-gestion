package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inscripcion_comedor")
@Getter
@Setter
@NoArgsConstructor
public class InscripcionComedor {

    @Id
    private Long alumnoId;

    public InscripcionComedor(Long alumnoId) {
        this.alumnoId = alumnoId;
    }
}
