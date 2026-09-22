package ar.edu.utn.centroeducativo.gestion.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoriaDTO {

    private Long id;

    private Long usuarioId;

    private String accion;

    private String entidad;

    private Long entidadId;

    private String detalle;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;
}
