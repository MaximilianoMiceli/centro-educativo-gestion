package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Calificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByEvaluacion_Id(Long evaluacionId);

    Optional<Calificacion> findByEvaluacion_IdAndAlumno_Id(Long evaluacionId, Long alumnoId);

    List<Calificacion> findByAlumno_Id(Long alumnoId);
}
