package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.DictadoService;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearDictadoRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearHorarioRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.DictadoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.HorarioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Asignaciones y horarios", description = "Materias por docente y curso, y horarios con validación de conflictos")
@SecurityRequirement(name = "bearerAuth")
public class AsignacionController {

    private final DictadoService dictadoService;

    @GetMapping("/api/v1/dictados")
    @Operation(summary = "Listar asignaciones", description = "Filtrable por docente con el parámetro docenteId.")
    public ResponseEntity<List<DictadoDTO>> listarDictados(@RequestParam(required = false) Long docenteId) {
        return ResponseEntity.ok(docenteId == null
                ? dictadoService.listarTodos()
                : dictadoService.listarPorDocente(docenteId));
    }

    @PostMapping("/api/v1/dictados")
    @Operation(summary = "Asignar una materia a un docente en un curso",
            description = "Requiere DATOS_MAESTROS_ADMINISTRAR. Una materia tiene un solo docente por curso.")
    public ResponseEntity<DictadoDTO> asignar(@RequestBody CrearDictadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dictadoService.asignar(request));
    }

    @GetMapping("/api/v1/horarios")
    @Operation(summary = "Listar horarios de una asignación", description = "Parámetro obligatorio: dictadoId.")
    public ResponseEntity<List<HorarioDTO>> listarHorarios(@RequestParam Long dictadoId) {
        return ResponseEntity.ok(dictadoService.listarHorarios(dictadoId));
    }

    @PostMapping("/api/v1/horarios")
    @Operation(summary = "Registrar horario de una asignación",
            description = "Requiere DATOS_MAESTROS_ADMINISTRAR. Rechaza solapamientos con horarios del docente o del curso.")
    public ResponseEntity<HorarioDTO> crearHorario(@RequestBody CrearHorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dictadoService.crearHorario(request));
    }
}
