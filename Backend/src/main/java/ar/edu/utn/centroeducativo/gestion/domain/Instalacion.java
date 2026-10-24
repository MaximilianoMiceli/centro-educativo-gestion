package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instalacion")
@Getter
@Setter
@NoArgsConstructor
public class Instalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoInstalacion tipo;

    private String ubicacion;

    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    private EstadoInstalacion estado = EstadoInstalacion.ACTIVA;

    private String descripcion;
}
