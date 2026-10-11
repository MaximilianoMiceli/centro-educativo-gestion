package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.NivelEducativo;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private AlumnoService alumnoService;

    @BeforeEach
    void setUp() {
        alumnoService = new AlumnoService(alumnoRepository, auditoriaService);
    }

    @Test
    void obtenerPorIdDevuelveDtoYRegistraAuditoria() {
        when(alumnoRepository.findWithCursoById(1L)).thenReturn(Optional.of(alumno()));

        var dto = alumnoService.obtenerPorId(1L);

        assertThat(dto.getLegajo()).isEqualTo("ALU001");
        assertThat(dto.getNivelNombre()).isEqualTo("Secundario");
        verify(auditoriaService).registrar("CONSULTA", "ALUMNO", 1L, "Búsqueda por ID: 1");
    }

    @Test
    void obtenerPorIdInexistenteLanzaNoEncontrado() {
        when(alumnoRepository.findWithCursoById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verifyNoInteractions(auditoriaService);
    }

    @Test
    void buscarPorLegajoInexistenteLanzaNoEncontrado() {
        when(alumnoRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoService.buscarPorLegajo("INEXISTENTE"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verifyNoInteractions(auditoriaService);
    }

    @Test
    void buscarPorDniEncontradoRegistraAuditoria() {
        when(alumnoRepository.findOne(any(Specification.class))).thenReturn(Optional.of(alumno()));

        var dto = alumnoService.buscarPorDni("45678901");

        assertThat(dto.getDni()).isEqualTo("45678901");
        verify(auditoriaService).registrar(anyString(), anyString(), any(), anyString());
    }

    private Alumno alumno() {
        NivelEducativo nivel = new NivelEducativo();
        nivel.setNombre("Secundario");

        Curso curso = new Curso();
        curso.setId(1L);
        curso.setNivel(nivel);
        curso.setAnio((short) 1);
        curso.setSeccion("A");

        Alumno alumno = new Alumno();
        alumno.setId(1L);
        alumno.setLegajo("ALU001");
        alumno.setDni("45678901");
        alumno.setNombre("Carlos");
        alumno.setApellido("García");
        alumno.setCurso(curso);
        return alumno;
    }
}
