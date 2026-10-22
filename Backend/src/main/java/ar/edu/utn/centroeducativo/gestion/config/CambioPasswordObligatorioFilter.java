package ar.edu.utn.centroeducativo.gestion.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class CambioPasswordObligatorioFilter extends OncePerRequestFilter {

    private static final String RUTA_CAMBIO_PASSWORD = "/api/v1/auth/cambiar-password";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean debeCambiar = auth instanceof JwtAuthenticationToken jwtAuth
                && Boolean.TRUE.equals(jwtAuth.getToken().getClaimAsBoolean("debeCambiarPassword"));

        if (debeCambiar && !RUTA_CAMBIO_PASSWORD.equals(request.getRequestURI())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":403,\"codigo\":\"CAMBIO_PASSWORD_REQUERIDO\","
                    + "\"message\":\"Debés cambiar tu contraseña antes de continuar\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
