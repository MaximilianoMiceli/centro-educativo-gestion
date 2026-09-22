package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoSpecs;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoDTO;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lógica de negocio para alumnos.
 * Maneja búsquedas, conversiones a DTO y auditoría.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final AuditoriaService auditoriaService;

    /**
     * Busca un alumno por ID con su curso y nivel cargados.
     * Registra la búsqueda en auditoría.
     */
    public AlumnoDTO obtenerPorId(Long alumnoId) {
        Alumno alumno = alumnoRepository.findWithCursoById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con ID " + alumnoId + " no encontrado"));

        auditoriaService.registrar("CONSULTA", "ALUMNO", alumnoId,
                "Búsqueda por ID: " + alumnoId);

        return convertirADTO(alumno);
    }

    /**
     * Busca alumno por legajo exacto.
     */
    public AlumnoDTO buscarPorLegajo(String legajo) {
        Alumno alumno = alumnoRepository.findOne(AlumnoSpecs.legajoIgual(legajo))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con legajo " + legajo + " no encontrado"));

        auditoriaService.registrar("CONSULTA", "ALUMNO", alumno.getId(),
                "Búsqueda por legajo: " + legajo);

        return convertirADTO(alumno);
    }

    /**
     * Busca alumno por DNI exacto.
     */
    public AlumnoDTO buscarPorDni(String dni) {
        Alumno alumno = alumnoRepository.findOne(AlumnoSpecs.dniIgual(dni))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con DNI " + dni + " no encontrado"));

        auditoriaService.registrar("CONSULTA", "ALUMNO", alumno.getId(),
                "Búsqueda por DNI: " + dni);

        return convertirADTO(alumno);
    }

    /**
     * Busca alumnos por nombre o apellido (búsqueda parcial).
     * Para profesor, filtra solo los alumnos de sus cursos.
     * Para admin, devuelve todos.
     */
    public AlumnoDTO buscarPorNombre(String texto, Collection<Long> cursosDelProfesor) {
        Specification<Alumno> spec = AlumnoSpecs.nombreContiene(texto);

        if (!cursosDelProfesor.isEmpty()) {
            spec = spec.and(AlumnoSpecs.cursoEn(cursosDelProfesor));
        }

        Alumno alumno = alumnoRepository.findOne(spec)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno con nombre/apellido que contiene '" + texto + "' no encontrado"));

        auditoriaService.registrar("CONSULTA", "ALUMNO", alumno.getId(),
                "Búsqueda por nombre: " + texto);

        return convertirADTO(alumno);
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
