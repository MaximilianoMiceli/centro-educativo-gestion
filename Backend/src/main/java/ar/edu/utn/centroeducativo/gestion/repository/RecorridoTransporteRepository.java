package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.RecorridoTransporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecorridoTransporteRepository extends JpaRepository<RecorridoTransporte, Short> {

    List<RecorridoTransporte> findAllByOrderByIdAsc();
}
