package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.EstadoDocente;
import ar.edu.utn.centroeducativo.gestion.exception.EdicionBloqueadaException;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteUpdateRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocenteServiceTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ControlEdicionDocente controlEdicion;

    private DocenteService docenteService;

    @BeforeEach
    void setUp() {
        docenteService = new DocenteService(docenteRepository, usuarioRepository, auditoriaService, controlEdicion);
    }

    @Test
    void actualizarDatosValidosGuardaYAuditaLosCambios() {
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente()));
        when(usuarioRepository.existsByDocente_Id(1L)).thenReturn(false);

        var dto = docenteService.actualizar(1L, request("12345678", "Pedro", "juan@centro.local"));

        assertThat(dto.nombre()).isEqualTo("Pedro");
        verify(docenteRepository).save(any(Docente.class));
        verify(auditoriaService).registrar("MODIFICACION", "DOCENTE", 1L, "nombre: 'Juan' -> 'Pedro'");
        verify(controlEdicion, never()).registrarIntentoFallido(any());
    }

    @Test
    void datosInvalidosRegistranIntentoYNoGuardan() {
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente()));

        assertThatThrownBy(() -> docenteService.actualizar(1L, request("12345678", "Juan", "juan-sin-arroba")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores()).containsKey("email"));

        verify(controlEdicion).registrarIntentoFallido(1L);
        verify(docenteRepository, never()).save(any());
        verify(auditoriaService, never()).registrar(anyString(), anyString(), any(), anyString());
    }

    @Test
    void docenteBloqueadoNoSeEditaNiSeGuarda() {
        LocalDateTime hasta = LocalDateTime.now().plusMinutes(10);
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente()));
        doThrow(new EdicionBloqueadaException("bloqueado", hasta))
                .when(controlEdicion).verificarNoBloqueado(any());

        assertThatThrownBy(() -> docenteService.actualizar(1L, request("12345678", "Pedro", "juan@centro.local")))
                .isInstanceOf(EdicionBloqueadaException.class);
        verify(docenteRepository, never()).save(any());
    }

    @Test
    void dniNoSeCambiaSiElDocenteTieneUsuarioAsociado() {
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente()));
        when(usuarioRepository.existsByDocente_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> docenteService.actualizar(1L, request("99999999", "Juan", "juan@centro.local")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsEntry("dni", "El DNI no se puede modificar porque el docente tiene un usuario asociado"));
        verify(docenteRepository, never()).save(any());
    }

    @Test
    void dniPuedeCambiarSiElDocenteNoTieneUsuario() {
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente()));
        when(usuarioRepository.existsByDocente_Id(1L)).thenReturn(false);
        when(docenteRepository.existsByDniAndIdNot("99999999", 1L)).thenReturn(false);

        var dto = docenteService.actualizar(1L, request("99999999", "Juan", "juan@centro.local"));

        assertThat(dto.dni()).isEqualTo("99999999");
        verify(docenteRepository).save(any(Docente.class));
    }

    @Test
    void dniDuplicadoEsRechazado() {
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente()));
        when(usuarioRepository.existsByDocente_Id(1L)).thenReturn(false);
        when(docenteRepository.existsByDniAndIdNot("87654321", 1L)).thenReturn(true);

        assertThatThrownBy(() -> docenteService.actualizar(1L, request("87654321", "Juan", "juan@centro.local")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsEntry("dni", "Ya existe un docente con ese DNI"));
    }

    @Test
    void docenteInexistenteDevuelveNoEncontrado() {
        when(docenteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteService.actualizar(99L, request("12345678", "Juan", "juan@centro.local")))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void buscarPorLegajoNoDiferenciaMayusculas() {
        when(docenteRepository.findByLegajoIgnoreCase("doc001")).thenReturn(Optional.of(docente()));
        when(usuarioRepository.existsByDocente_Id(1L)).thenReturn(false);

        var dto = docenteService.buscarPorLegajo("doc001");

        assertThat(dto.legajo()).isEqualTo("DOC001");
    }

    private Docente docente() {
        Docente docente = new Docente();
        docente.setId(1L);
        docente.setLegajo("DOC001");
        docente.setDni("12345678");
        docente.setNombre("Juan");
        docente.setApellido("Rodríguez");
        docente.setEspecialidad("Matemática");
        docente.setEmail("juan@centro.local");
        docente.setTelefono("3624123456");
        docente.setEstado(EstadoDocente.ACTIVO);
        return docente;
    }

    private DocenteUpdateRequest request(String dni, String nombre, String email) {
        return new DocenteUpdateRequest(dni, nombre, "Rodríguez", "Matemática", email, "3624123456", "ACTIVO");
    }
}
