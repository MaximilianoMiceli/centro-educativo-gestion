package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoResumenDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.MiInformacionDTO;
import java.util.Comparator;
import java.util.List;
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
