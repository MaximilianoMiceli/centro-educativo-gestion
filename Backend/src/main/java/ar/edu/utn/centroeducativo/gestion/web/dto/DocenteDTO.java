package ar.edu.utn.centroeducativo.gestion.web.dto;

import java.util.List;

public record DocenteDTO(
        Long id,
        String legajo,
        String dni,
        String nombre,
        String apellido,
        String especialidad,
        String email,
        String telefono,
        String estado,
        boolean dniEditable,
        List<String> cursos
) {
}
