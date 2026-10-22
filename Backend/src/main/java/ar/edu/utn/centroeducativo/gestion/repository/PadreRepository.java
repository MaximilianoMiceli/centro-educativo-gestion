package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Padre;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PadreRepository extends JpaRepository<Padre, Long> {

    Optional<Padre> findByDni(String dni);
}
