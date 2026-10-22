package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.PermisosService;
import ar.edu.utn.centroeducativo.gestion.web.dto.PermisoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permisos")
@RequiredArgsConstructor
@Tag(name = "Roles y permisos", description = "Catálogo de permisos (requiere PERMISOS_ADMINISTRAR)")
@SecurityRequirement(name = "bearerAuth")
public class PermisoController {

    private final PermisosService permisosService;

    @GetMapping
    @Operation(summary = "Listar permisos disponibles")
    public ResponseEntity<List<PermisoDTO>> listar() {
        return ResponseEntity.ok(permisosService.listarPermisos());
    }
}
