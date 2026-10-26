package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.InscripcionIdioma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionIdiomaRepository extends JpaRepository<InscripcionIdioma, Long> {

    List<InscripcionIdioma> findByAlumno_IdOrderByIdioma_NombreAsc(Long alumnoId);

    boolean existsByAlumno_IdAndIdioma_Id(Long alumnoId, Short idiomaId);
}
