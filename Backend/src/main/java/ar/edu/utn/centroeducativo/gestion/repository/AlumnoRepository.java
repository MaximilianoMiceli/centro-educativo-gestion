package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlumnoRepository extends JpaRepository<Alumno, Long>, JpaSpecificationExecutor<Alumno> {

    @EntityGraph(attributePaths = {"curso", "curso.nivel"})
    Optional<Alumno> findWithCursoById(Long id);

    Optional<Alumno> findByDni(String dni);

    boolean existsByLegajoIgnoreCase(String legajo);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Alumno> findByCurso_Id(Long cursoId);
}
