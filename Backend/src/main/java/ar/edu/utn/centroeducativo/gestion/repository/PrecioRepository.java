package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.ConceptoPrecio;
import ar.edu.utn.centroeducativo.gestion.domain.Precio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrecioRepository extends JpaRepository<Precio, Long> {

    List<Precio> findAllByConcepto(ConceptoPrecio concepto);
}
