package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocenteRepository extends JpaRepository<Docente, Long> {

    @Query("select c.id from Docente d join d.cursos c where d.id = :docenteId")
    Set<Long> findCursoIdsByDocenteId(@Param("docenteId") Long docenteId);
}
