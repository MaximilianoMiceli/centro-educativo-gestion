package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoSpecs;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoDTO;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lógica de negocio para alumnos.
 * Maneja búsquedas, control de acceso por rol, conversiones a DTO y auditoría.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final UsuarioActual usuarioActual;

    public AlumnoDTO obtenerPorId(Long alumnoId) {
        Alumno alumno = alumnoRepository.findWithCursoById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con ID " + alumnoId + " no encontrado"));

        verificarAcceso(alumno);
        auditoriaService.registrar("CONSULTA", "ALUMNO", alumnoId,
                "Búsqueda por ID: " + alumnoId);

        return convertirADTO(alumno);
    }

    public AlumnoDTO buscarPorLegajo(String legajo) {
        Alumno alumno = alumnoRepository.findOne(AlumnoSpecs.legajoIgual(legajo))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con legajo " + legajo + " no encontrado"));

        verificarAcceso(alumno);
        auditoriaService.registrar("CONSULTA", "ALUMNO", alumno.getId(),
                "Búsqueda por legajo: " + legajo);

        return convertirADTO(alumno);
    }

    public AlumnoDTO buscarPorDni(String dni) {
        Alumno alumno = alumnoRepository.findOne(AlumnoSpecs.dniIgual(dni))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con DNI " + dni + " no encontrado"));

        verificarAcceso(alumno);
        auditoriaService.registrar("CONSULTA", "ALUMNO", alumno.getId(),
                "Búsqueda por DNI: " + dni);

        return convertirADTO(alumno);
    }

    /**
     * Busca alumnos por nombre o apellido (parcial).
     * Para profesor, devuelve solo los alumnos de sus cursos; si hay coincidencias
     * pero ninguna es suya, responde 403.
     */
    public List<AlumnoDTO> buscarPorNombre(String texto) {
        List<Alumno> coincidencias = alumnoRepository.findAll(AlumnoSpecs.nombreContiene(texto));
        if (coincidencias.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "Alumno con nombre/apellido que contiene '" + texto + "' no encontrado");
        }

        Predicate<Alumno> tieneAcceso = criterioDeAcceso();
        List<Alumno> permitidos = coincidencias.stream().filter(tieneAcceso).toList();
        if (permitidos.isEmpty()) {
            throw new NoAutorizadoException("Los alumnos encontrados no pertenecen a tus cursos");
        }

        permitidos.forEach(alumno -> auditoriaService.registrar("CONSULTA", "ALUMNO", alumno.getId(),
                "Búsqueda por nombre: " + texto));

        return permitidos.stream().map(this::convertirADTO).toList();
    }

    private void verificarAcceso(Alumno alumno) {
        if (!criterioDeAcceso().test(alumno)) {
            throw new NoAutorizadoException("El alumno no pertenece a tus cursos");
        }
    }

    private Predicate<Alumno> criterioDeAcceso() {
        if (!usuarioActual.esProfesor()) {
            return alumno -> true;
        }
        Set<Long> cursosDelProfesor = cursosDelProfesorActual();
        return alumno -> cursosDelProfesor.contains(alumno.getCurso().getId());
    }

    private Set<Long> cursosDelProfesorActual() {
        return usuarioActual.id()
                .flatMap(usuarioRepository::findById)
                .map(Usuario::getDocente)
                .map(docente -> docenteRepository.findCursoIdsByDocenteId(docente.getId()))
                .orElse(Set.of());
    }

    private AlumnoDTO convertirADTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setId(alumno.getId());
        dto.setLegajo(alumno.getLegajo());
        dto.setDni(alumno.getDni());
        dto.setNombre(alumno.getNombre());
        dto.setApellido(alumno.getApellido());
        dto.setFechaNacimiento(alumno.getFechaNacimiento());
        dto.setDomicilio(alumno.getDomicilio());
        dto.setTelefono(alumno.getTelefono());
        dto.setEmail(alumno.getEmail());
        dto.setCursoId(alumno.getCurso().getId());
        dto.setCursoDescripcion(alumno.getCurso().getDescripcion());
        dto.setNivelNombre(alumno.getCurso().getNivel().getNombre());
        dto.setEstado(alumno.getEstado().toString());
        return dto;
    }
}
