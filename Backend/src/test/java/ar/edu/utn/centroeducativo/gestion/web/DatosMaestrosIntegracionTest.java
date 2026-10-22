package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.centroeducativo.gestion.service.AuditoriaService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DatosMaestrosIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("^\\{\"id\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditoriaService auditoriaService;

    @Test
    void flujoCompletoDeAsignacionYHorarios() throws Exception {
        String admin = login("admin", "password");

        long cursoId = idDe(mockMvc.perform(post("/api/v1/cursos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelId\":1,\"anio\":6,\"seccion\":\"z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seccion").value("Z"))
                .andReturn());

        long materiaId = idDe(mockMvc.perform(post("/api/v1/materias")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Historia de prueba\"}"))
                .andExpect(status().isCreated())
                .andReturn());

        long dictadoId = idDe(mockMvc.perform(post("/api/v1/dictados")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docenteId\":2,\"materiaId\":" + materiaId + ",\"cursoId\":" + cursoId + "}"))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(post("/api/v1/horarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horario(dictadoId, 5, "17:00", "18:00")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/horarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horario(dictadoId, 5, "17:30", "18:30")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.horaInicio").exists());

        mockMvc.perform(post("/api/v1/horarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horario(dictadoId, 5, "18:00", "17:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.horaFin").value("La hora de fin debe ser posterior a la de inicio"));

        mockMvc.perform(get("/api/v1/horarios").param("dictadoId", String.valueOf(dictadoId))
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listadosQueUsaLaPantallaDevuelvenDatos() throws Exception {
        String admin = login("admin", "password");
        String profesor = login("profesor1", "password");

        mockMvc.perform(get("/api/v1/docentes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/v1/docentes").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/dictados").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/niveles").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void materiaDuplicadaSeRechaza() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/materias")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Historia de prueba\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/materias")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"historia de prueba\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("Ya existe una materia con ese nombre"));
    }

    @Test
    void cursoDuplicadoEnElMismoNivelSeRechaza() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/cursos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nivelId\":3,\"anio\":1,\"seccion\":\"A\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.seccion").value("Ya existe un curso con ese año y sección en el nivel"));
    }

    @Test
    void profesorNoPuedeAdministrarDatosMaestros() throws Exception {
        String profesor = login("profesor1", "password");

        mockMvc.perform(post("/api/v1/materias")
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Biología\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/materias").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk());
    }

    private static String horario(long dictadoId, int dia, String inicio, String fin) {
        return "{\"dictadoId\":" + dictadoId + ",\"diaSemana\":" + dia
                + ",\"horaInicio\":\"" + inicio + "\",\"horaFin\":\"" + fin + "\"}";
    }

    private long idDe(MvcResult resultado) throws Exception {
        String cuerpo = resultado.getResponse().getContentAsString();
        Matcher matcher = ID.matcher(cuerpo);
        assertThat(matcher.find()).as("respuesta: %s", cuerpo).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    private String login(String usuario, String clave) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = TOKEN.matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
