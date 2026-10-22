package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccesoAlumnos {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final UsuarioActual usuarioActual;

    public Predicate<Alumno> criterio() {
        if (!usuarioActual.esProfesor()) {
            return alumno -> true;
        }
        Set<Long> cursosDelProfesor = cursosDelProfesorActual();
        return alumno -> cursosDelProfesor.contains(alumno.getCurso().getId());
    }

    public void verificar(Alumno alumno) {
        if (!criterio().test(alumno)) {
            throw new NoAutorizadoException("El alumno no pertenece a tus cursos");
        }
    }

    private Set<Long> cursosDelProfesorActual() {
        return usuarioActual.id()
                .flatMap(usuarioRepository::findById)
                .map(Usuario::getDocente)
                .map(docente -> docenteRepository.findCursoIdsByDocenteId(docente.getId()))
                .orElse(Set.of());
    }
}
