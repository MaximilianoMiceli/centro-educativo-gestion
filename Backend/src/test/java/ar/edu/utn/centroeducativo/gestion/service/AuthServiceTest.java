package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
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

    @Mock
    private AuditoriaService auditoriaService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, new BCryptPasswordEncoder(), jwtTokenProvider, auditoriaService);
    }

    @Test
    void credencialesValidasDevuelveTokenConRol() {
        when(usuarioRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(usuario(true)));
        when(jwtTokenProvider.generarToken("1", "admin", "ADMIN", false)).thenReturn("token-generado");

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

    @Test
    void cambiarPasswordGuardaLaNuevaYDevuelveTokenSinObligacion() {
        Usuario usuario = usuario(true);
        usuario.setDebeCambiarPassword(true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generarToken("1", "admin", "ADMIN", false)).thenReturn("token-nuevo");

        var respuesta = authService.cambiarPassword(1L, "password", "NuevaClave2026");

        assertThat(respuesta.token()).isEqualTo("token-nuevo");
        assertThat(respuesta.debeCambiarPassword()).isFalse();
        assertThat(usuario.isDebeCambiarPassword()).isFalse();
        assertThat(new BCryptPasswordEncoder().matches("NuevaClave2026", usuario.getPasswordHash())).isTrue();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPasswordRechazaClaveActualIncorrecta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(true)));

        assertThatThrownBy(() -> authService.cambiarPassword(1L, "otra", "NuevaClave2026"))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsKey("passwordActual"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPasswordRechazaNuevaIgualALaActual() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(true)));

        assertThatThrownBy(() -> authService.cambiarPassword(1L, "password", "password"))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsKey("passwordNueva"));
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
        usuario.setDebeCambiarPassword(false);
        return usuario;
    }
}
