package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class ServiciosComplementariosApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void todoDeporteTieneProfesorResponsable() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/deportes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[?(@.docenteResponsableId == null)]").isEmpty());
    }

    @Test
    void noSePuedeCrearUnDeporteSinResponsable() throws Exception {
        String admin = login("admin");

        mockMvc.perform(post("/api/v1/deportes")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Rugby\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.docenteResponsableId").exists());
    }

    @Test
    void inscribeAUnIdiomaSinDuplicarlo() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/idiomas").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(post("/api/v1/alumnos/1/idiomas")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idiomaId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idiomas.length()").value(1))
                .andExpect(jsonPath("$.idiomas[0].idioma").value("Inglés"));

        mockMvc.perform(post("/api/v1/alumnos/1/idiomas")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idiomaId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.idiomaId").exists());
    }

    @Test
    void registraYDaDeBajaElApoyoEstudiantil() throws Exception {
        String admin = login("admin");

        mockMvc.perform(put("/api/v1/alumnos/1/apoyo")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observaciones\":\"Refuerzo de matemática\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apoyo.observaciones").value("Refuerzo de matemática"));

        mockMvc.perform(delete("/api/v1/alumnos/1/apoyo").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apoyo").doesNotExist());
    }

    @Test
    void enfermeriaSoloLaVenQuienAdministraAlumnos() throws Exception {
        String admin = login("admin");
        String direccion = login("direccion1");

        mockMvc.perform(post("/api/v1/alumnos/1/enfermeria")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Dolor de cabeza\",\"observaciones\":\"Se le dio agua\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motivo").value("Dolor de cabeza"))
                .andExpect(jsonPath("$.registradoPor").value("admin"));

        mockMvc.perform(get("/api/v1/alumnos/1/enfermeria").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/alumnos/1/enfermeria").header("Authorization", "Bearer " + direccion))
                .andExpect(status().isForbidden());
    }

    @Test
    void renombraUnRecorridoSinRepetirNombres() throws Exception {
        String admin = login("admin");

        mockMvc.perform(put("/api/v1/recorridos/1")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Recorrido 1 - Norte renovado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Recorrido 1 - Norte renovado"));

        mockMvc.perform(put("/api/v1/recorridos/1")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Recorrido 2 - Sur\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());

        mockMvc.perform(get("/api/v1/recorridos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void existeUnUsuarioDeDemostracionPorRol() throws Exception {
        login("admin");
        login("direccion1");
        String profesor = login("profesor2");

        mockMvc.perform(get("/api/v1/alumnos").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
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
