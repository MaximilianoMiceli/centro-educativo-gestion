package ar.edu.utn.centroeducativo.gestion.reporte;

import java.util.List;

public record Reporte(String codigo, String titulo, List<String> filtros, List<Seccion> secciones) {

    public record Seccion(String titulo, List<String> columnas, List<List<String>> filas) {
    }
}
