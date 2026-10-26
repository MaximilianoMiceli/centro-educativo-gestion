package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FuncionalidadesUxApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void administradorDesactivaYReactivaUnaCuenta() throws Exception {
        String admin = login("admin");
        long direccion = idUsuario(admin, "direccion1");

        mockMvc.perform(put("/api/v1/usuarios/" + direccion + "/activo")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"direccion1\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/usuarios/" + direccion + "/activo")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void noSePuedeDesactivarLaPropiaCuenta() throws Exception {
        String admin = login("admin");
        long self = idUsuario(admin, "admin");

        mockMvc.perform(put("/api/v1/usuarios/" + self + "/activo")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.activo").value(containsString("propia cuenta")));
    }

    @Test
    void horariosSePuedenFiltrarPorDocenteYCurso() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/horarios").param("docenteId", "2")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].docente").value(containsString("López")));

        mockMvc.perform(get("/api/v1/horarios").param("cursoId", "1")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }

    @Test
    void profesorVeSuPropiaAgenda() throws Exception {
        String profesor = login("profesor2");

        mockMvc.perform(get("/api/v1/mi-agenda").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente").value(containsString("López")))
                .andExpect(jsonPath("$.cursos.length()").value(greaterThan(0)));
    }

    @Test
    void usuarioSinDocenteNoTieneAgenda() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/mi-agenda").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void listadosDeServiciosRespetanElRol() throws Exception {
        String admin = login("admin");
        String direccion = login("direccion1");

        mockMvc.perform(post("/api/v1/alumnos/1/idiomas")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idiomaId\":2}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/servicios/idiomas").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idioma").exists());

        mockMvc.perform(get("/api/v1/servicios/apoyo").header("Authorization", "Bearer " + direccion))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/servicios/enfermeria").header("Authorization", "Bearer " + direccion))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/servicios/enfermeria").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }

    @Test
    void fichasDeAlumnoYDocenteSeConsultanPorId() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/alumnos/1/ficha").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legajo").value("ALU001"));

        mockMvc.perform(get("/api/v1/docentes/2").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellido").value("López"));
    }

    private long idUsuario(String admin, String username) throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = Pattern.compile("\"id\":(\\d+),\"username\":\"" + username + "\"")
                .matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    private String login(String usuario) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = TOKEN.matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
