package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.time.LocalDateTime;

public final class ServiciosComplementariosDTO {

    private ServiciosComplementariosDTO() {
    }

    public record IdiomaDTO(Short id, String nombre) {
    }

    public record AtencionEnfermeriaDTO(
            Long id,
            LocalDateTime fechaHora,
            String motivo,
            String observaciones,
            String registradoPor
    ) {
    }

    public record InscribirIdioma(Short idiomaId) {
    }

    public record SolicitudApoyo(String observaciones) {
    }

    public record RegistrarAtencion(String motivo, String observaciones) {
    }

    public record RenombrarRecorrido(String nombre) {
    }
}
