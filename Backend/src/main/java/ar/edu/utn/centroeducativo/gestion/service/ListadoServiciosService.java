package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.AtencionEnfermeria;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionApoyo;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionIdioma;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.AtencionEnfermeriaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionApoyoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionIdiomaRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosServiciosDTO.AtencionListadoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosServiciosDTO.InscriptoApoyoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosServiciosDTO.InscriptoIdiomaDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listados globales de idiomas, apoyo estudiantil y enfermería. Respetan el criterio de acceso por curso
 * (un profesor solo ve los alumnos de sus cursos).
 */
@Service
@RequiredArgsConstructor
public class ListadoServiciosService {

    private static final Comparator<Alumno> ORDEN_ALUMNOS = Comparator
            .comparing(Alumno::getApellido)
            .thenComparing(Alumno::getNombre);

    private final InscripcionIdiomaRepository inscripcionIdiomaRepository;
    private final InscripcionApoyoRepository inscripcionApoyoRepository;
    private final AtencionEnfermeriaRepository atencionEnfermeriaRepository;
    private final AlumnoRepository alumnoRepository;
    private final AccesoAlumnos accesoAlumnos;

    @Transactional(readOnly = true)
    public List<InscriptoIdiomaDTO> idiomas(Short nivelId, Long cursoId, Short idiomaId) {
        return inscripcionIdiomaRepository.findAll().stream()
                .filter((InscripcionIdioma i) -> accesoAlumnos.criterio().test(i.getAlumno()))
                .filter(i -> coincide(i.getAlumno(), nivelId, cursoId))
                .filter(i -> idiomaId == null || i.getIdioma().getId().equals(idiomaId))
                .sorted(Comparator.comparing((InscripcionIdioma i) -> i.getAlumno(), ORDEN_ALUMNOS))
                .map(i -> new InscriptoIdiomaDTO(
                        i.getAlumno().getId(),
                        i.getAlumno().getLegajo(),
                        nombre(i.getAlumno()),
                        i.getAlumno().getCurso().getNivel().getNombreLegible(),
                        i.getAlumno().getCurso().getDescripcion(),
                        i.getIdioma().getNombre(),
                        i.getFechaInscripcion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscriptoApoyoDTO> apoyo(Short nivelId, Long cursoId) {
        Map<Long, Alumno> alumnos = alumnosPorId();
        return inscripcionApoyoRepository.findAll().stream()
                .filter((InscripcionApoyo a) -> alumnos.containsKey(a.getAlumnoId()))
                .filter(a -> accesoAlumnos.criterio().test(alumnos.get(a.getAlumnoId())))
                .filter(a -> coincide(alumnos.get(a.getAlumnoId()), nivelId, cursoId))
                .sorted(Comparator.comparing((InscripcionApoyo a) -> alumnos.get(a.getAlumnoId()), ORDEN_ALUMNOS))
                .map(a -> {
                    Alumno alumno = alumnos.get(a.getAlumnoId());
                    return new InscriptoApoyoDTO(
                            alumno.getId(),
                            alumno.getLegajo(),
                            nombre(alumno),
                            alumno.getCurso().getNivel().getNombreLegible(),
                            alumno.getCurso().getDescripcion(),
                            a.getFechaInicio(),
                            a.getObservaciones());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AtencionListadoDTO> enfermeria(Short nivelId, Long cursoId) {
        return atencionEnfermeriaRepository.findAll().stream()
                .filter((AtencionEnfermeria a) -> accesoAlumnos.criterio().test(a.getAlumno()))
                .filter(a -> coincide(a.getAlumno(), nivelId, cursoId))
                .sorted(Comparator.comparing((AtencionEnfermeria a) -> a.getFechaHora()).reversed())
                .map(a -> new AtencionListadoDTO(
                        a.getId(),
                        a.getAlumno().getId(),
                        a.getAlumno().getLegajo(),
                        nombre(a.getAlumno()),
                        a.getAlumno().getCurso().getNivel().getNombreLegible(),
                        a.getAlumno().getCurso().getDescripcion(),
                        a.getFechaHora(),
                        a.getMotivo(),
                        a.getObservaciones(),
                        a.getRegistradoPor() == null ? null : a.getRegistradoPor().getUsername()))
                .toList();
    }

    private Map<Long, Alumno> alumnosPorId() {
        return alumnoRepository.findAll().stream()
                .collect(Collectors.toMap(Alumno::getId, Function.identity()));
    }

    private static boolean coincide(Alumno alumno, Short nivelId, Long cursoId) {
        return (nivelId == null || alumno.getCurso().getNivel().getId().equals(nivelId))
                && (cursoId == null || alumno.getCurso().getId().equals(cursoId));
    }

    private static String nombre(Alumno alumno) {
        return alumno.getApellido() + ", " + alumno.getNombre();
    }
}
