package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Horario;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByDictado_IdOrderByDiaSemanaAscHoraInicioAsc(Long dictadoId);

    @Query("""
            select h from Horario h join h.dictado d
            where h.diaSemana = :dia
              and h.horaInicio < :fin
              and h.horaFin > :inicio
              and (d.docente.id = :docenteId or d.curso.id = :cursoId)
            """)
    List<Horario> findConflictos(@Param("dia") Short dia,
                                 @Param("inicio") LocalTime inicio,
                                 @Param("fin") LocalTime fin,
                                 @Param("docenteId") Long docenteId,
                                 @Param("cursoId") Long cursoId);
}
