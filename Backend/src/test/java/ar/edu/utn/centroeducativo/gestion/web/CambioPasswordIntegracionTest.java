package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
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
class CambioPasswordIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final Pattern PASSWORD_TEMPORAL = Pattern.compile("\"passwordTemporal\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void usuarioNuevoDebeCambiarLaPasswordAntesDeOperar() throws Exception {
        String admin = extraer(TOKEN, login("admin", "password"));

        MvcResult alta = mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cambio.test\",\"email\":\"cambio.test@centro.local\","
                                + "\"tipoDocumento\":\"DNI\",\"numeroDocumento\":\"87654321\",\"rol\":\"PROFESOR\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String temporal = extraer(PASSWORD_TEMPORAL, alta.getResponse().getContentAsString());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cambio.test\",\"password\":\"" + temporal + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarPassword").value(true))
                .andReturn();
        String tokenInicial = extraer(TOKEN, login.getResponse().getContentAsString());

        mockMvc.perform(get("/api/v1/alumnos").header("Authorization", "Bearer " + tokenInicial))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CAMBIO_PASSWORD_REQUERIDO"));

        MvcResult cambio = mockMvc.perform(post("/api/v1/auth/cambiar-password")
                        .header("Authorization", "Bearer " + tokenInicial)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"" + temporal + "\",\"passwordNueva\":\"NuevaClave2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarPassword").value(false))
                .andReturn();
        String tokenNuevo = extraer(TOKEN, cambio.getResponse().getContentAsString());

        mockMvc.perform(get("/api/v1/alumnos").header("Authorization", "Bearer " + tokenNuevo))
                .andExpect(status().isOk());
    }

    private String login(String usuario, String clave) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"password\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private static String extraer(Pattern patron, String cuerpo) {
        Matcher matcher = patron.matcher(cuerpo);
        assertThat(matcher.find()).as("respuesta: %s", cuerpo).isTrue();
        return matcher.group(1);
    }
}
