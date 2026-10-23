package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.UsuarioService;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearUsuarioRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.RestablecerPasswordRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.UsuarioCreadoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.UsuarioListaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Alta, listado y restablecimiento de contraseñas (requiere USUARIOS_ADMINISTRAR)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<List<UsuarioListaDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    @Operation(summary = "Crear usuario",
            description = "El administrador define la contraseña inicial. Por defecto el usuario debe cambiarla al ingresar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (incluye errores por campo)"),
            @ApiResponse(responseCode = "403", description = "Requiere USUARIOS_ADMINISTRAR")
    })
    public ResponseEntity<UsuarioCreadoDTO> crear(@RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Restablecer la contraseña de un usuario",
            description = "Reemplaza la contraseña. Por defecto el usuario debe cambiarla al ingresar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> restablecerPassword(@PathVariable Long id,
                                                    @RequestBody RestablecerPasswordRequest request) {
        usuarioService.restablecerPassword(id, request);
        return ResponseEntity.ok().build();
    }
}
