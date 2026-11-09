package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CorreoDTO(
        Long id,
        String tipo,
        String periodo,
        Long padreId,
        String padre,
        String destinatario,
        String asunto,
        /** SIMULADO, ENVIADO o ERROR. */
        String estado,
        String detalleError,
        LocalDateTime creadoEn,
        /** Solo viene en el detalle de un correo. */
        String cuerpo,
        List<Adjunto> adjuntos
) {

    public record Adjunto(Long id, String nombre) {
    }
}
