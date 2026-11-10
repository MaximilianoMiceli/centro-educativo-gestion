package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.EnvioCorreoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvioCorreoAdjuntoRepository extends JpaRepository<EnvioCorreoAdjunto, Long> {
}
