package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Deporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeporteRepository extends JpaRepository<Deporte, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Deporte> findAllByOrderByNombreAsc();
}
