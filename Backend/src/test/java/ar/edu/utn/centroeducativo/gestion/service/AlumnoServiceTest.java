package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.NivelEducativo;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceTest {

    private static final long CURSO_DEL_PROFESOR = 1L;
    private static final long CURSO_AJENO = 3L;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private UsuarioActual usuarioActual;

    private AlumnoService alumnoService;

    @BeforeEach
    void setUp() {
        alumnoService = new AlumnoService(alumnoRepository, auditoriaService,
                usuarioRepository, docenteRepository, usuarioActual);
    }

    @Test
    void adminObtienePorIdCualquierAlumno() {
        when(usuarioActual.esProfesor()).thenReturn(false);
        when(alumnoRepository.findWithCursoById(1L)).thenReturn(Optional.of(alumno("ALU001", CURSO_AJENO)));

        var dto = alumnoService.obtenerPorId(1L);

        assertThat(dto.getLegajo()).isEqualTo("ALU001");
        assertThat(dto.getNivelNombre()).isEqualTo("Secundario");
        verify(auditoriaService).registrar("CONSULTA", "ALUMNO", 1L, "Búsqueda por ID: 1");
    }

    @Test
    void profesorObtienePorIdAlumnoDeSusCursos() {
        stubProfesor();
        when(alumnoRepository.findWithCursoById(1L)).thenReturn(Optional.of(alumno("ALU001", CURSO_DEL_PROFESOR)));

        var dto = alumnoService.obtenerPorId(1L);

        assertThat(dto.getLegajo()).isEqualTo("ALU001");
        verify(auditoriaService).registrar("CONSULTA", "ALUMNO", 1L, "Búsqueda por ID: 1");
    }

    @Test
    void profesorObtienePorIdAlumnoAjenoDevuelveNoAutorizado() {
        stubProfesor();
        when(alumnoRepository.findWithCursoById(4L)).thenReturn(Optional.of(alumno("ALU004", CURSO_AJENO)));

        assertThatThrownBy(() -> alumnoService.obtenerPorId(4L))
                .isInstanceOf(NoAutorizadoException.class);
        verify(auditoriaService, never()).registrar(anyString(), anyString(), any(), anyString());
    }

    @Test
    void obtenerPorIdInexistenteDevuelveNoEncontrado() {
        when(alumnoRepository.findWithCursoById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verifyNoInteractions(auditoriaService);
    }

    @Test
    void buscarPorLegajoInexistenteDevuelveNoEncontrado() {
        when(alumnoRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoService.buscarPorLegajo("INEXISTENTE"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verifyNoInteractions(auditoriaService);
    }

    @Test
    void profesorBuscaPorDniAlumnoAjenoDevuelveNoAutorizado() {
        stubProfesor();
        when(alumnoRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(alumno("ALU004", CURSO_AJENO)));

        assertThatThrownBy(() -> alumnoService.buscarPorDni("78901234"))
                .isInstanceOf(NoAutorizadoException.class);
    }

    @Test
    void buscarPorNombreProfesorSoloRecibeAlumnosDeSusCursos() {
        stubProfesor();
        when(alumnoRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(alumno("ALU001", CURSO_DEL_PROFESOR), alumno("ALU004", CURSO_AJENO)));

        var resultados = alumnoService.buscarPorNombre("a");

        assertThat(resultados).extracting("legajo").containsExactly("ALU001");
    }

    @Test
    void buscarPorNombreConCoincidenciasAjenasDevuelveNoAutorizado() {
        stubProfesor();
        when(alumnoRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(alumno("ALU004", CURSO_AJENO)));

        assertThatThrownBy(() -> alumnoService.buscarPorNombre("Paula"))
                .isInstanceOf(NoAutorizadoException.class);
    }

    @Test
    void buscarPorNombreSinCoincidenciasDevuelveNoEncontrado() {
        when(alumnoRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThatThrownBy(() -> alumnoService.buscarPorNombre("inexistente"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private void stubProfesor() {
        Docente docente = new Docente();
        docente.setId(1L);

        Usuario profesor = new Usuario();
        profesor.setId(3L);
        profesor.setDocente(docente);

        when(usuarioActual.esProfesor()).thenReturn(true);
        when(usuarioActual.id()).thenReturn(Optional.of(3L));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(profesor));
        when(docenteRepository.findCursoIdsByDocenteId(1L)).thenReturn(Set.of(CURSO_DEL_PROFESOR, 2L));
    }

    private Alumno alumno(String legajo, long cursoId) {
        NivelEducativo nivel = new NivelEducativo();
        nivel.setNombre("Secundario");

        Curso curso = new Curso();
        curso.setId(cursoId);
        curso.setNivel(nivel);
        curso.setAnio((short) 1);
        curso.setSeccion("A");

        Alumno alumno = new Alumno();
        alumno.setId(cursoId * 10);
        alumno.setLegajo(legajo);
        alumno.setDni("45678901");
        alumno.setNombre("Carlos");
        alumno.setApellido("García");
        alumno.setCurso(curso);
        return alumno;
    }
}
