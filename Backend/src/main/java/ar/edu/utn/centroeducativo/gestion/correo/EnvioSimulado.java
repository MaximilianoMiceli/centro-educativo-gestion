package ar.edu.utn.centroeducativo.gestion.correo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Modo sin envío: el correo queda guardado en el sistema, pero no sale hacia ningún servidor. */
@Component
@ConditionalOnProperty(name = "app.correo.modo", havingValue = "SIN_ENVIO", matchIfMissing = true)
public class EnvioSimulado implements CorreoSender {

    @Override
    public ResultadoEnvio enviar(CorreoSaliente correo) {
        return ResultadoEnvio.simulado();
    }
}
