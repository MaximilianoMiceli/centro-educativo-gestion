package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.EstadoDocente;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteUpdateRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class DocenteValidador {

    static final Set<String> ESPECIALIDADES = Set.of(
            "Matemática", "Lengua", "Ciencias Sociales", "Ciencias Naturales",
            "Inglés", "Educación Física", "Artes");

    private static final Pattern DNI = Pattern.compile("\\d{7,8}");
    private static final Pattern NOMBRE = Pattern.compile("\\p{L}+(?:[ '\\-]\\p{L}+)*");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+");
    private static final Pattern TELEFONO = Pattern.compile("\\d{10}");

    private DocenteValidador() {
    }

    static Map<String, String> validar(DocenteUpdateRequest r) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (!coincide(DNI, r.dni())) {
            errores.put("dni", "DNI inválido. Debe tener 7 u 8 dígitos");
        }
        if (!coincide(NOMBRE, r.nombre()) || r.nombre().length() > 80) {
            errores.put("nombre", "Nombre inválido. Solo letras, espacios, guiones y apóstrofos");
        }
        if (!coincide(NOMBRE, r.apellido()) || r.apellido().length() > 80) {
            errores.put("apellido", "Apellido inválido. Solo letras, espacios, guiones y apóstrofos");
        }
        if (r.especialidad() == null || !ESPECIALIDADES.contains(r.especialidad())) {
            errores.put("especialidad", "Especialidad inválida. Elegí una de la lista");
        }
        if (!coincide(EMAIL, r.email()) || r.email().length() > 120) {
            errores.put("email", "Email inválido. Use formato: ejemplo@dominio.com");
        }
        if (!coincide(TELEFONO, r.telefono())) {
            errores.put("telefono", "Teléfono inválido. Debe tener 10 dígitos");
        }
        if (estadoValido(r.estado()) == null) {
            errores.put("estado", "Estado inválido. Use Activo, Inactivo o Licencia");
        }
        return errores;
    }

    static EstadoDocente estadoValido(String estado) {
        if (estado == null) {
            return null;
        }
        try {
            return EstadoDocente.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean coincide(Pattern patron, String valor) {
        return valor != null && patron.matcher(valor).matches();
    }
}
