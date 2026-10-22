package ar.edu.utn.centroeducativo.gestion.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.centroeducativo.gestion.config.SecurityConfig;
import ar.edu.utn.centroeducativo.gestion.security.JwtTokenProvider;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.service.AlumnoService;
import ar.edu.utn.centroeducativo.gestion.service.AuthService;
import ar.edu.utn.centroeducativo.gestion.service.PermisosService;
import ar.edu.utn.centroeducativo.gestion.web.controller.AlumnoController;
import ar.edu.utn.centroeducativo.gestion.web.controller.AuthController;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.LoginResponse;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AuthController.class, AlumnoController.class})
@Import({SecurityConfig.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-pruebas-para-seguridad-jwt-con-longitud-suficiente-para-hs512-0123456789",
        "app.cors.allowed-origins=http://localhost:5173"
})
class SeguridadApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AlumnoService alumnoService;

    @MockitoBean
    private PermisosService permisosService;

    @MockitoBean
    private UsuarioActual usuarioActual;

    @BeforeEach
    void permisosDeAdmin() {
        when(permisosService.codigosPorRol("ADMIN")).thenReturn(Set.of(
                "ALUMNOS_CONSULTAR", "DOCENTES_CONSULTAR", "DOCENTES_EDITAR",
                "USUARIOS_ADMINISTRAR", "PERMISOS_ADMINISTRAR", "REPORTES_GENERAR"));
    }

    @Test
    void loginConCredencialesValidasDevuelveToken() throws Exception {
        when(authService.login("admin", "password"))
                .thenReturn(Optional.of(new LoginResponse("token-generado", "admin", "ADMIN")));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-generado"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"mal\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenPublicoAnteriorYaNoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/auth/token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void busquedaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/alumnos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void busquedaConTokenValidoDevuelveAlumno() throws Exception {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setId(1L);
        dto.setLegajo("ALU001");
        when(alumnoService.obtenerPorId(1L)).thenReturn(dto);

        String token = jwtTokenProvider.generarToken("1", "admin", "ADMIN");

        mockMvc.perform(get("/api/v1/alumnos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legajo").value("ALU001"));
    }
}
