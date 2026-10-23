package ar.edu.utn.centroeducativo.gestion.config;

import ar.edu.utn.centroeducativo.gestion.service.PermisosService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final PermisosService permisosService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String rol = jwt.getClaimAsString("rol");
            if (rol == null) {
                return List.of();
            }
            return permisosService.codigosPorRol(rol).stream()
                    .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                    .toList();
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/alumnos/**").hasAuthority("ALUMNOS_CONSULTAR")
                .requestMatchers(HttpMethod.GET, "/api/v1/docentes", "/api/v1/docentes/**").hasAuthority("DOCENTES_CONSULTAR")
                .requestMatchers("/api/v1/docentes", "/api/v1/docentes/**").hasAuthority("DOCENTES_EDITAR")
                .requestMatchers("/api/v1/usuarios/**").hasAuthority("USUARIOS_ADMINISTRAR")
                .requestMatchers("/api/v1/mi-informacion/**").hasAuthority("MI_INFORMACION_CONSULTAR")
                .requestMatchers("/api/v1/permisos/**", "/api/v1/roles/**").hasAuthority("PERMISOS_ADMINISTRAR")
                .requestMatchers(HttpMethod.POST, "/api/v1/alumnos", "/api/v1/alumnos/**").hasAuthority("ALUMNOS_ADMINISTRAR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/alumnos/**").hasAuthority("ALUMNOS_ADMINISTRAR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/alumnos/**").hasAuthority("ALUMNOS_ADMINISTRAR")
                .requestMatchers(HttpMethod.POST, "/api/v1/deportes/**").hasAuthority("DATOS_MAESTROS_ADMINISTRAR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/deportes/**").hasAuthority("DATOS_MAESTROS_ADMINISTRAR")
                .requestMatchers(HttpMethod.POST, "/api/v1/niveles", "/api/v1/cursos", "/api/v1/materias",
                        "/api/v1/dictados", "/api/v1/horarios").hasAuthority("DATOS_MAESTROS_ADMINISTRAR")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        http.addFilterAfter(new CambioPasswordObligatorioFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
