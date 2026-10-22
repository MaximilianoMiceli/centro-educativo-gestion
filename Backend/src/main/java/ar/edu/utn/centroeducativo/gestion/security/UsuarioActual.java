package ar.edu.utn.centroeducativo.gestion.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActual {

    private static final String ROL_PROFESOR = "PROFESOR";

    public Optional<Long> id() {
        return jwt().map(jwt -> Long.valueOf(jwt.getSubject()));
    }

    public boolean esProfesor() {
        return jwt().map(jwt -> ROL_PROFESOR.equals(jwt.getClaimAsString("rol"))).orElse(false);
    }

    private Optional<Jwt> jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }
}
