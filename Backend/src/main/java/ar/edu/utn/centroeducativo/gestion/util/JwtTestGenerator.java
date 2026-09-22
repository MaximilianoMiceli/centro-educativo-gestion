package ar.edu.utn.centroeducativo.gestion.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;

/**
 * Utilidad para generar tokens JWT de prueba.
 *
 * Uso:
 * java -cp "target/classes:..." ar.edu.utn.centroeducativo.gestion.util.JwtTestGenerator admin ADMIN
 */
public class JwtTestGenerator {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: JwtTestGenerator <usuarioId> <rol>");
            System.out.println("Ejemplo: JwtTestGenerator 1 ADMIN");
            System.exit(1);
        }

        String usuarioId = args[0];
        String rol = args[1];

        String secreto = "esta-es-una-clave-secreta-para-desarrollo-solamente-cambiar-en-produccion";
        long expiracion = 3600000; // 1 hora

        Date ahora = new Date();
        Date expiracionDate = new Date(ahora.getTime() + expiracion);

        String token = Jwts.builder()
                .setSubject(usuarioId)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(expiracionDate)
                .signWith(Keys.hmacShaKeyFor(secreto.getBytes()), SignatureAlgorithm.HS512)
                .compact();

        System.out.println("Token JWT (válido por 1 hora):");
        System.out.println(token);
        System.out.println("\nPara usar en curl:");
        System.out.println("curl -H 'Authorization: Bearer " + token + "' http://localhost:8080/api/v1/alumnos/1");
    }
}
