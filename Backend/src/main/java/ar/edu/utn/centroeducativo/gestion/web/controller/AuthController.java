package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación.
 * Para desarrollo: GET /api/v1/auth/token devuelve un token válido.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación (desarrollo)")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Devuelve un token JWT válido para desarrollo.
     * En producción, usar un endpoint /login real.
     */
    @GetMapping("/token")
    @Operation(summary = "Obtener token de prueba",
            description = "Devuelve un token JWT válido para desarrollo")
    public ResponseEntity<Map<String, String>> getToken() {
        String token = jwtTokenProvider.generarToken("1", "admin", "ADMIN");
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Token válido por 1 hora");
        return ResponseEntity.ok(response);
    }
}
