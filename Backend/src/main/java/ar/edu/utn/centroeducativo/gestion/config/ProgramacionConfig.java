package ar.edu.utn.centroeducativo.gestion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita las tareas programadas, como el envío mensual de correos a los padres. */
@Configuration
@EnableScheduling
public class ProgramacionConfig {
}
