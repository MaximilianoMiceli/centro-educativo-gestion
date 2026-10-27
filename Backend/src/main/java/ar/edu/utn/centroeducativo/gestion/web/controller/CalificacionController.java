package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.CalificacionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.CrearEvaluacionRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.DictadoCalificableDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.EvaluacionDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.GuardarNotasRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.PlanillaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calificaciones")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Evaluaciones y notas de las materias a cargo (profesor) o de todas (administrador)")
@SecurityRequirement(name = "bearerAuth")
public class CalificacionController {

    private final CalificacionService calificacionService;

    @GetMapping("/dictados")
    @Operation(summary = "Materias que puedo calificar")
    public ResponseEntity<List<DictadoCalificableDTO>> dictados() {
        return ResponseEntity.ok(calificacionService.dictados());
    }

    @GetMapping("/dictados/{dictadoId}/evaluaciones")
    @Operation(summary = "Evaluaciones de una materia")
    public ResponseEntity<List<EvaluacionDTO>> evaluaciones(@PathVariable Long dictadoId) {
        return ResponseEntity.ok(calificacionService.evaluaciones(dictadoId));
    }

    @PostMapping("/dictados/{dictadoId}/evaluaciones")
    @Operation(summary = "Crear una evaluación en una materia", description = "Requiere CALIFICACIONES_CARGAR.")
    public ResponseEntity<EvaluacionDTO> crearEvaluacion(@PathVariable Long dictadoId,
                                                         @RequestBody CrearEvaluacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calificacionService.crearEvaluacion(dictadoId, request));
    }

    @GetMapping("/evaluaciones/{evaluacionId}")
    @Operation(summary = "Planilla de notas de una evaluación")
    public ResponseEntity<PlanillaDTO> planilla(@PathVariable Long evaluacionId) {
        return ResponseEntity.ok(calificacionService.planilla(evaluacionId));
    }

    @PutMapping("/evaluaciones/{evaluacionId}")
    @Operation(summary = "Cargar o corregir notas de una evaluación", description = "Requiere CALIFICACIONES_CARGAR.")
    public ResponseEntity<PlanillaDTO> guardarNotas(@PathVariable Long evaluacionId,
                                                    @RequestBody GuardarNotasRequest request) {
        return ResponseEntity.ok(calificacionService.guardarNotas(evaluacionId, request.notas()));
    }
}
