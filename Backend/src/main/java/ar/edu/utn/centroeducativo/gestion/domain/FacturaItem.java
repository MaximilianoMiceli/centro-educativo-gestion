package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "factura_item")
@Getter
@Setter
@NoArgsConstructor
public class FacturaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @Enumerated(EnumType.STRING)
    private ConceptoPrecio concepto;

    private String descripcion;

    /** Id del deporte o del recorrido al que corresponde el ítem; null para la cuota base y el comedor. */
    private Long referenciaId;

    private BigDecimal monto;

    /** Pago que cubrió este ítem; null mientras esté pendiente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;
}
