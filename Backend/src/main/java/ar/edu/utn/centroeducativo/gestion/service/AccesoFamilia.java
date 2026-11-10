package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Padre;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resuelve qué alumnos puede ver el usuario: el propio alumno o los hijos vinculados al padre. */
@Component
@RequiredArgsConstructor
public class AccesoFamilia {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;

    public Usuario usuario() {
        return usuarioActual.id()
                .flatMap(usuarioRepository::findById)
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida"));
    }

    public List<Alumno> alumnosVisibles() {
        Usuario usuario = usuario();
        if (usuario.getAlumno() != null) {
            return List.of(usuario.getAlumno());
        }
        if (usuario.getPadre() != null) {
            return List.copyOf(usuario.getPadre().getAlumnos());
        }
        return List.of();
    }

    public Alumno alumnoVisible(Long alumnoId) {
        return alumnosVisibles().stream()
                .filter(a -> a.getId().equals(alumnoId))
                .findFirst()
                .orElseThrow(() -> new NoAutorizadoException("El alumno no está vinculado a tu cuenta"));
    }

    public Padre padre() {
        Padre padre = usuario().getPadre();
        if (padre == null) {
            throw new NoAutorizadoException("Solo los padres pueden registrar pagos");
        }
        return padre;
    }
}
