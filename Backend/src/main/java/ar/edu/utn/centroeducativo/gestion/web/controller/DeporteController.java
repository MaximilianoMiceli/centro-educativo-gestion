package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.DeporteService;
import ar.edu.utn.centroeducativo.gestion.web.dto.DeporteDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.AsignarResponsable;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.CrearDeporte;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.CrearGrupo;
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
@RequestMapping("/api/v1/deportes")
@RequiredArgsConstructor
@Tag(name = "Deportes", description = "Deportes, responsables y grupos por nivel y horario")
@SecurityRequirement(name = "bearerAuth")
public class DeporteController {

    private final DeporteService deporteService;

    @GetMapping
    @Operation(summary = "Listar deportes con sus grupos")
    public ResponseEntity<List<DeporteDTO>> listar() {
        return ResponseEntity.ok(deporteService.listar());
    }

    @PostMapping
    @Operation(summary = "Crear deporte", description = "Requiere DATOS_MAESTROS_ADMINISTRAR.")
    public ResponseEntity<DeporteDTO> crear(@RequestBody CrearDeporte request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deporteService.crear(request));
    }

    @PutMapping("/{id}/responsable")
    @Operation(summary = "Asignar profesor responsable", description = "Requiere DATOS_MAESTROS_ADMINISTRAR.")
    public ResponseEntity<DeporteDTO> asignarResponsable(@PathVariable Long id, @RequestBody AsignarResponsable request) {
        return ResponseEntity.ok(deporteService.asignarResponsable(id, request));
    }

    @PostMapping("/{id}/grupos")
    @Operation(summary = "Crear grupo del deporte para un nivel y horario",
            description = "Requiere DATOS_MAESTROS_ADMINISTRAR. Rechaza horarios en conflicto con el docente responsable.")
    public ResponseEntity<DeporteDTO> crearGrupo(@PathVariable Long id, @RequestBody CrearGrupo request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deporteService.crearGrupo(id, request));
    }
}
