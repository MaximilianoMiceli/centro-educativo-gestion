package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Archivo de la transferencia que el padre adjunta al pago. */
@Entity
@Table(name = "pago_archivo")
@Getter
@Setter
@NoArgsConstructor
public class PagoArchivo {

    @Id
    private Long pagoId;

    private byte[] contenido;
}
