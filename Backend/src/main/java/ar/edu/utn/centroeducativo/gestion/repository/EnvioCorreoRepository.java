package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.EnvioCorreo;
import ar.edu.utn.centroeducativo.gestion.domain.TipoCorreo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnvioCorreoRepository extends JpaRepository<EnvioCorreo, Long> {

    @EntityGraph(attributePaths = {"padre"})
    List<EnvioCorreo> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"padre", "adjuntos"})
    Optional<EnvioCorreo> findWithAdjuntosById(Long id);

    @EntityGraph(attributePaths = {"adjuntos"})
    @Query("select e from EnvioCorreo e where e.tipo = :tipo and e.periodo = :periodo")
    List<EnvioCorreo> findDelPeriodo(@Param("tipo") TipoCorreo tipo, @Param("periodo") String periodo);
}
