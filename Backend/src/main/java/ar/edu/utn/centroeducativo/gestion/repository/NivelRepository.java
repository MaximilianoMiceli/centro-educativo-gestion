package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.NivelEducativo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NivelRepository extends JpaRepository<NivelEducativo, Short> {

    boolean existsByNombreIgnoreCase(String nombre);
}
