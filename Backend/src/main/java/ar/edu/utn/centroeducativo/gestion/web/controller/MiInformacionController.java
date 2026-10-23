package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.MiInformacionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoResumenDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.MiInformacionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mi-informacion")
@RequiredArgsConstructor
@Tag(name = "Mi información", description = "Información propia del alumno o de los hijos vinculados al padre (solo lectura)")
@SecurityRequirement(name = "bearerAuth")
public class MiInformacionController {

    private final MiInformacionService miInformacionService;

    @GetMapping("/alumnos")
    @Operation(summary = "Alumnos visibles para el usuario (él mismo o sus hijos)")
    public ResponseEntity<List<AlumnoResumenDTO>> alumnos() {
        return ResponseEntity.ok(miInformacionService.alumnos());
    }

    @GetMapping("/alumnos/{id}")
    @Operation(summary = "Ficha, materias y servicios de un alumno visible para el usuario")
    public ResponseEntity<MiInformacionDTO> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(miInformacionService.detalle(id));
    }
}
