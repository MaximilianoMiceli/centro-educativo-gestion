package ar.edu.utn.centroeducativo.gestion.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class DocentesAltaIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminDaDeAltaUnDocenteYRechazaLegajoRepetido() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/docentes")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(docente("DOC900", "30111999", "ana.pereyra@centro.local")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.legajo").value("DOC900"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        mockMvc.perform(post("/api/v1/docentes")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(docente("DOC001", "30111888", "otro.docente@centro.local")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.legajo").value("Ya existe un docente con ese legajo"));
    }

    @Test
    void agendaDelDocenteMuestraCursosNivelMateriasYHorarios() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(get("/api/v1/docentes/2/agenda").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docente").value("López, María"))
                .andExpect(jsonPath("$.cursos[0].nivel").value("Secundario"))
                .andExpect(jsonPath("$.cursos[0].materias[0].materia").value("Matemática"))
                .andExpect(jsonPath("$.cursos[0].materias[0].horarios").isArray());
    }

    @Test
    void profesorNoPuedeDarDeAltaDocentes() throws Exception {
        String profesor = login("profesor1", "password");

        mockMvc.perform(post("/api/v1/docentes")
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(docente("DOC901", "30111777", "x901.docente@centro.local")))
                .andExpect(status().isForbidden());
    }

    private static String docente(String legajo, String dni, String email) {
        return "{\"legajo\":\"" + legajo + "\",\"dni\":\"" + dni + "\",\"nombre\":\"Ana\","
                + "\"apellido\":\"Pereyra\",\"especialidad\":\"Artes\",\"email\":\"" + email + "\","
                + "\"telefono\":\"3624999999\",\"estado\":\"ACTIVO\"}";
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
        matcher.find();
        return matcher.group(1);
    }
}
