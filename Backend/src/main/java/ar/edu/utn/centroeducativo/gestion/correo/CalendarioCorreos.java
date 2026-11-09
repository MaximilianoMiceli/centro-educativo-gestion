package ar.edu.utn.centroeducativo.gestion.correo;

import ar.edu.utn.centroeducativo.gestion.domain.TipoCorreo;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Set;

/** Decide qué correos corresponden cada día: recordatorio el último día hábil y aviso de deuda el día 20. */
public final class CalendarioCorreos {

    public static final int DIA_AVISO_DEUDA = 20;

    /** Feriados nacionales de fecha fija. Los trasladables (por ejemplo 17 de agosto) no se consideran. */
    private static final Set<MonthDay> FERIADOS_FIJOS = Set.of(
            MonthDay.of(1, 1), MonthDay.of(3, 24), MonthDay.of(4, 2), MonthDay.of(5, 1), MonthDay.of(5, 25),
            MonthDay.of(6, 20), MonthDay.of(7, 9), MonthDay.of(12, 8), MonthDay.of(12, 25));

    private CalendarioCorreos() {
    }

    public static boolean esHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY
                && !FERIADOS_FIJOS.contains(MonthDay.from(fecha));
    }

    public static LocalDate ultimoDiaHabil(YearMonth mes) {
        LocalDate fecha = mes.atEndOfMonth();
        while (!esHabil(fecha)) {
            fecha = fecha.minusDays(1);
        }
        return fecha;
    }

    public static Set<TipoCorreo> tareasDelDia(LocalDate hoy) {
        Set<TipoCorreo> tareas = EnumSet.noneOf(TipoCorreo.class);
        if (hoy.equals(ultimoDiaHabil(YearMonth.from(hoy)))) {
            tareas.add(TipoCorreo.RECORDATORIO);
        }
        if (hoy.getDayOfMonth() == DIA_AVISO_DEUDA) {
            tareas.add(TipoCorreo.DEUDA);
        }
        return tareas;
    }
}
