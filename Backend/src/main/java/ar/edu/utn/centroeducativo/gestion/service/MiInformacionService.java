package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.EstadoAlumno;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoResumenDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.MateriaCalificacionesDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.MiInformacionDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MiInformacionService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
    private final AlumnoAdministracionService alumnoAdministracionService;
    private final AlumnoServiciosService alumnoServiciosService;
    private final CalificacionService calificacionService;

    @Transactional(readOnly = true)
    public List<AlumnoResumenDTO> alumnos() {
        return alumnosVisibles().stream()
                .sorted(Comparator.comparing(Alumno::getApellido).thenComparing(Alumno::getNombre))
                .map(a -> new AlumnoResumenDTO(a.getId(), a.getLegajo(), a.getApellido() + ", " + a.getNombre(),
                        a.getCurso().getDescripcion(), a.getCurso().getNivel().getNombreLegible()))
                .toList();
    }

    @Transactional(readOnly = true)
    public MiInformacionDTO detalle(Long alumnoId) {
        boolean visible = alumnosVisibles().stream().anyMatch(a -> a.getId().equals(alumnoId));
        if (!visible) {
            throw new NoAutorizadoException("El alumno no está vinculado a tu cuenta");
        }
        return new MiInformacionDTO(
                alumnoAdministracionService.ficha(alumnoId),
                alumnoAdministracionService.materias(alumnoId),
                alumnoServiciosService.obtener(alumnoId));
    }

    /** Calificaciones del alumno visible para el usuario (él mismo o un hijo vinculado). */
    @Transactional(readOnly = true)
    public List<MateriaCalificacionesDTO> calificaciones(Long alumnoId) {
        Alumno alumno = alumnosVisibles().stream()
                .filter(a -> a.getId().equals(alumnoId))
                .findFirst()
                .orElseThrow(() -> new NoAutorizadoException("El alumno no está vinculado a tu cuenta"));
        return calificacionService.calificacionesDeAlumno(alumno);
    }

    /** Servicios del propio alumno, para que los gestione desde su perfil. */
    @Transactional(readOnly = true)
    public AlumnoServiciosDTO serviciosPropios() {
        return alumnoServiciosService.obtener(alumnoPropio().getId());
    }

    @Transactional
    public AlumnoServiciosDTO inscribirDeporte(Long grupoId) {
        return alumnoServiciosService.inscribirDeporte(alumnoActivoPropio().getId(), grupoId);
    }

    @Transactional
    public AlumnoServiciosDTO quitarDeporte(Long inscripcionId) {
        return alumnoServiciosService.quitarDeporte(alumnoActivoPropio().getId(), inscripcionId);
    }

    @Transactional
    public AlumnoServiciosDTO asignarTransporte(Short recorridoId) {
        return alumnoServiciosService.asignarTransporte(alumnoActivoPropio().getId(), recorridoId);
    }

    @Transactional
    public AlumnoServiciosDTO quitarTransporte() {
        return alumnoServiciosService.quitarTransporte(alumnoActivoPropio().getId());
    }

    @Transactional
    public AlumnoServiciosDTO asignarComedor(boolean inscripto) {
        return alumnoServiciosService.asignarComedor(alumnoActivoPropio().getId(), inscripto);
    }

    @Transactional
    public AlumnoServiciosDTO inscribirIdioma(Short idiomaId) {
        return alumnoServiciosService.inscribirIdioma(alumnoActivoPropio().getId(), idiomaId);
    }

    @Transactional
    public AlumnoServiciosDTO quitarIdioma(Long inscripcionId) {
        return alumnoServiciosService.quitarIdioma(alumnoActivoPropio().getId(), inscripcionId);
    }

    /** Solo el alumno puede gestionar sus servicios; el padre y el resto de los roles no. */
    private Alumno alumnoPropio() {
        Usuario usuario = usuarioActual.id()
                .flatMap(usuarioRepository::findById)
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida"));
        if (usuario.getAlumno() == null) {
            throw new NoAutorizadoException("Solo el alumno puede gestionar sus propios servicios");
        }
        return usuario.getAlumno();
    }

    /** Las inscripciones nuevas las puede hacer solo un alumno activo. */
    private Alumno alumnoActivoPropio() {
        Alumno alumno = alumnoPropio();
        if (alumno.getEstado() != EstadoAlumno.ACTIVO) {
            throw new ValidacionException("No se pudo completar la operación",
                    Map.of("estado", "Solo los alumnos activos pueden gestionar sus servicios"));
        }
        return alumno;
    }

    private List<Alumno> alumnosVisibles() {
        Usuario usuario = usuarioActual.id()
                .flatMap(usuarioRepository::findById)
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida"));
        if (usuario.getAlumno() != null) {
            return List.of(usuario.getAlumno());
        }
        if (usuario.getPadre() != null) {
            return List.copyOf(usuario.getPadre().getAlumnos());
        }
        return List.of();
    }
}
