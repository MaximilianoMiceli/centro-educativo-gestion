package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.PermisosService;
import ar.edu.utn.centroeducativo.gestion.web.dto.ActualizarPermisosRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.RolPermisosDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles y permisos", description = "Configuración de permisos por rol (requiere PERMISOS_ADMINISTRAR)")
@SecurityRequirement(name = "bearerAuth")
public class RolController {

    private final PermisosService permisosService;

    @GetMapping
    @Operation(summary = "Listar roles con sus permisos")
    public ResponseEntity<List<RolPermisosDTO>> listar() {
        return ResponseEntity.ok(permisosService.listarRoles());
    }

    @PutMapping("/{nombre}/permisos")
    @Operation(summary = "Reemplazar los permisos de un rol",
            description = "Los cambios aplican de inmediato en las siguientes requests.")
    public ResponseEntity<RolPermisosDTO> actualizarPermisos(
            @PathVariable String nombre,
            @RequestBody ActualizarPermisosRequest request) {
        return ResponseEntity.ok(permisosService.actualizarPermisos(nombre, request.permisos()));
    }
}
