package ar.edu.utn.centroeducativo.gestion.correo;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.utn.centroeducativo.gestion.domain.TipoCorreo;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class CalendarioCorreosTest {

    @Test
    void finDeSemanaYFeriadosFijosNoSonHabiles() {
        assertThat(CalendarioCorreos.esHabil(LocalDate.of(2026, 11, 28))).isFalse(); // sábado
        assertThat(CalendarioCorreos.esHabil(LocalDate.of(2026, 11, 29))).isFalse(); // domingo
        assertThat(CalendarioCorreos.esHabil(LocalDate.of(2026, 12, 25))).isFalse(); // Navidad, viernes
        assertThat(CalendarioCorreos.esHabil(LocalDate.of(2026, 12, 24))).isTrue();
    }

    @Test
    void elUltimoDiaHabilRetrocedeSiElMesTerminaEnFinDeSemana() {
        assertThat(CalendarioCorreos.ultimoDiaHabil(YearMonth.of(2026, 12))).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(CalendarioCorreos.ultimoDiaHabil(YearMonth.of(2026, 11))).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(CalendarioCorreos.ultimoDiaHabil(YearMonth.of(2026, 5))).isEqualTo(LocalDate.of(2026, 5, 29));
        assertThat(CalendarioCorreos.ultimoDiaHabil(YearMonth.of(2026, 2))).isEqualTo(LocalDate.of(2026, 2, 27));
        assertThat(CalendarioCorreos.ultimoDiaHabil(YearMonth.of(2027, 1))).isEqualTo(LocalDate.of(2027, 1, 29));
    }

    @Test
    void elRecordatorioCorrespondeSoloElUltimoDiaHabil() {
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 11, 30))).containsExactly(TipoCorreo.RECORDATORIO);
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 5, 29))).containsExactly(TipoCorreo.RECORDATORIO);
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 5, 31))).isEmpty();
    }

    @Test
    void elAvisoDeDeudaCorrespondeElDia20AunqueNoSeaHabil() {
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 11, 20))).containsExactly(TipoCorreo.DEUDA);
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 12, 20))).containsExactly(TipoCorreo.DEUDA); // domingo
    }

    @Test
    void cualquierOtroDiaNoHayCorreos() {
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 12, 15))).isEmpty();
        assertThat(CalendarioCorreos.tareasDelDia(LocalDate.of(2026, 12, 1))).isEmpty();
    }
}
