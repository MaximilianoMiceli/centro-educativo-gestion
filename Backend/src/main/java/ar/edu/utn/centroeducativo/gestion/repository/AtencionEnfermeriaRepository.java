package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.AtencionEnfermeria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtencionEnfermeriaRepository extends JpaRepository<AtencionEnfermeria, Long> {

    List<AtencionEnfermeria> findByAlumno_IdOrderByFechaHoraDesc(Long alumnoId);
}
