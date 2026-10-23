package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.InscripcionComedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionComedorRepository extends JpaRepository<InscripcionComedor, Long> {
}
