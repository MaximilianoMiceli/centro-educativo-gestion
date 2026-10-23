package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.GrupoDeporte;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrupoDeporteRepository extends JpaRepository<GrupoDeporte, Long> {

    List<GrupoDeporte> findByDeporte_IdOrderByNivel_IdAscDiaSemanaAscHoraInicioAsc(Long deporteId);

    @Query("""
            select g from GrupoDeporte g join g.deporte d
            where d.docenteResponsable.id = :docenteId
              and d.id <> :deporteId
              and g.diaSemana = :dia
              and g.horaInicio < :fin
              and g.horaFin > :inicio
            """)
    List<GrupoDeporte> findConflictosDocenteEnOtrosDeportes(@Param("docenteId") Long docenteId,
                                                            @Param("deporteId") Long deporteId,
                                                            @Param("dia") Short dia,
                                                            @Param("inicio") LocalTime inicio,
                                                            @Param("fin") LocalTime fin);

    @Query("""
            select g from GrupoDeporte g join g.deporte d
            where d.docenteResponsable.id = :docenteId
              and g.diaSemana = :dia
              and g.horaInicio < :fin
              and g.horaFin > :inicio
            """)
    List<GrupoDeporte> findConflictosDocente(@Param("docenteId") Long docenteId,
                                             @Param("dia") Short dia,
                                             @Param("inicio") LocalTime inicio,
                                             @Param("fin") LocalTime fin);
}
