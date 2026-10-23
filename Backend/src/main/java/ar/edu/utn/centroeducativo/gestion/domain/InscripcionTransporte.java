package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inscripcion_transporte")
@Getter
@Setter
@NoArgsConstructor
public class InscripcionTransporte {

    @Id
    @Column(name = "alumno_id")
    private Long alumnoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorrido_id")
    private RecorridoTransporte recorrido;
}
