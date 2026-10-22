package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.AlumnoAdministracionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoFichaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoMateriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DatosAlumnoRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alumnos")
@RequiredArgsConstructor
@Tag(name = "Alumnos", description = "Alta, modificación y materias cursadas de alumnos")
@SecurityRequirement(name = "bearerAuth")
public class AlumnoAdministracionController {

    private final AlumnoAdministracionService alumnoAdministracionService;

    @GetMapping
    @Operation(summary = "Listar alumnos",
            description = "Filtrable por nivel (nivelId), curso (cursoId) y por texto en legajo, DNI, nombre o apellido (busqueda). "
                    + "Los profesores solo ven los alumnos de sus cursos.")
    public ResponseEntity<List<AlumnoFichaDTO>> listar(@RequestParam(required = false) String busqueda,
                                                       @RequestParam(required = false) Short nivelId,
                                                       @RequestParam(required = false) Long cursoId) {
        return ResponseEntity.ok(alumnoAdministracionService.listar(busqueda, nivelId, cursoId));
    }

    @PostMapping
    @Operation(summary = "Dar de alta un alumno", description = "Requiere ALUMNOS_ADMINISTRAR.")
    public ResponseEntity<AlumnoFichaDTO> crear(@RequestBody DatosAlumnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alumnoAdministracionService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar un alumno",
            description = "Requiere ALUMNOS_ADMINISTRAR. El legajo no se modifica; el cambio de curso queda registrado en auditoría.")
    public ResponseEntity<AlumnoFichaDTO> modificar(@PathVariable Long id, @RequestBody DatosAlumnoRequest request) {
        return ResponseEntity.ok(alumnoAdministracionService.modificar(id, request));
    }

    @GetMapping("/{id}/materias")
    @Operation(summary = "Materias cursadas por el alumno", description = "Materias de su curso, con docente y horarios.")
    public ResponseEntity<List<AlumnoMateriaDTO>> materias(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoAdministracionService.materias(id));
    }
}
