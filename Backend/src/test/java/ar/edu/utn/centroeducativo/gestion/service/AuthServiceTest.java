package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String HASH_DE_PASSWORD = "$2a$12$dArhfeeimtM4pwWNnhtkm.//G..DhSOn4uWnLezIPhiHFdn0bu5O.";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, new BCryptPasswordEncoder(), jwtTokenProvider);
    }

    @Test
    void credencialesValidasDevuelveTokenConRol() {
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuario(true)));
        when(jwtTokenProvider.generarToken("1", "admin", "ADMIN")).thenReturn("token-generado");

        var respuesta = authService.login("admin", "password");

        assertThat(respuesta).isPresent();
        assertThat(respuesta.get().token()).isEqualTo("token-generado");
        assertThat(respuesta.get().username()).isEqualTo("admin");
        assertThat(respuesta.get().rol()).isEqualTo("ADMIN");
    }

    @Test
    void claveIncorrectaNoGeneraToken() {
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuario(true)));

        assertThat(authService.login("admin", "otra-clave")).isEmpty();
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void usuarioInexistenteNoGeneraToken() {
        when(usuarioRepository.findByUsernameIgnoreCase("nadie")).thenReturn(Optional.empty());

        assertThat(authService.login("nadie", "password")).isEmpty();
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void usuarioInactivoNoGeneraToken() {
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuario(false)));

        assertThat(authService.login("admin", "password")).isEmpty();
        verifyNoInteractions(jwtTokenProvider);
    }

    private Usuario usuario(boolean activo) {
        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPasswordHash(HASH_DE_PASSWORD);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuario;
    }
}
