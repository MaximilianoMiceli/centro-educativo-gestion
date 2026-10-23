package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Horario;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class FormatoHorario {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    private FormatoHorario() {
    }

    public static String formatear(Horario horario) {
        return rango(horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin());
    }

    public static String rango(Short diaSemana, LocalTime inicio, LocalTime fin) {
        return DIAS[diaSemana - 1] + " " + inicio.format(HORA) + " a " + fin.format(HORA);
    }
}
