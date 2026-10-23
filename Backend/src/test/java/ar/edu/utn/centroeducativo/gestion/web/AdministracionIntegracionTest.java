package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ar.edu.utn.centroeducativo.gestion.service.AuditoriaService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdministracionIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditoriaService auditoriaService;

    @Test
    void altaDeProfesorConDocenteExistenteGeneraCredencialesQueFuncionan() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nuevo.prof","email":"nuevo.prof@centro.local",
                                 "tipoDocumento":"DNI","numeroDocumento":"87654321","rol":"PROFESOR",
                                 "password":"ClaveInicial1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("PROFESOR"));

        assertThat(login("nuevo.prof", "ClaveInicial1")).isNotBlank();
    }

    @Test
    void adminRestableceLaContrasenaDeUnUsuario() throws Exception {
        String admin = login("admin", "password");

        MvcResult alta = mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo.prof\",\"email\":\"nuevo.prof@centro.local\","
                                + "\"tipoDocumento\":\"DNI\",\"numeroDocumento\":\"87654321\",\"rol\":\"PROFESOR\","
                                + "\"password\":\"ClaveInicial1\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher id = Pattern.compile("\"id\":(\\d+)").matcher(alta.getResponse().getContentAsString());
        assertThat(id.find()).isTrue();

        mockMvc.perform(put("/api/v1/usuarios/" + id.group(1) + "/password")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"OtraClave2026\",\"debeCambiarPassword\":false}"))
                .andExpect(status().isOk());

        assertThat(login("nuevo.prof", "OtraClave2026")).isNotBlank();
    }

    @Test
    void altaRechazaDocumentoYaVinculadoAOtroUsuario() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"otro.prof","email":"otro.prof@centro.local",
                                 "tipoDocumento":"DNI","numeroDocumento":"12345678","rol":"PROFESOR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroDocumento").value("Ya existe un usuario con ese documento"));
    }

    @Test
    void altaDeProfesorSinDocenteExistenteEsRechazada() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"fantasma.prof","email":"fantasma@centro.local",
                                 "tipoDocumento":"DNI","numeroDocumento":"99999999","rol":"PROFESOR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroDocumento").value("No existe un docente con ese DNI"));
    }

    @Test
    void quitarPermisoAlRolProfesorBloqueaLaConsultaDeDocentesDeInmediato() throws Exception {
        String admin = login("admin", "password");
        String profesor = login("profesor1", "password");

        mockMvc.perform(get("/api/v1/docentes/buscar/legajo").param("legajo", "DOC001")
                        .header("Authorization", "Bearer " + profesor))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/roles/PROFESOR/permisos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permisos\":[\"ALUMNOS_CONSULTAR\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos[0]").value("ALUMNOS_CONSULTAR"));

        mockMvc.perform(get("/api/v1/docentes/buscar/legajo").param("legajo", "DOC001")
                        .header("Authorization", "Bearer " + profesor))
                .andExpect(status().isForbidden());
    }

    @Test
    void noSePuedeQuitarPermisosAdministrarAlAdmin() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(put("/api/v1/roles/ADMIN/permisos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permisos\":[\"ALUMNOS_CONSULTAR\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.permisos").value("El rol ADMIN debe conservar PERMISOS_ADMINISTRAR"));
    }

    @Test
    void permisoInexistenteEsRechazado() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(put("/api/v1/roles/PROFESOR/permisos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permisos\":[\"INVENTADO\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.permisos").value("Permisos inexistentes: INVENTADO"));
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
        return extraer(TOKEN, resultado.getResponse().getContentAsString());
    }

    private static String extraer(Pattern patron, String cuerpo) {
        Matcher matcher = patron.matcher(cuerpo);
        assertThat(matcher.find()).as("respuesta: %s", cuerpo).isTrue();
        return matcher.group(1);
    }
}
