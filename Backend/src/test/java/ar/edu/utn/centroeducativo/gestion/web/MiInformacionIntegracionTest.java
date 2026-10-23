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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MiInformacionIntegracionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void padreVeSoloLosHijosVinculadosEnSoloLectura() throws Exception {
        String padre = login("padre1", "password");

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos").header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1").header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ficha.legajo").value("ALU001"))
                .andExpect(jsonPath("$.materias").isArray())
                .andExpect(jsonPath("$.servicios.alumnoId").value(1));

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/3").header("Authorization", "Bearer " + padre))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/alumnos").header("Authorization", "Bearer " + padre))
                .andExpect(status().isForbidden());
    }

    @Test
    void alumnoVeSoloSuPropiaInformacion() throws Exception {
        String alumno = login("alumno1", "password");

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].legajo").value("ALU001"));

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/2").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/alumnos/1/servicios").header("Authorization", "Bearer " + alumno))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVinculaLosHijosDeUnPadre() throws Exception {
        String admin = login("admin", "password");
        long padreUsuarioId = idUsuario(admin, "padre1");

        mockMvc.perform(put("/api/v1/usuarios/" + padreUsuarioId + "/hijos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alumnoIds\":[3]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios/" + padreUsuarioId + "/hijos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].legajo").value("ALU003"));

        String padre = login("padre1", "password");
        mockMvc.perform(get("/api/v1/mi-informacion/alumnos").header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void vincularHijosRechazaUsuariosQueNoSonPadres() throws Exception {
        String admin = login("admin", "password");

        mockMvc.perform(put("/api/v1/usuarios/1/hijos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alumnoIds\":[1]}"))
                .andExpect(status().isBadRequest());
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
