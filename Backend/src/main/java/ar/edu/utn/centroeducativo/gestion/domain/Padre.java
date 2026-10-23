package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "padre")
@Getter
@Setter
@NoArgsConstructor
public class Padre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dni;

    private String nombre;

    private String apellido;

    private String email;

    private String telefono;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "padre_alumno",
            joinColumns = @JoinColumn(name = "padre_id"),
            inverseJoinColumns = @JoinColumn(name = "alumno_id"))
    private Set<Alumno> alumnos = new HashSet<>();
}
