package ar.edu.utn.centroeducativo.gestion.correo;

import java.util.List;

/** Un correo listo para enviar, con sus adjuntos. */
public record CorreoSaliente(String destinatario, String asunto, String cuerpo, List<Adjunto> adjuntos) {

    public record Adjunto(String nombre, byte[] contenido) {
    }
}
