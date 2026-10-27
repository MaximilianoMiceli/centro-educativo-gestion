package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
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
class CalificacionesApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void profesorCargaEvaluacionesYNotasDeSusMaterias() throws Exception {
        DictadoPruebas.asignarMateriaConHorario(mockMvc, login("admin"), 2L);
        String profesor = login("profesor2");

        MvcResult materias = mockMvc.perform(get("/api/v1/calificaciones/dictados")
                        .header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andReturn();
        long dictadoId = ((Number) ((List<?>) JsonPath.read(materias.getResponse().getContentAsString(), "$[*].id")).get(0)).longValue();

        mockMvc.perform(post("/api/v1/calificaciones/dictados/" + dictadoId + "/evaluaciones")
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Prueba de demostración\",\"tipo\":\"EXAMEN\",\"periodo\":2,\"fecha\":\"2026-09-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Prueba de demostración"));

        mockMvc.perform(post("/api/v1/calificaciones/dictados/" + dictadoId + "/evaluaciones")
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"prueba de demostración\",\"tipo\":\"EXAMEN\",\"periodo\":2,\"fecha\":\"2026-09-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists());

        MvcResult evaluaciones = mockMvc.perform(get("/api/v1/calificaciones/dictados/" + dictadoId + "/evaluaciones")
                        .header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andReturn();
        long evaluacionId = ((Number) ((List<?>) JsonPath.read(evaluaciones.getResponse().getContentAsString(), "$[?(@.titulo=='Prueba de demostración')].id")).get(0)).longValue();

        MvcResult planilla = mockMvc.perform(get("/api/v1/calificaciones/evaluaciones/" + evaluacionId)
                        .header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alumnos.length()").value(greaterThan(0)))
                .andReturn();
        long alumnoId = ((Number) ((List<?>) JsonPath.read(planilla.getResponse().getContentAsString(), "$.alumnos[*].alumnoId")).get(0)).longValue();

        mockMvc.perform(put("/api/v1/calificaciones/evaluaciones/" + evaluacionId)
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notas\":[{\"alumnoId\":" + alumnoId + ",\"nota\":7.5}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alumnos[0].alumnoId").value(alumnoId))
                .andExpect(jsonPath("$.alumnos[0].nota").value(7.5));

        mockMvc.perform(put("/api/v1/calificaciones/evaluaciones/" + evaluacionId)
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notas\":[{\"alumnoId\":" + alumnoId + ",\"nota\":11}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profesorNoPuedeCalificarMateriasDeOtroDocente() throws Exception {
        String admin = login("admin");
        String profesor = login("profesor2");

        MvcResult todas = mockMvc.perform(get("/api/v1/calificaciones/dictados").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        List<Integer> ajenas = JsonPath.read(todas.getResponse().getContentAsString(), "$[?(@.docente!='López, María')].id");
        assertThat(ajenas).isNotEmpty();

        mockMvc.perform(get("/api/v1/calificaciones/dictados/" + ajenas.get(0) + "/evaluaciones")
                        .header("Authorization", "Bearer " + profesor))
                .andExpect(status().isForbidden());
    }

    @Test
    void alumnoYPadreVenSusCalificacionesPorMateria() throws Exception {
        String alumno = login("alumno1");
        String padre = login("padre1");

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/calificaciones").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].evaluaciones.length()").value(3));

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/calificaciones").header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/3/calificaciones").header("Authorization", "Bearer " + padre))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/calificaciones/dictados").header("Authorization", "Bearer " + alumno))
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
