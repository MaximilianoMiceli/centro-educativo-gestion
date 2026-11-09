package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    /** Número del comprobante que emite el sistema. */
    private String numeroComprobante;

    private BigDecimal monto;

    private LocalDate fechaPago;

    private LocalDateTime registradoEn = LocalDateTime.now();

    private Long registradoPor;

    private String nombreArchivo;

    private String tipoContenido;

    @OneToMany(mappedBy = "pago")
    @OrderBy("id")
    private List<FacturaItem> items = new ArrayList<>();
}
