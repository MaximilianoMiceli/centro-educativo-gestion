package ar.edu.utn.centroeducativo.gestion.web.dto;

public final class SolicitudesServicios {

    private SolicitudesServicios() {
    }

    public record AsignarTransporte(Short recorridoId) {
    }

    public record AsignarComedor(boolean inscripto) {
    }
}
