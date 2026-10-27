package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Calificacion;
import ar.edu.utn.centroeducativo.gestion.domain.Dictado;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.Evaluacion;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.CalificacionRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DictadoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.EvaluacionRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.CrearEvaluacionRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.DictadoCalificableDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.EvaluacionDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.EvaluacionNotaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.MateriaCalificacionesDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.NotaAlumnoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.NotaRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.PlanillaDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Evaluaciones y calificaciones. El profesor gestiona solo las materias que dicta; el administrador, todas.
 */
@Service
@RequiredArgsConstructor
public class CalificacionService {

    private static final Set<String> TIPOS = Set.of("EXAMEN", "TRABAJO_PRACTICO", "ORAL", "OTRA");
    private static final BigDecimal NOTA_MINIMA = BigDecimal.ONE;
    private static final BigDecimal NOTA_MAXIMA = BigDecimal.TEN;

    private final DictadoRepository dictadoRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final CalificacionRepository calificacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<DictadoCalificableDTO> dictados() {
        Usuario usuario = usuarioActual();
        List<Dictado> dictados;
        if (esAdmin(usuario)) {
            dictados = dictadoRepository.findAll();
        } else if (usuario.getDocente() != null) {
            dictados = dictadoRepository.findByDocente_IdOrderByMateria_NombreAsc(usuario.getDocente().getId());
        } else {
            dictados = List.of();
        }
        return dictados.stream()
                .sorted(Comparator.comparing((Dictado d) -> d.getCurso().getNivel().getId())
                        .thenComparing(d -> d.getCurso().getAnio())
                        .thenComparing(d -> d.getCurso().getSeccion())
                        .thenComparing(d -> d.getMateria().getNombre()))
                .map(d -> new DictadoCalificableDTO(d.getId(), d.getMateria().getNombre(), d.getCurso().getDescripcion(),
                        d.getCurso().getNivel().getNombreLegible(),
                        d.getDocente().getApellido() + ", " + d.getDocente().getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EvaluacionDTO> evaluaciones(Long dictadoId) {
        Dictado dictado = dictadoVisible(dictadoId);
        return evaluacionRepository.findByDictado_IdOrderByPeriodoAscFechaAsc(dictado.getId()).stream()
                .map(CalificacionService::toDTO)
                .toList();
    }

    @Transactional
    public EvaluacionDTO crearEvaluacion(Long dictadoId, CrearEvaluacionRequest request) {
        Dictado dictado = dictadoVisible(dictadoId);
        Map<String, String> errores = new LinkedHashMap<>();
        String titulo = request.titulo() == null ? null : request.titulo().strip();
        if (titulo == null || titulo.length() < 3 || titulo.length() > 100) {
            errores.put("titulo", "El nombre de la evaluación debe tener entre 3 y 100 caracteres");
        } else if (evaluacionRepository.existsByDictado_IdAndTituloIgnoreCase(dictadoId, titulo)) {
            errores.put("titulo", "Ya existe una evaluación con ese nombre en esta materia");
        }
        if (request.tipo() == null || !TIPOS.contains(request.tipo())) {
            errores.put("tipo", "Seleccioná el tipo de evaluación");
        }
        if (request.periodo() == null || request.periodo() < 1 || request.periodo() > 3) {
            errores.put("periodo", "El período debe ser 1, 2 o 3");
        }
        if (request.fecha() == null) {
            errores.put("fecha", "Ingresá la fecha de la evaluación");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo crear la evaluación", errores);
        }

        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setDictado(dictado);
        evaluacion.setTitulo(titulo);
        evaluacion.setTipo(request.tipo());
        evaluacion.setPeriodo(request.periodo());
        evaluacion.setFecha(request.fecha());
        evaluacionRepository.save(evaluacion);

        auditoriaService.registrar("ALTA", "EVALUACION", evaluacion.getId(),
                "Evaluación " + titulo + " de " + dictado.getMateria().getNombre() + " (" + dictado.getCurso().getDescripcion() + ")");
        return toDTO(evaluacion);
    }

    @Transactional(readOnly = true)
    public PlanillaDTO planilla(Long evaluacionId) {
        Evaluacion evaluacion = evaluacionVisible(evaluacionId);
        return construirPlanilla(evaluacion);
    }

    /** Carga o corrige las notas. Una nota vacía borra la calificación del alumno en esa evaluación. */
    @Transactional
    public PlanillaDTO guardarNotas(Long evaluacionId, List<NotaRequest> notas) {
        Evaluacion evaluacion = evaluacionVisible(evaluacionId);
        Map<Long, Alumno> delCurso = new HashMap<>();
        alumnoRepository.findByCurso_Id(evaluacion.getDictado().getCurso().getId())
                .forEach(a -> delCurso.put(a.getId(), a));

        Map<String, String> errores = new LinkedHashMap<>();
        for (NotaRequest nota : notas) {
            if (nota.alumnoId() == null || !delCurso.containsKey(nota.alumnoId())) {
                errores.put("alumnoId", "Hay un alumno que no pertenece a este curso");
            } else if (nota.nota() != null && (nota.nota().compareTo(NOTA_MINIMA) < 0 || nota.nota().compareTo(NOTA_MAXIMA) > 0)) {
                errores.put("nota", "Las notas deben estar entre 1 y 10");
            }
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudieron guardar las notas", errores);
        }

        int cargadas = 0;
        int borradas = 0;
        for (NotaRequest nota : notas) {
            Calificacion existente = calificacionRepository.findByEvaluacion_IdAndAlumno_Id(evaluacionId, nota.alumnoId()).orElse(null);
            if (nota.nota() == null) {
                if (existente != null) {
                    calificacionRepository.delete(existente);
                    borradas++;
                }
                continue;
            }
            BigDecimal valor = nota.nota().setScale(2, RoundingMode.HALF_UP);
            if (existente == null) {
                Calificacion nueva = new Calificacion();
                nueva.setEvaluacion(evaluacion);
                nueva.setAlumno(delCurso.get(nota.alumnoId()));
                nueva.setNota(valor);
                calificacionRepository.save(nueva);
            } else {
                existente.setNota(valor);
                calificacionRepository.save(existente);
            }
            cargadas++;
        }

        auditoriaService.registrar("MODIFICACION", "CALIFICACION", evaluacionId,
                "Notas de " + evaluacion.getTitulo() + ": " + cargadas + " cargadas, " + borradas + " quitadas");
        return construirPlanilla(evaluacion);
    }

    /** Calificaciones de un alumno, agrupadas por materia, con el promedio de cada una. */
    @Transactional(readOnly = true)
    public List<MateriaCalificacionesDTO> calificacionesDeAlumno(Alumno alumno) {
        Map<Long, Calificacion> porEvaluacion = new HashMap<>();
        calificacionRepository.findByAlumno_Id(alumno.getId())
                .forEach(c -> porEvaluacion.put(c.getEvaluacion().getId(), c));

        return dictadoRepository.findByCurso_IdOrderByMateria_NombreAsc(alumno.getCurso().getId()).stream()
                .map(dictado -> {
                    List<EvaluacionNotaDTO> evaluaciones = evaluacionRepository
                            .findByDictado_IdOrderByPeriodoAscFechaAsc(dictado.getId()).stream()
                            .map(e -> new EvaluacionNotaDTO(e.getTitulo(), e.getTipo(), e.getPeriodo(), e.getFecha(),
                                    notaDe(porEvaluacion.get(e.getId()))))
                            .toList();
                    return new MateriaCalificacionesDTO(
                            dictado.getMateria().getNombre(),
                            dictado.getDocente().getApellido() + ", " + dictado.getDocente().getNombre(),
                            promedio(evaluaciones),
                            evaluaciones);
                })
                .toList();
    }

    private PlanillaDTO construirPlanilla(Evaluacion evaluacion) {
        Map<Long, Calificacion> porAlumno = new HashMap<>();
        calificacionRepository.findByEvaluacion_Id(evaluacion.getId())
                .forEach(c -> porAlumno.put(c.getAlumno().getId(), c));

        List<NotaAlumnoDTO> alumnos = alumnoRepository.findByCurso_Id(evaluacion.getDictado().getCurso().getId()).stream()
                .sorted(Comparator.comparing(Alumno::getApellido).thenComparing(Alumno::getNombre))
                .map(a -> new NotaAlumnoDTO(a.getId(), a.getLegajo(), a.getApellido() + ", " + a.getNombre(),
                        notaDe(porAlumno.get(a.getId()))))
                .toList();

        return new PlanillaDTO(evaluacion.getId(), evaluacion.getTitulo(), evaluacion.getTipo(),
                evaluacion.getDictado().getMateria().getNombre(), evaluacion.getDictado().getCurso().getDescripcion(),
                evaluacion.getFecha(), alumnos);
    }

    private Dictado dictadoVisible(Long dictadoId) {
        Dictado dictado = dictadoRepository.findById(dictadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia con ID " + dictadoId + " no encontrada"));
        verificarAcceso(dictado.getDocente());
        return dictado;
    }

    private Evaluacion evaluacionVisible(Long evaluacionId) {
        Evaluacion evaluacion = evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación con ID " + evaluacionId + " no encontrada"));
        verificarAcceso(evaluacion.getDictado().getDocente());
        return evaluacion;
    }

    private void verificarAcceso(Docente docente) {
        Usuario usuario = usuarioActual();
        if (esAdmin(usuario)) {
            return;
        }
        if (usuario.getDocente() == null || !Objects.equals(usuario.getDocente().getId(), docente.getId())) {
            throw new NoAutorizadoException("Solo podés calificar las materias que dictás");
        }
    }

    private Usuario usuarioActual() {
        return usuarioActual.id()
                .flatMap(usuarioRepository::findById)
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida"));
    }

    private static boolean esAdmin(Usuario usuario) {
        return "ADMIN".equals(usuario.getRol().getNombre());
    }

    private static BigDecimal notaDe(Calificacion calificacion) {
        return calificacion == null ? null : calificacion.getNota();
    }

    private static BigDecimal promedio(List<EvaluacionNotaDTO> evaluaciones) {
        List<BigDecimal> notas = new ArrayList<>();
        evaluaciones.stream().map(EvaluacionNotaDTO::nota).filter(Objects::nonNull).forEach(notas::add);
        if (notas.isEmpty()) {
            return null;
        }
        BigDecimal suma = notas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private static EvaluacionDTO toDTO(Evaluacion e) {
        return new EvaluacionDTO(e.getId(), e.getTitulo(), e.getTipo(), e.getPeriodo(), e.getFecha());
    }
}
