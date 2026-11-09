package ar.edu.utn.centroeducativo.gestion.correo;

/**
 * Entrega un correo. Hoy solo existe la versión que no envía; un servicio real (por ejemplo Resend)
 * se agrega como otra implementación y se elige con la propiedad app.correo.modo.
 */
public interface CorreoSender {

    ResultadoEnvio enviar(CorreoSaliente correo);
}
