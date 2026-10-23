package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.InscripcionDeporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionDeporteRepository extends JpaRepository<InscripcionDeporte, Long> {

    List<InscripcionDeporte> findByAlumno_Id(Long alumnoId);

    boolean existsByAlumno_IdAndGrupo_Deporte_Id(Long alumnoId, Long deporteId);
}
