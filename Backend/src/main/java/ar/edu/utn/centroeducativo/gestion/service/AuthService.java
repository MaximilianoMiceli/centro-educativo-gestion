package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.JwtTokenProvider;
import ar.edu.utn.centroeducativo.gestion.web.dto.LoginResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public Optional<LoginResponse> login(String username, String password) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(Usuario::isActivo)
                .filter(usuario -> passwordEncoder.matches(password, usuario.getPasswordHash()))
                .map(this::generarRespuesta);
    }

    private LoginResponse generarRespuesta(Usuario usuario) {
        String rol = usuario.getRol().getNombre();
        String token = jwtTokenProvider.generarToken(usuario.getId().toString(), usuario.getUsername(), rol);
        return new LoginResponse(token, usuario.getUsername(), rol);
    }
}
