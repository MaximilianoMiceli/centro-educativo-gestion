package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Evaluacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findByDictado_IdOrderByPeriodoAscFechaAsc(Long dictadoId);

    boolean existsByDictado_IdAndTituloIgnoreCase(Long dictadoId, String titulo);
}
