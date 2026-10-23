package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.PadreRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RolRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearUsuarioRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private PadreRepository padreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, rolRepository, docenteRepository,
                alumnoRepository, padreRepository, passwordEncoder, auditoriaService);
    }

    @Test
    void creaUsuarioConLaPasswordDelAdministradorHasheada() {
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rol("ADMIN")));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        var creado = usuarioService.crear(request("nuevo.admin", "nuevo@centro.local", "DNI", "30111222", "ADMIN"));

        assertThat(creado.username()).isEqualTo("nuevo.admin");
        verify(passwordEncoder).encode("ClaveInicial1");
        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getPasswordHash()).isEqualTo("hash");
        assertThat(guardado.getValue().isDebeCambiarPassword()).isTrue();
        verify(auditoriaService).registrar(anyString(), anyString(), any(), anyString());
    }

    @Test
    void profesorSeVinculaConSuDocente() {
        Docente docente = new Docente();
        docente.setId(2L);
        when(rolRepository.findByNombre("PROFESOR")).thenReturn(Optional.of(rol("PROFESOR")));
        when(docenteRepository.findByDni("87654321")).thenReturn(Optional.of(docente));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        usuarioService.crear(request("jperez", "jperez@centro.local", "DNI", "87654321", "PROFESOR"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getDocente()).isSameAs(docente);
    }

    @Test
    void profesorSinDocenteExistenteEsRechazado() {
        when(rolRepository.findByNombre("PROFESOR")).thenReturn(Optional.of(rol("PROFESOR")));
        when(docenteRepository.findByDni("11111111")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.crear(request("jperez", "jperez@centro.local", "DNI", "11111111", "PROFESOR")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsEntry("numeroDocumento", "No existe un docente con ese DNI"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void usernameCortoOEnUsoSeRechaza() {
        assertThatThrownBy(() -> usuarioService.crear(request("ab", "x@centro.local", "DNI", "30111222", "ADMIN")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores()).containsKey("username"));

        when(usuarioRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.crear(request("admin", "x@centro.local", "DNI", "30111222", "ADMIN")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores()).containsKey("username"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void dniConFormatoInvalidoSeRechaza() {
        assertThatThrownBy(() -> usuarioService.crear(request("nuevo.admin", "n@centro.local", "DNI", "12-3", "ADMIN")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores()).containsKey("numeroDocumento"));
    }

    @Test
    void rolInexistenteSeRechaza() {
        assertThatThrownBy(() -> usuarioService.crear(request("nuevo.admin", "n@centro.local", "DNI", "30111222", "SUPERUSER")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores()).containsKey("rol"));
    }

    private CrearUsuarioRequest request(String username, String email, String tipo, String numero, String rol) {
        return new CrearUsuarioRequest(username, email, tipo, numero, rol, "ClaveInicial1", null);
    }

    private Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }
}
