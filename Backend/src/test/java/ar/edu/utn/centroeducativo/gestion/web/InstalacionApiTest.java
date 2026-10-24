package ar.edu.utn.centroeducativo.gestion.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

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
class InstalacionApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaLasInstalacionesDePrueba() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/instalaciones").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].nombre").value("Cancha de fútbol 1"));
    }

    @Test
    void creaUnaInstalacion() throws Exception {
        String admin = login("admin");

        mockMvc.perform(post("/api/v1/instalaciones")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Biblioteca\",\"tipo\":\"otra\",\"estado\":\"ACTIVA\",\"capacidad\":30}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("OTRA"))
                .andExpect(jsonPath("$.capacidad").value(30));
    }

    @Test
    void rechazaNombreDuplicadoIgnorandoMayusculas() throws Exception {
        String admin = login("admin");

        mockMvc.perform(post("/api/v1/instalaciones")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"comedor\",\"tipo\":\"COMEDOR\",\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("Ya existe una instalación con ese nombre"));
    }

    @Test
    void validaTipoEstadoYCapacidad() throws Exception {
        String admin = login("admin");

        mockMvc.perform(post("/api/v1/instalaciones")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sala nueva\",\"tipo\":\"VOLADOR\",\"estado\":\"\",\"capacidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.tipo").exists())
                .andExpect(jsonPath("$.errores.estado").exists())
                .andExpect(jsonPath("$.errores.capacidad").exists());
    }

    @Test
    void cambiaElEstadoDeUnaInstalacion() throws Exception {
        String admin = login("admin");
        long id = idPorNombre(admin, "Pileta de natación");

        mockMvc.perform(put("/api/v1/instalaciones/" + id)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pileta de natación\",\"tipo\":\"PILETA\",\"estado\":\"EN_MANTENIMIENTO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_MANTENIMIENTO"));
    }

    @Test
    void padreNoPuedeCrearInstalaciones() throws Exception {
        String padre = login("padre1");

        mockMvc.perform(post("/api/v1/instalaciones")
                        .header("Authorization", "Bearer " + padre)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sala\",\"tipo\":\"OTRA\",\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isForbidden());
    }

    private long idPorNombre(String admin, String nombre) throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/v1/instalaciones").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = Pattern.compile("\"id\":(\\d+),\"nombre\":\"" + nombre + "\"")
                .matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
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
