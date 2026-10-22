package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.exception.EdicionBloqueadaException;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlEdicionDocenteTest {

    @Mock
    private DocenteRepository docenteRepository;

    private ControlEdicionDocente control;

    @BeforeEach
    void setUp() {
        control = new ControlEdicionDocente(docenteRepository);
    }

    @Test
    void tercerIntentoFallidoBloqueaLaEdicion() {
        Docente docente = docente((short) 2, null);
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente));

        control.registrarIntentoFallido(1L);

        ArgumentCaptor<Docente> guardado = ArgumentCaptor.forClass(Docente.class);
        verify(docenteRepository).save(guardado.capture());
        assertThat(guardado.getValue().getEdicionBloqueadaHasta()).isAfter(LocalDateTime.now().plusMinutes(14));
        assertThat(guardado.getValue().getIntentosEdicionFallidos()).isZero();
    }

    @Test
    void primerIntentoFallidoSoloIncrementa() {
        Docente docente = docente((short) 0, null);
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente));

        control.registrarIntentoFallido(1L);

        assertThat(docente.getIntentosEdicionFallidos()).isEqualTo((short) 1);
        assertThat(docente.getEdicionBloqueadaHasta()).isNull();
    }

    @Test
    void bloqueoVigenteImpideEditar() {
        Docente docente = docente((short) 0, LocalDateTime.now().plusMinutes(5));

        assertThatThrownBy(() -> control.verificarNoBloqueado(docente))
                .isInstanceOf(EdicionBloqueadaException.class);
    }

    @Test
    void bloqueoVencidoPermiteEditar() {
        Docente docente = docente((short) 0, LocalDateTime.now().minusMinutes(1));

        control.verificarNoBloqueado(docente);
    }

    @Test
    void bloqueoVencidoReiniciaElConteoAntesDeContarElNuevoIntento() {
        Docente docente = docente((short) 2, LocalDateTime.now().minusMinutes(1));
        when(docenteRepository.findById(1L)).thenReturn(Optional.of(docente));

        control.registrarIntentoFallido(1L);

        assertThat(docente.getIntentosEdicionFallidos()).isEqualTo((short) 1);
        assertThat(docente.getEdicionBloqueadaHasta()).isNull();
    }

    private Docente docente(short intentos, LocalDateTime bloqueadaHasta) {
        Docente docente = new Docente();
        docente.setId(1L);
        docente.setIntentosEdicionFallidos(intentos);
        docente.setEdicionBloqueadaHasta(bloqueadaHasta);
        return docente;
    }
}
