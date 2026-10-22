package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByNivel_IdAndAnioAndSeccion(Short nivelId, Short anio, String seccion);

    List<Curso> findByNivel_IdOrderByAnioAscSeccionAsc(Short nivelId);

    List<Curso> findAllByOrderByNivel_IdAscAnioAscSeccionAsc();
}
