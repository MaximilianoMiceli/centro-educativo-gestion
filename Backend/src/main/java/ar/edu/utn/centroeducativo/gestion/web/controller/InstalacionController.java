package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.InstalacionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.InstalacionDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudInstalacion;
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
@RequestMapping("/api/v1/instalaciones")
@RequiredArgsConstructor
@Tag(name = "Instalaciones", description = "Instalaciones del centro. Las altas y cambios requieren DATOS_MAESTROS_ADMINISTRAR")
@SecurityRequirement(name = "bearerAuth")
public class InstalacionController {

    private final InstalacionService instalacionService;

    @GetMapping
    @Operation(summary = "Listar instalaciones")
    public List<InstalacionDTO> listar() {
        return instalacionService.listar();
    }

    @PostMapping
    @Operation(summary = "Crear instalación")
    public ResponseEntity<InstalacionDTO> crear(@RequestBody SolicitudInstalacion solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instalacionService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar instalación, incluido su estado (activa, en mantenimiento o inactiva)")
    public InstalacionDTO modificar(@PathVariable Long id, @RequestBody SolicitudInstalacion solicitud) {
        return instalacionService.modificar(id, solicitud);
    }
}
