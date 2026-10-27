package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
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
class MiServiciosAlumnoApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void alumnoGestionaSusPropiosServicios() throws Exception {
        String alumno = login("alumno1");

        mockMvc.perform(get("/api/v1/mi-informacion/servicios").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alumnoId").value(1));

        mockMvc.perform(put("/api/v1/mi-informacion/servicios/comedor")
                        .header("Authorization", "Bearer " + alumno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inscripto\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comedor").value(true));

        mockMvc.perform(put("/api/v1/mi-informacion/servicios/transporte")
                        .header("Authorization", "Bearer " + alumno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recorridoId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transporte.id").value(1));

        mockMvc.perform(delete("/api/v1/mi-informacion/servicios/transporte").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transporte").doesNotExist());

        mockMvc.perform(post("/api/v1/mi-informacion/servicios/idiomas")
                        .header("Authorization", "Bearer " + alumno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idiomaId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idiomas[0].idioma").value("Francés"));
    }

    @Test
    void alumnoSeInscribeAUnDeporteDeSuNivel() throws Exception {
        String alumno = login("alumno1");
        MvcResult deportes = mockMvc.perform(get("/api/v1/deportes").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk())
                .andReturn();
        List<Integer> grupos = JsonPath.read(deportes.getResponse().getContentAsString(),
                "$[?(@.nombre=='Atletismo')].grupos[?(@.nivel=='Secundario')].id");
        assertThat(grupos).isNotEmpty();

        mockMvc.perform(post("/api/v1/mi-informacion/servicios/deportes")
                        .header("Authorization", "Bearer " + alumno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grupoId\":" + grupos.get(0) + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deportes[*].deporte", org.hamcrest.Matchers.hasItem("Atletismo")));
    }

    @Test
    void padreNoPuedeGestionarServiciosDeAlumnos() throws Exception {
        String padre = login("padre1");

        mockMvc.perform(get("/api/v1/mi-informacion/servicios").header("Authorization", "Bearer " + padre))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/mi-informacion/servicios/comedor")
                        .header("Authorization", "Bearer " + padre)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inscripto\":true}"))
                .andExpect(status().isForbidden());
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
