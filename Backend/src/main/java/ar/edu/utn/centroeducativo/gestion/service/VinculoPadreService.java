package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Padre;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoResumenDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VinculoPadreService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<AlumnoResumenDTO> hijos(Long usuarioId) {
        Padre padre = buscarPadre(usuarioId);
        return padre.getAlumnos().stream()
                .sorted(Comparator.comparing(Alumno::getApellido).thenComparing(Alumno::getNombre))
                .map(a -> new AlumnoResumenDTO(a.getId(), a.getLegajo(), a.getApellido() + ", " + a.getNombre(),
                        a.getCurso().getDescripcion(), a.getCurso().getNivel().getNombreLegible()))
                .toList();
    }

    @Transactional
    public void vincular(Long usuarioId, List<Long> alumnoIds) {
        Padre padre = buscarPadre(usuarioId);
        List<Alumno> alumnos = alumnoRepository.findAllById(alumnoIds == null ? List.of() : alumnoIds);
        if (alumnos.size() != (alumnoIds == null ? 0 : alumnoIds.stream().distinct().count())) {
            throw new ValidacionException("No se pudieron vincular los alumnos",
                    Map.of("alumnoIds", "Alguno de los alumnos seleccionados no existe"));
        }
        padre.getAlumnos().clear();
        padre.getAlumnos().addAll(alumnos);

        auditoriaService.registrar("VINCULACION", "PADRE", padre.getId(),
                "Hijos vinculados: " + alumnos.stream().map(Alumno::getLegajo).sorted().toList());
    }

    private Padre buscarPadre(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + usuarioId + " no encontrado"));
        if (usuario.getPadre() == null) {
            throw new ValidacionException("El usuario no es un padre vinculado a una persona",
                    Map.of("usuarioId", "Solo los usuarios con rol Padre pueden tener hijos vinculados"));
        }
        return usuario.getPadre();
    }
}
