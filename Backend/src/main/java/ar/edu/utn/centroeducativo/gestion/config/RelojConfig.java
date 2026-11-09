package ar.edu.utn.centroeducativo.gestion.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj en hora argentina, inyectable para poder fijar la fecha en las pruebas. */
@Configuration
public class RelojConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA);
    }
}
