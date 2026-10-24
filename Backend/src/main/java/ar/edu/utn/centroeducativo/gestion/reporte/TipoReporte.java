package ar.edu.utn.centroeducativo.gestion.reporte;

import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import java.util.Arrays;
import java.util.Locale;

public enum TipoReporte {

    ALUMNO_FICHA("REP-001", "Ficha del alumno"),
    DOCENTE_CURSOS("REP-002", "Docentes: cursos y horarios por nivel"),
    ALUMNOS_CURSO("REP-003", "Alumnos por curso"),
    ALUMNOS_MATERIA("REP-004", "Alumnos por materia"),
    DOCENTES_NIVEL("REP-005", "Docentes por nivel"),
    ALUMNOS_DEPORTE("REP-006", "Alumnos por deporte"),
    ALUMNOS_DEPORTE_NIVEL("REP-007", "Alumnos por deporte y nivel"),
    DEPORTES_DETALLE("REP-008", "Detalle de deportes"),
    ALUMNOS_TRANSPORTE("REP-009", "Alumnos por recorrido de transporte");

    private final String codigo;
    private final String titulo;

    TipoReporte(String codigo, String titulo) {
        this.codigo = codigo;
        this.titulo = titulo;
    }

    public String codigo() {
        return codigo;
    }

    public String titulo() {
        return titulo;
    }

    /** Identificador usado en la URL, por ejemplo {@code alumnos-curso}. */
    public String slug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static TipoReporte desde(String slug) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.slug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado: " + slug));
    }
}
