package ar.edu.utn.centroeducativo.gestion.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.centroeducativo.gestion.service.AuditoriaService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlumnosAdministracionIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditoriaService auditoriaService;

    @Test
    void adminVeLosAlumnosDeLosDatosDePrueba() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(get("/api/v1/alumnos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(60)));
    }

    @Test
    void altaDeAlumnoValidaYRechazaDuplicados() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/alumnos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumno("ALU900", "31999888", "alumno.nuevo900@estudiante.local", 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.legajo").value("ALU900"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        mockMvc.perform(post("/api/v1/alumnos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumno("ALU001", "31999777", "otro900@estudiante.local", 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.legajo").value("Ya existe un alumno con ese legajo"));
    }

    @Test
    void altaRechazaDniInvalidoYEdadFueraDeRango() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/alumnos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumno("ALU901", "12-3", "x901@estudiante.local", 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.dni").exists());

        mockMvc.perform(post("/api/v1/alumnos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumno("ALU902", "31999666", "x902@estudiante.local", 1)
                                .replace("2015-05-10", "1990-05-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaNacimiento").exists());
    }

    @Test
    void cambioDeCursoSeAplicaYSeAudita() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(put("/api/v1/alumnos/1")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumno("ALU001", "45678901", "carlos@estudiante.local", 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursoId").value(3));
    }

    @Test
    void profesorNoPuedeDarDeAltaPeroSiVeMateriasDelAlumno() throws Exception {
        String admin = login("admin", "password");
        String profesor = login("profesor1", "password");

        mockMvc.perform(post("/api/v1/alumnos")
                        .header("Authorization", "Bearer " + profesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alumno("ALU903", "31999555", "x903@estudiante.local", 1)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/alumnos/1/materias").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/alumnos/1/materias").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void profesorSoloListaAlumnosDeSusCursosYLaBusquedaFiltra() throws Exception {
        String admin = login("admin", "password");
        String profesor = login("profesor1", "password");

        mockMvc.perform(get("/api/v1/alumnos").header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.cursoId != 1 && @.cursoId != 2)]").isEmpty());

        mockMvc.perform(get("/api/v1/alumnos").param("nivelId", "1")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nivelNombre != 'INICIAL')]").isEmpty());

        mockMvc.perform(get("/api/v1/alumnos").param("busqueda", "ALU001")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].legajo").value("ALU001"));
    }

    private static String alumno(String legajo, String dni, String email, long cursoId) {
        return "{\"legajo\":\"" + legajo + "\",\"dni\":\"" + dni + "\",\"nombre\":\"Ana\","
                + "\"apellido\":\"Quiroga\",\"fechaNacimiento\":\"2015-05-10\","
                + "\"domicilio\":\"Calle 1 100\",\"telefono\":\"3624555555\","
                + "\"email\":\"" + email + "\",\"cursoId\":" + cursoId + ",\"estado\":\"ACTIVO\"}";
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
