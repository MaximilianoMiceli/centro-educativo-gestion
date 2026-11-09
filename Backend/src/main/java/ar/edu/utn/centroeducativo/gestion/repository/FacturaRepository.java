package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @EntityGraph(attributePaths = {"alumno", "items"})
    Optional<Factura> findWithDetalleById(Long id);

    @EntityGraph(attributePaths = {"alumno", "items"})
    @Query("select f from Factura f where f.alumno.id = :alumnoId and f.emision between :desde and :hasta "
            + "order by f.periodo desc, f.id desc")
    List<Factura> findDelAlumno(@Param("alumnoId") Long alumnoId, @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("select f.alumno.id from Factura f where f.periodo = :periodo")
    Set<Long> findAlumnoIdsConFactura(@Param("periodo") String periodo);

    /** Bloquea la factura mientras se registra un pago, para que dos pagos no cubran el mismo ítem. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.id = :id")
    Optional<Factura> bloquearPorId(@Param("id") Long id);
}
