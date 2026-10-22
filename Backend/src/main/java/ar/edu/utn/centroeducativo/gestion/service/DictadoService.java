package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.Dictado;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.Horario;
import ar.edu.utn.centroeducativo.gestion.domain.Materia;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.CursoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DictadoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.HorarioRepository;
import ar.edu.utn.centroeducativo.gestion.repository.MateriaRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearDictadoRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearHorarioRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.DictadoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.HorarioDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DictadoService {

    private static final short MIN_DIA = 1;
    private static final short MAX_DIA = 7;

    private final DictadoRepository dictadoRepository;
    private final HorarioRepository horarioRepository;
    private final DocenteRepository docenteRepository;
    private final MateriaRepository materiaRepository;
    private final CursoRepository cursoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<DictadoDTO> listarTodos() {
        return dictadoRepository.findAllByOrderByCurso_Nivel_IdAscCurso_AnioAscCurso_SeccionAscMateria_NombreAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DictadoDTO> listarPorDocente(Long docenteId) {
        return dictadoRepository.findByDocente_IdOrderByMateria_NombreAsc(docenteId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public DictadoDTO asignar(CrearDictadoRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        Docente docente = request.docenteId() == null ? null
                : docenteRepository.findById(request.docenteId()).orElse(null);
        Materia materia = request.materiaId() == null ? null
                : materiaRepository.findById(request.materiaId()).orElse(null);
        Curso curso = request.cursoId() == null ? null
                : cursoRepository.findById(request.cursoId()).orElse(null);
        if (docente == null) {
            errores.put("docenteId", "El docente seleccionado no existe");
        }
        if (materia == null) {
            errores.put("materiaId", "La materia seleccionada no existe");
        }
        if (curso == null) {
            errores.put("cursoId", "El curso seleccionado no existe");
        }
        if (errores.isEmpty() && dictadoRepository.existsByMateria_IdAndCurso_Id(materia.getId(), curso.getId())) {
            errores.put("cursoId", "Esa materia ya tiene docente asignado en ese curso");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en la asignación", errores);
        }

        Dictado dictado = new Dictado();
        dictado.setDocente(docente);
        dictado.setMateria(materia);
        dictado.setCurso(curso);
        dictadoRepository.save(dictado);
        docente.getCursos().add(curso);

        auditoriaService.registrar("ASIGNACION", "DICTADO", dictado.getId(),
                "Materia " + materia.getNombre() + " en " + curso.getDescripcion()
                        + " asignada a " + docente.getNombre() + " " + docente.getApellido());
        return toDTO(dictado);
    }

    @Transactional(readOnly = true)
    public List<HorarioDTO> listarHorarios(Long dictadoId) {
        return horarioRepository.findByDictado_IdOrderByDiaSemanaAscHoraInicioAsc(dictadoId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public HorarioDTO crearHorario(CrearHorarioRequest request) {
        Dictado dictado = request.dictadoId() == null ? null
                : dictadoRepository.findById(request.dictadoId()).orElse(null);
        if (dictado == null) {
            throw new ValidacionException("La asignación indicada no existe",
                    Map.of("dictadoId", "La asignación seleccionada no existe"));
        }

        Map<String, String> errores = new LinkedHashMap<>();
        if (request.diaSemana() == null || request.diaSemana() < MIN_DIA || request.diaSemana() > MAX_DIA) {
            errores.put("diaSemana", "El día debe estar entre 1 (lunes) y 7 (domingo)");
        }
        if (request.horaInicio() == null) {
            errores.put("horaInicio", "Ingresá la hora de inicio");
        }
        if (request.horaFin() == null) {
            errores.put("horaFin", "Ingresá la hora de fin");
        }
        if (errores.isEmpty() && !request.horaInicio().isBefore(request.horaFin())) {
            errores.put("horaFin", "La hora de fin debe ser posterior a la de inicio");
        }
        if (errores.isEmpty()) {
            List<Horario> conflictos = horarioRepository.findConflictos(request.diaSemana(),
                    request.horaInicio(), request.horaFin(),
                    dictado.getDocente().getId(), dictado.getCurso().getId());
            if (!conflictos.isEmpty()) {
                errores.put("horaInicio", "Conflicto de horario con " + describir(conflictos.get(0)));
            }
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el horario", errores);
        }

        Horario horario = new Horario();
        horario.setDictado(dictado);
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        horarioRepository.save(horario);

        auditoriaService.registrar("ALTA", "HORARIO", horario.getId(),
                "Horario " + dictado.getMateria().getNombre() + " en " + dictado.getCurso().getDescripcion()
                        + ": día " + request.diaSemana() + " de " + request.horaInicio() + " a " + request.horaFin());
        return toDTO(horario);
    }

    private static String describir(Horario horario) {
        Dictado dictado = horario.getDictado();
        return dictado.getMateria().getNombre() + " en " + dictado.getCurso().getDescripcion()
                + " (" + horario.getHoraInicio() + " a " + horario.getHoraFin() + ")";
    }

    private DictadoDTO toDTO(Dictado dictado) {
        Docente docente = dictado.getDocente();
        return new DictadoDTO(
                dictado.getId(),
                docente.getId(),
                docente.getApellido() + ", " + docente.getNombre(),
                dictado.getMateria().getId(),
                dictado.getMateria().getNombre(),
                dictado.getCurso().getId(),
                dictado.getCurso().getDescripcion());
    }

    private HorarioDTO toDTO(Horario horario) {
        Dictado dictado = horario.getDictado();
        return new HorarioDTO(
                horario.getId(),
                dictado.getId(),
                dictado.getMateria().getNombre(),
                dictado.getCurso().getDescripcion(),
                dictado.getDocente().getApellido() + ", " + dictado.getDocente().getNombre(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin());
    }
}
