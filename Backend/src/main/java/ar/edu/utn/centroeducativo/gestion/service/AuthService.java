package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.JwtTokenProvider;
import ar.edu.utn.centroeducativo.gestion.web.dto.LoginResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int LARGO_MINIMO_PASSWORD = 8;
    private static final int LARGO_MAXIMO_PASSWORD = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Optional<LoginResponse> login(String username, String password) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(Usuario::isActivo)
                .filter(usuario -> passwordEncoder.matches(password, usuario.getPasswordHash()))
                .map(this::generarRespuesta);
    }

    @Transactional
    public LoginResponse cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + usuarioId + " no encontrado"));

        Map<String, String> errores = new LinkedHashMap<>();
        if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            errores.put("passwordActual", "La contraseña actual no es correcta");
        }
        if (passwordNueva == null || passwordNueva.length() < LARGO_MINIMO_PASSWORD
                || passwordNueva.length() > LARGO_MAXIMO_PASSWORD) {
            errores.put("passwordNueva", "La nueva contraseña debe tener entre 8 y 72 caracteres");
        } else if (passwordNueva.equals(passwordActual)) {
            errores.put("passwordNueva", "La nueva contraseña debe ser distinta de la actual");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo cambiar la contraseña", errores);
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);
        auditoriaService.registrar("CAMBIO_PASSWORD", "USUARIO", usuario.getId(), "Contraseña cambiada por el usuario");

        return generarRespuesta(usuario);
    }

    private LoginResponse generarRespuesta(Usuario usuario) {
        String rol = usuario.getRol().getNombre();
        String token = jwtTokenProvider.generarToken(usuario.getId().toString(), usuario.getUsername(), rol,
                usuario.isDebeCambiarPassword());
        return new LoginResponse(token, usuario.getUsername(), rol, usuario.isDebeCambiarPassword());
    }
}
