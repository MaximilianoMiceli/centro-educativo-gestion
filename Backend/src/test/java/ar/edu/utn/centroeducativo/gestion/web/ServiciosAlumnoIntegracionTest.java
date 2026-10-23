package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
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
class ServiciosAlumnoIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("^\\{\"id\":(\\d+)");
    private static final Pattern ID_GRUPO = Pattern.compile("\"grupos\":\\[\\{\"id\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void inscripcionADeportesRespetaLasReglas() throws Exception {
        String admin = login("admin", "password");
        long ajedrez = crearDeporteConGrupo(admin, "Ajedrez prueba", 6, "10:00", "11:00");
        long yoga = crearDeporteConGrupo(admin, "Yoga prueba", 6, "10:30", "11:30");
        long tenis = crearDeporteConGrupo(admin, "Tenis prueba", 2, "10:00", "11:00");
        long rugby = crearDeporteConGrupo(admin, "Rugby prueba", 3, "10:00", "11:00");

        inscribir(admin, 1, ajedrez).andExpect(status().isCreated());

        inscribir(admin, 1, ajedrez).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.grupoId").value(containsString("ya está inscripto")));

        inscribir(admin, 1, yoga).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.grupoId").value(containsString("Conflicto de horario")));

        inscribir(admin, 1, tenis).andExpect(status().isCreated());

        inscribir(admin, 1, rugby).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.grupoId").value(containsString("ya tiene 2 deportes")));

        mockMvc.perform(get("/api/v1/alumnos/1/servicios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deportes.length()").value(2));
    }

    @Test
    void transporteYComedorSeAsignanYQuitan() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(put("/api/v1/alumnos/1/transporte")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recorridoId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transporte.id").value(2));

        mockMvc.perform(delete("/api/v1/alumnos/1/transporte").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transporte").value(nullValue()));

        mockMvc.perform(put("/api/v1/alumnos/1/comedor")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inscripto\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comedor").value(true));
    }

    @Test
    void profesorPuedeVerServiciosPeroNoInscribir() throws Exception {
        String profesor = login("profesor1", "password");

        mockMvc.perform(get("/api/v1/alumnos/1/servicios").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/alumnos/1/comedor")
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inscripto\":true}"))
                .andExpect(status().isForbidden());
    }

    private long crearDeporteConGrupo(String admin, String nombre, int dia, String inicio, String fin) throws Exception {
        MvcResult deporte = mockMvc.perform(post("/api/v1/deportes")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long deporteId = extraerLong(ID, deporte.getResponse().getContentAsString());

        MvcResult grupo = mockMvc.perform(post("/api/v1/deportes/" + deporteId + "/grupos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelId\":3,\"diaSemana\":" + dia + ",\"horaInicio\":\"" + inicio
                                + "\",\"horaFin\":\"" + fin + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return extraerLong(ID_GRUPO, grupo.getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.ResultActions inscribir(String token, long alumnoId, long grupoId)
            throws Exception {
        return mockMvc.perform(post("/api/v1/alumnos/" + alumnoId + "/deportes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"grupoId\":" + grupoId + "}"));
    }

    private String login(String usuario, String clave) throws Exception {
        if ("profesor1".equals(usuario)) {
            SesionPruebas.restablecerPassword(mockMvc, login("admin", "password"), SesionPruebas.ID_PROFESOR1, "password");
        }
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = TOKEN.matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private static long extraerLong(Pattern patron, String cuerpo) {
        Matcher matcher = patron.matcher(cuerpo);
        assertThat(matcher.find()).as("respuesta: %s", cuerpo).isTrue();
        return Long.parseLong(matcher.group(1));
    }
}
