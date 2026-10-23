package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.Dictado;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.DictadoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.HorarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteAgendaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteAgendaDTO.CursoAgendaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteAgendaDTO.MateriaAgendaDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocenteAgendaService {

    private static final Comparator<Dictado> ORDEN_CURSOS = Comparator
            .comparing((Dictado d) -> d.getCurso().getNivel().getId())
            .thenComparing(d -> d.getCurso().getAnio())
            .thenComparing(d -> d.getCurso().getSeccion())
            .thenComparing(d -> d.getMateria().getNombre());

    private final DocenteRepository docenteRepository;
    private final DictadoRepository dictadoRepository;
    private final HorarioRepository horarioRepository;

    @Transactional(readOnly = true)
    public DocenteAgendaDTO agenda(Long docenteId) {
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente con ID " + docenteId + " no encontrado"));

        List<Dictado> dictados = dictadoRepository.findByDocente_IdOrderByMateria_NombreAsc(docenteId).stream()
                .sorted(ORDEN_CURSOS)
                .toList();

        Map<Long, Curso> cursos = new HashMap<>();
        Map<Long, List<MateriaAgendaDTO>> materiasPorCurso = new LinkedHashMap<>();
        for (Dictado dictado : dictados) {
            Curso curso = dictado.getCurso();
            cursos.putIfAbsent(curso.getId(), curso);
            List<String> horarios = horarioRepository
                    .findByDictado_IdOrderByDiaSemanaAscHoraInicioAsc(dictado.getId()).stream()
                    .map(FormatoHorario::formatear)
                    .toList();
            materiasPorCurso.computeIfAbsent(curso.getId(), id -> new ArrayList<>())
                    .add(new MateriaAgendaDTO(dictado.getMateria().getNombre(), horarios));
        }

        List<CursoAgendaDTO> cursosAgenda = materiasPorCurso.entrySet().stream()
                .map(entrada -> {
                    Curso curso = cursos.get(entrada.getKey());
                    return new CursoAgendaDTO(curso.getId(), curso.getDescripcion(),
                            curso.getNivel().getNombreLegible(), entrada.getValue());
                })
                .toList();

        return new DocenteAgendaDTO(docente.getId(), docente.getApellido() + ", " + docente.getNombre(), cursosAgenda);
    }
}
