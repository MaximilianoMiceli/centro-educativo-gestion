package ar.edu.utn.centroeducativo.gestion.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.centroeducativo.gestion.config.SecurityConfig;
import ar.edu.utn.centroeducativo.gestion.exception.EdicionBloqueadaException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.security.JwtTokenProvider;
import ar.edu.utn.centroeducativo.gestion.service.DocenteService;
import ar.edu.utn.centroeducativo.gestion.web.controller.DocenteController;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocenteController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-pruebas-para-seguridad-jwt-con-longitud-suficiente-para-hs512-0123456789",
        "app.cors.allowed-origins=http://localhost:5173"
})
class DocenteApiTest {

    private static final String JSON_EDICION = """
            {"dni":"12345678","nombre":"Juan","apellido":"Rodríguez","especialidad":"Matemática",
             "email":"juan-sin-arroba","telefono":"3624123456","estado":"ACTIVO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private DocenteService docenteService;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/docentes/buscar/legajo").param("legajo", "DOC001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profesorDevuelve403AunqueTengaToken() throws Exception {
        String token = jwtTokenProvider.generarToken("3", "profesor1", "PROFESOR");

        mockMvc.perform(get("/api/v1/docentes/buscar/legajo").param("legajo", "DOC001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminBuscaDocentePorLegajo() throws Exception {
        when(docenteService.buscarPorLegajo("DOC001")).thenReturn(docenteDTO());
        String token = jwtTokenProvider.generarToken("1", "admin", "ADMIN");

        mockMvc.perform(get("/api/v1/docentes/buscar/legajo").param("legajo", "DOC001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legajo").value("DOC001"))
                .andExpect(jsonPath("$.dniEditable").value(true));
    }

    @Test
    void adminRecibeErroresPorCampoAlEditarConDatosInvalidos() throws Exception {
        when(docenteService.actualizar(eq(1L), any())).thenThrow(new ValidacionException(
                "Hay datos inválidos en el formulario",
                Map.of("email", "Email inválido. Use formato: ejemplo@dominio.com")));
        String token = jwtTokenProvider.generarToken("1", "admin", "ADMIN");

        mockMvc.perform(put("/api/v1/docentes/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_EDICION))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").value("Email inválido. Use formato: ejemplo@dominio.com"));
    }

    @Test
    void edicionBloqueadaDevuelve423() throws Exception {
        when(docenteService.actualizar(eq(1L), any())).thenThrow(new EdicionBloqueadaException(
                "Edición bloqueada", LocalDateTime.now().plusMinutes(15)));
        String token = jwtTokenProvider.generarToken("1", "admin", "ADMIN");

        mockMvc.perform(put("/api/v1/docentes/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_EDICION))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.bloqueadaHasta").exists());
    }

    private DocenteDTO docenteDTO() {
        return new DocenteDTO(1L, "DOC001", "12345678", "Juan", "Rodríguez", "Matemática",
                "juan@centro.local", "3624123456", "ACTIVO", true, List.of("1° A"));
    }
}
