package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Dictado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DictadoRepository extends JpaRepository<Dictado, Long> {

    boolean existsByMateria_IdAndCurso_Id(Long materiaId, Long cursoId);

    List<Dictado> findByDocente_IdOrderByMateria_NombreAsc(Long docenteId);

    List<Dictado> findAllByOrderByCurso_Nivel_IdAscCurso_AnioAscCurso_SeccionAscMateria_NombreAsc();
}
