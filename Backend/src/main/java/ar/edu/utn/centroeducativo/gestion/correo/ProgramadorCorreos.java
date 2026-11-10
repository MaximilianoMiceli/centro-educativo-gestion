package ar.edu.utn.centroeducativo.gestion.correo;

import ar.edu.utn.centroeducativo.gestion.domain.TipoCorreo;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Corre todos los días a las 9:00 (hora argentina) y manda los correos que correspondan a la fecha.
 * Si el servidor está dormido a esa hora (por ejemplo en un plan gratuito de la nube), la tarea no corre:
 * en ese caso se dispara a mano desde POST /api/v1/correos/ejecutar.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.correo.programado", havingValue = "true", matchIfMissing = true)
public class ProgramadorCorreos {

    private static final Logger LOG = LoggerFactory.getLogger(ProgramadorCorreos.class);

    private final CorreosService correosService;
    private final Clock reloj;

    @Scheduled(cron = "${app.correo.cron:0 0 9 * * *}", zone = "America/Argentina/Buenos_Aires")
    public void ejecutarDelDia() {
        LocalDate hoy = LocalDate.now(reloj);
        for (TipoCorreo tipo : CalendarioCorreos.tareasDelDia(hoy)) {
            try {
                var resultado = correosService.ejecutar(tipo, hoy);
                LOG.info("Correos {} del {}: {} enviados, {} ya enviados, {} sin correo", tipo, hoy,
                        resultado.enviados(), resultado.yaEnviados(), resultado.sinCorreo());
            } catch (RuntimeException e) {
                LOG.error("No se pudieron preparar los correos {} del {}", tipo, hoy, e);
            }
        }
    }
}
