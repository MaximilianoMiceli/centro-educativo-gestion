package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.service.AuthService;
import ar.edu.utn.centroeducativo.gestion.web.dto.CambiarPasswordRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.LoginRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión y cambio de contraseña")
public class AuthController {

    private final AuthService authService;
    private final UsuarioActual usuarioActual;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida usuario y contraseña y devuelve un token JWT. Si debeCambiarPassword es true, el token solo permite cambiar la contraseña.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credenciales válidas"),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar la contraseña del usuario autenticado",
            description = "Devuelve un token nuevo sin la obligación de cambiar la contraseña.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña cambiada"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva inválida")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<LoginResponse> cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request) {
        Long usuarioId = usuarioActual.id()
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida"));
        return ResponseEntity.ok(authService.cambiarPassword(usuarioId, request.passwordActual(), request.passwordNueva()));
    }
}
