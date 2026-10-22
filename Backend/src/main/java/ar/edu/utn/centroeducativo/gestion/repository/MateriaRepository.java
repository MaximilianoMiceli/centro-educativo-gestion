package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Materia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Materia> findAllByOrderByNombreAsc();
}
