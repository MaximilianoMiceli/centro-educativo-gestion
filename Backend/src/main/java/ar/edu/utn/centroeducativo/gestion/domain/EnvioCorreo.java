package ar.edu.utn.centroeducativo.gestion.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "envio_correo")
@Getter
@Setter
@NoArgsConstructor
public class EnvioCorreo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoCorreo tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "padre_id")
    private Padre padre;

    /** Mes en el que se envía, con formato AAAA-MM. */
    private String periodo;

    private String destinatario;

    private String asunto;

    private String cuerpo;

    @Enumerated(EnumType.STRING)
    private EstadoCorreo estado;

    private String detalleError;

    private LocalDateTime creadoEn = LocalDateTime.now();

    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<EnvioCorreoAdjunto> adjuntos = new ArrayList<>();
}
