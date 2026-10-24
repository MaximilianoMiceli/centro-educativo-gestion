package ar.edu.utn.centroeducativo.gestion.reporte;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Dictado;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.GrupoDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionTransporte;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.CursoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DictadoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.GrupoDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.HorarioRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.MateriaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.NivelRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RecorridoTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.service.AlumnoAdministracionService;
import ar.edu.utn.centroeducativo.gestion.service.AlumnoServiciosService;
import ar.edu.utn.centroeducativo.gestion.service.FormatoHorario;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoFichaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoMateriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO;
import ar.edu.utn.centroeducativo.gestion.reporte.Reporte.Seccion;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SIN_DATO = "-";

    private final AlumnoRepository alumnoRepository;
    private final AlumnoAdministracionService alumnoAdministracionService;
    private final AlumnoServiciosService alumnoServiciosService;
    private final DictadoRepository dictadoRepository;
    private final HorarioRepository horarioRepository;
    private final DocenteRepository docenteRepository;
    private final GrupoDeporteRepository grupoDeporteRepository;
    private final InscripcionDeporteRepository inscripcionDeporteRepository;
    private final InscripcionTransporteRepository inscripcionTransporteRepository;
    private final DeporteRepository deporteRepository;
    private final RecorridoTransporteRepository recorridoRepository;
    private final NivelRepository nivelRepository;
    private final CursoRepository cursoRepository;
    private final MateriaRepository materiaRepository;

    @Transactional(readOnly = true)
    public Reporte generar(TipoReporte tipo, FiltrosReporte filtros) {
        validar(tipo, filtros);
        List<Seccion> secciones = switch (tipo) {
            case ALUMNO_FICHA -> fichaAlumno(filtros.alumnoId());
            case DOCENTE_CURSOS -> docenteCursos(filtros);
            case ALUMNOS_CURSO -> alumnosCurso(filtros);
            case ALUMNOS_MATERIA -> alumnosMateria(filtros);
            case DOCENTES_NIVEL -> docentesNivel(filtros);
            case ALUMNOS_DEPORTE, ALUMNOS_DEPORTE_NIVEL -> alumnosDeporte(filtros);
            case DEPORTES_DETALLE -> deportesDetalle(filtros);
            case ALUMNOS_TRANSPORTE -> alumnosTransporte(filtros);
        };
        return new Reporte(tipo.codigo(), tipo.titulo(), filtrosAplicados(filtros), secciones);
    }

    private static void validar(TipoReporte tipo, FiltrosReporte filtros) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (tipo == TipoReporte.ALUMNO_FICHA && filtros.alumnoId() == null) {
            errores.put("alumnoId", "Seleccioná un alumno");
        }
        if (tipo == TipoReporte.ALUMNOS_DEPORTE_NIVEL && filtros.nivelId() == null) {
            errores.put("nivelId", "Seleccioná un nivel");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Faltan datos para generar el reporte", errores);
        }
    }

    private List<Seccion> fichaAlumno(Long alumnoId) {
        AlumnoFichaDTO ficha = alumnoAdministracionService.ficha(alumnoId);
        List<AlumnoMateriaDTO> materias = alumnoAdministracionService.materias(alumnoId);
        AlumnoServiciosDTO servicios = alumnoServiciosService.obtener(alumnoId);

        return List.of(
                new Seccion("Datos personales", List.of("Campo", "Valor"), List.of(
                        List.of("Legajo", nz(ficha.legajo())),
                        List.of("DNI", nz(ficha.dni())),
                        List.of("Apellido y nombre", ficha.apellido() + ", " + ficha.nombre()),
                        List.of("Fecha de nacimiento", ficha.fechaNacimiento() == null ? SIN_DATO
                                : ficha.fechaNacimiento().format(FECHA)),
                        List.of("Domicilio", nz(ficha.domicilio())),
                        List.of("Teléfono", nz(ficha.telefono())),
                        List.of("Email", nz(ficha.email())),
                        List.of("Curso", nz(ficha.cursoDescripcion())),
                        List.of("Nivel", nz(ficha.nivelNombre())),
                        List.of("Estado", nz(ficha.estado())))),
                new Seccion("Materias cursadas", List.of("Materia", "Docente", "Horarios"), materias.stream()
                        .map(m -> List.of(nz(m.materia()), nz(m.docente()), String.join("; ", m.horarios())))
                        .toList()),
                new Seccion("Deportes", List.of("Deporte", "Horario"), servicios.deportes().stream()
                        .map(d -> List.of(nz(d.deporte()), nz(d.horario())))
                        .toList()),
                new Seccion("Transporte y comedor", List.of("Servicio", "Detalle"), List.of(
                        List.of("Transporte", servicios.transporte() == null ? "No inscripto"
                                : servicios.transporte().nombre()),
                        List.of("Comedor", servicios.comedor() ? "Inscripto" : "No inscripto"))));
    }

    private List<Seccion> docenteCursos(FiltrosReporte filtros) {
        List<List<String>> filas = dictados(filtros.docenteId(), filtros.nivelId(), null, null).stream()
                .map(d -> List.of(
                        persona(d.getDocente().getApellido(), d.getDocente().getNombre()),
                        d.getCurso().getNivel().getNombreLegible(),
                        d.getCurso().getDescripcion(),
                        d.getMateria().getNombre(),
                        horariosDe(d)))
                .toList();
        return List.of(new Seccion("Cursos y horarios por docente",
                List.of("Docente", "Nivel", "Curso", "Materia", "Horarios"), filas));
    }

    private List<Seccion> alumnosCurso(FiltrosReporte filtros) {
        List<List<String>> filas = alumnos(filtros.nivelId(), filtros.cursoId()).stream()
                .map(a -> List.of(
                        a.getCurso().getNivel().getNombreLegible(),
                        a.getCurso().getDescripcion(),
                        a.getLegajo(),
                        a.getApellido(),
                        a.getNombre()))
                .toList();
        return List.of(new Seccion("Alumnos por curso",
                List.of("Nivel", "Curso", "Legajo", "Apellido", "Nombre"), filas));
    }

    private List<Seccion> alumnosMateria(FiltrosReporte filtros) {
        List<List<String>> filas = new ArrayList<>();
        for (Dictado dictado : dictados(null, filtros.nivelId(), filtros.cursoId(), filtros.materiaId())) {
            for (Alumno alumno : alumnos(null, dictado.getCurso().getId())) {
                filas.add(List.of(
                        dictado.getCurso().getNivel().getNombreLegible(),
                        dictado.getCurso().getDescripcion(),
                        dictado.getMateria().getNombre(),
                        persona(dictado.getDocente().getApellido(), dictado.getDocente().getNombre()),
                        persona(alumno.getApellido(), alumno.getNombre()),
                        alumno.getLegajo()));
            }
        }
        return List.of(new Seccion("Alumnos por materia",
                List.of("Nivel", "Curso", "Materia", "Profesor", "Alumno", "Legajo"), filas));
    }

    private List<Seccion> docentesNivel(FiltrosReporte filtros) {
        Map<String, List<Dictado>> porDocenteNivel = dictados(null, filtros.nivelId(), null, null).stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDocente().getId() + ":" + d.getCurso().getNivel().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<List<String>> filas = porDocenteNivel.values().stream()
                .map(grupo -> {
                    Dictado primero = grupo.get(0);
                    return List.of(
                            persona(primero.getDocente().getApellido(), primero.getDocente().getNombre()),
                            primero.getCurso().getNivel().getNombreLegible(),
                            grupo.stream().map(d -> d.getMateria().getNombre()).distinct().sorted()
                                    .collect(Collectors.joining(", ")),
                            grupo.stream().map(d -> d.getCurso().getDescripcion()).distinct().sorted()
                                    .collect(Collectors.joining(", ")));
                })
                .toList();
        return List.of(new Seccion("Docentes por nivel",
                List.of("Profesor", "Nivel", "Materias", "Cursos"), filas));
    }

    private List<Seccion> alumnosDeporte(FiltrosReporte filtros) {
        List<List<String>> filas = inscripcionDeporteRepository.findAll().stream()
                .filter(i -> filtros.deporteId() == null
                        || i.getGrupo().getDeporte().getId().equals(filtros.deporteId()))
                .filter(i -> filtros.nivelId() == null
                        || i.getAlumno().getCurso().getNivel().getId().equals(filtros.nivelId()))
                .sorted(Comparator.comparing((InscripcionDeporte i) -> i.getGrupo().getDeporte().getNombre())
                        .thenComparing(i -> i.getAlumno().getApellido())
                        .thenComparing(i -> i.getAlumno().getNombre()))
                .map(i -> List.of(
                        i.getGrupo().getDeporte().getNombre(),
                        persona(i.getAlumno().getApellido(), i.getAlumno().getNombre()),
                        i.getAlumno().getLegajo(),
                        i.getAlumno().getCurso().getDescripcion(),
                        i.getAlumno().getCurso().getNivel().getNombreLegible()))
                .toList();
        return List.of(new Seccion("Alumnos inscriptos en deportes",
                List.of("Deporte", "Alumno", "Legajo", "Curso", "Nivel"), filas));
    }

    private List<Seccion> deportesDetalle(FiltrosReporte filtros) {
        Map<Long, List<Alumno>> alumnosPorGrupo = inscripcionDeporteRepository.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getGrupo().getId(),
                        Collectors.mapping(InscripcionDeporte::getAlumno, Collectors.toList())));
        List<List<String>> filas = grupoDeporteRepository.findAll().stream()
                .filter(g -> filtros.deporteId() == null || g.getDeporte().getId().equals(filtros.deporteId()))
                .filter(g -> filtros.nivelId() == null || g.getNivel().getId().equals(filtros.nivelId()))
                .sorted(Comparator.comparing((GrupoDeporte g) -> g.getDeporte().getNombre())
                        .thenComparing(g -> g.getNivel().getId())
                        .thenComparing(GrupoDeporte::getDiaSemana)
                        .thenComparing(GrupoDeporte::getHoraInicio))
                .map(g -> {
                    List<Alumno> alumnos = alumnosPorGrupo.getOrDefault(g.getId(), List.of());
                    Docente responsable = g.getDeporte().getDocenteResponsable();
                    return List.of(
                            g.getDeporte().getNombre(),
                            g.getNivel().getNombreLegible(),
                            FormatoHorario.rango(g.getDiaSemana(), g.getHoraInicio(), g.getHoraFin()),
                            responsable == null ? "Sin responsable"
                                    : persona(responsable.getApellido(), responsable.getNombre()),
                            String.valueOf(alumnos.size()),
                            alumnos.isEmpty() ? "Sin alumnos" : alumnos.stream()
                                    .map(a -> persona(a.getApellido(), a.getNombre()))
                                    .sorted()
                                    .collect(Collectors.joining("; ")));
                })
                .toList();
        return List.of(new Seccion("Detalle de deportes",
                List.of("Deporte", "Nivel", "Horario", "Responsable", "Cantidad", "Alumnos"), filas));
    }

    private List<Seccion> alumnosTransporte(FiltrosReporte filtros) {
        Map<Long, Alumno> alumnosPorId = alumnoRepository.findAll().stream()
                .collect(Collectors.toMap(Alumno::getId, Function.identity()));
        List<List<String>> filas = inscripcionTransporteRepository.findAll().stream()
                .filter(i -> filtros.recorridoId() == null
                        || i.getRecorrido().getId().equals(filtros.recorridoId()))
                .filter(i -> alumnosPorId.containsKey(i.getAlumnoId()))
                .sorted(Comparator.comparing((InscripcionTransporte i) -> i.getRecorrido().getNombre())
                        .thenComparing(i -> alumnosPorId.get(i.getAlumnoId()).getApellido())
                        .thenComparing(i -> alumnosPorId.get(i.getAlumnoId()).getNombre()))
                .map(i -> {
                    Alumno alumno = alumnosPorId.get(i.getAlumnoId());
                    return List.of(
                            i.getRecorrido().getNombre(),
                            persona(alumno.getApellido(), alumno.getNombre()),
                            alumno.getLegajo(),
                            alumno.getCurso().getDescripcion(),
                            alumno.getCurso().getNivel().getNombreLegible());
                })
                .toList();
        return List.of(new Seccion("Alumnos por recorrido de transporte",
                List.of("Recorrido", "Alumno", "Legajo", "Curso", "Nivel"), filas));
    }

    /** Dictados filtrados, en el orden que ya define el repositorio (nivel, curso, materia). */
    private List<Dictado> dictados(Long docenteId, Short nivelId, Long cursoId, Long materiaId) {
        return dictadoRepository.findAllByOrderByCurso_Nivel_IdAscCurso_AnioAscCurso_SeccionAscMateria_NombreAsc()
                .stream()
                .filter(d -> docenteId == null || d.getDocente().getId().equals(docenteId))
                .filter(d -> nivelId == null || d.getCurso().getNivel().getId().equals(nivelId))
                .filter(d -> cursoId == null || d.getCurso().getId().equals(cursoId))
                .filter(d -> materiaId == null || d.getMateria().getId().equals(materiaId))
                .toList();
    }

    /** Alumnos filtrados, ordenados por nivel, curso y apellido. */
    private List<Alumno> alumnos(Short nivelId, Long cursoId) {
        return alumnoRepository.findAll().stream()
                .filter(a -> nivelId == null || a.getCurso().getNivel().getId().equals(nivelId))
                .filter(a -> cursoId == null || a.getCurso().getId().equals(cursoId))
                .sorted(Comparator.comparing((Alumno a) -> a.getCurso().getNivel().getId())
                        .thenComparing(a -> a.getCurso().getAnio())
                        .thenComparing(a -> a.getCurso().getSeccion())
                        .thenComparing(Alumno::getApellido)
                        .thenComparing(Alumno::getNombre))
                .toList();
    }

    private String horariosDe(Dictado dictado) {
        List<String> horarios = horarioRepository.findByDictado_IdOrderByDiaSemanaAscHoraInicioAsc(dictado.getId())
                .stream()
                .map(FormatoHorario::formatear)
                .toList();
        return horarios.isEmpty() ? "Sin horarios" : String.join("; ", horarios);
    }

    private List<String> filtrosAplicados(FiltrosReporte filtros) {
        List<String> aplicados = new ArrayList<>();
        if (filtros.alumnoId() != null) {
            alumnoRepository.findById(filtros.alumnoId()).ifPresent(a -> aplicados.add(
                    "Alumno: " + persona(a.getApellido(), a.getNombre()) + " (" + a.getLegajo() + ")"));
        }
        if (filtros.docenteId() != null) {
            docenteRepository.findById(filtros.docenteId()).ifPresent(d -> aplicados.add(
                    "Docente: " + persona(d.getApellido(), d.getNombre())));
        }
        if (filtros.nivelId() != null) {
            nivelRepository.findById(filtros.nivelId()).ifPresent(n -> aplicados.add(
                    "Nivel: " + n.getNombreLegible()));
        }
        if (filtros.cursoId() != null) {
            cursoRepository.findById(filtros.cursoId()).ifPresent(c -> aplicados.add(
                    "Curso: " + c.getDescripcion()));
        }
        if (filtros.materiaId() != null) {
            materiaRepository.findById(filtros.materiaId()).ifPresent(m -> aplicados.add(
                    "Materia: " + m.getNombre()));
        }
        if (filtros.deporteId() != null) {
            deporteRepository.findById(filtros.deporteId()).ifPresent(d -> aplicados.add(
                    "Deporte: " + d.getNombre()));
        }
        if (filtros.recorridoId() != null) {
            recorridoRepository.findById(filtros.recorridoId()).ifPresent(r -> aplicados.add(
                    "Recorrido: " + r.getNombre()));
        }
        return aplicados;
    }

    private static String persona(String apellido, String nombre) {
        return Objects.toString(apellido, "") + ", " + Objects.toString(nombre, "");
    }

    private static String nz(String valor) {
        return valor == null || valor.isBlank() ? SIN_DATO : valor;
    }
}
