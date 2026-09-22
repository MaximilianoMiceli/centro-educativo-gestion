import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;

public class GenerateToken {
    public static void main(String[] args) {
        String jwtSecret = "esta-es-una-clave-secreta-para-desarrollo-solamente-cambiar-en-produccion";
        long jwtExpirationMs = 86400000; // 24 horas

        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .setSubject("1")
                .claim("username", "admin")
                .claim("rol", "ADMIN")
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS512)
                .compact();

        System.out.println("Token JWT válido (24 horas):");
        System.out.println(token);
    }
}
