package ar.edu.utn.centroeducativo.gestion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Decoder personalizado que valida tokens JWT con jjwt (misma librería que los genera).
 * Esto asegura consistencia entre el generador (JwtTokenProvider) y el validador.
 */
public class CustomJwtDecoder implements JwtDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CustomJwtDecoder.class);
    private final String secret;

    public CustomJwtDecoder(String secret) {
        this.secret = secret;
        logger.info("CustomJwtDecoder inicializado con clave de {} caracteres", secret.length());
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        logger.debug("Intentando decodificar token: {}", token.substring(0, Math.min(50, token.length())) + "...");

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            logger.info("Token decodificado exitosamente. Sujeto: {}, Rol: {}",
                    claims.getSubject(), claims.get("rol"));

            String rol = (String) claims.get("rol");

            // Parsear headers del JWT (primera parte del token)
            String[] parts = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<String, Object> headers = new ObjectMapper().readValue(headerJson, Map.class);

            Jwt.Builder builder = Jwt.withTokenValue(token)
                    .subject(claims.getSubject())
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(claims.getExpiration().toInstant())
                    .claim("rol", rol)
                    .claim("username", claims.get("username"));

            // Agregar headers (requerido por Spring Security)
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                builder.header(header.getKey(), header.getValue().toString());
            }

            return builder.build();

        } catch (Exception e) {
            logger.error("Error decodificando JWT: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new BadJwtException("Invalid JWT token: " + e.getMessage(), e);
        }
    }
}
