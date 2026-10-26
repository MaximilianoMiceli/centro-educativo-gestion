package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.DocenteAgendaService;
import ar.edu.utn.centroeducativo.gestion.service.DocenteService;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteAgendaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearDocenteRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/docentes")
@RequiredArgsConstructor
@Tag(name = "Docentes", description = "Consulta y modificación de docentes (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class DocenteController {

    private final DocenteService docenteService;
    private final DocenteAgendaService docenteAgendaService;

    @GetMapping("/{id}")
    @Operation(summary = "Datos de un docente")
    public ResponseEntity<DocenteDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(docenteService.obtener(id));
    }

    @GetMapping("/{id}/agenda")
    @Operation(summary = "Agenda del docente",
            description = "Cursos a cargo (con nivel), materias de cada curso y sus horarios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agenda del docente"),
            @ApiResponse(responseCode = "404", description = "Docente no encontrado")
    })
    public ResponseEntity<DocenteAgendaDTO> agenda(@Parameter(description = "ID del docente") @PathVariable Long id) {
        return ResponseEntity.ok(docenteAgendaService.agenda(id));
    }

    @PostMapping
    @Operation(summary = "Dar de alta un docente", description = "Requiere DOCENTES_EDITAR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Docente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye errores por campo)"),
            @ApiResponse(responseCode = "403", description = "Requiere DOCENTES_EDITAR")
    })
    public ResponseEntity<DocenteDTO> crear(@RequestBody CrearDocenteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar docentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de docentes"),
            @ApiResponse(responseCode = "403", description = "Requiere DOCENTES_CONSULTAR")
    })
    public ResponseEntity<List<DocenteDTO>> listar() {
        return ResponseEntity.ok(docenteService.listar());
    }

    @GetMapping("/buscar/legajo")
    @Operation(summary = "Buscar docente por legajo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Docente encontrado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Docente no encontrado")
    })
    public ResponseEntity<DocenteDTO> buscarPorLegajo(
            @Parameter(description = "Legajo del docente") @RequestParam String legajo) {
        return ResponseEntity.ok(docenteService.buscarPorLegajo(legajo));
    }

    @GetMapping("/buscar/dni")
    @Operation(summary = "Buscar docente por DNI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Docente encontrado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Docente no encontrado")
    })
    public ResponseEntity<DocenteDTO> buscarPorDni(
            @Parameter(description = "DNI del docente") @RequestParam String dni) {
        return ResponseEntity.ok(docenteService.buscarPorDni(dni));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar datos de un docente",
            description = "Actualiza los datos editables de forma atómica. Tras 3 intentos inválidos, bloquea la edición 15 minutos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos actualizados"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye errores por campo)"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Docente no encontrado"),
            @ApiResponse(responseCode = "423", description = "Edición bloqueada temporalmente")
    })
    public ResponseEntity<DocenteDTO> actualizar(
            @Parameter(description = "ID del docente") @PathVariable Long id,
            @RequestBody DocenteUpdateRequest request) {
        return ResponseEntity.ok(docenteService.actualizar(id, request));
    }
}
