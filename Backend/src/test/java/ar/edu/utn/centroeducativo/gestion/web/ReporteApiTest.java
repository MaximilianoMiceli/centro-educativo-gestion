package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
class ReporteApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaElCatalogoDeNueveReportes() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/reportes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[2].codigo").value("REP-003"))
                .andExpect(jsonPath("$[2].slug").value("alumnos-curso"));
    }

    @Test
    void generaElReporteEnJsonConSecciones() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/reportes/alumnos-curso").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("REP-003"))
                .andExpect(jsonPath("$.secciones[0].columnas.length()").value(5))
                .andExpect(jsonPath("$.secciones[0].filas.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void fichaDelAlumnoTieneSeisSecciones() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/reportes/alumno-ficha")
                        .param("alumnoId", "1")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secciones.length()").value(6))
                .andExpect(jsonPath("$.secciones[0].filas[0][1]").value("ALU001"))
                .andExpect(jsonPath("$.filtros[0]").value(org.hamcrest.Matchers.containsString("ALU001")));
    }

    @Test
    void fichaSinAlumnoDevuelveValidacion() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/reportes/alumno-ficha").header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.alumnoId").exists());
    }

    @Test
    void deportePorNivelExigeElNivel() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/reportes/alumnos-deporte-nivel").header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nivelId").exists());
    }

    @Test
    void reporteInexistenteDevuelveNotFound() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/reportes/no-existe").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void descargaPdfConEncabezadoDelCentro() throws Exception {
        String admin = login("admin");

        MvcResult resultado = mockMvc.perform(get("/api/v1/reportes/alumnos-curso/pdf")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("REP-003-alumnos-curso.pdf")))
                .andReturn();

        byte[] pdf = resultado.getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void descargaExcelConFormatoXlsx() throws Exception {
        String admin = login("admin");

        MvcResult resultado = mockMvc.perform(get("/api/v1/reportes/docentes-nivel/excel")
                        .param("nivelId", "3")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(content().contentType(XLSX))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("REP-005-docentes-nivel.xlsx")))
                .andReturn();

        byte[] excel = resultado.getResponse().getContentAsByteArray();
        assertThat(new String(excel, 0, 2, StandardCharsets.US_ASCII)).isEqualTo("PK");
    }

    @Test
    void padreNoPuedeGenerarReportes() throws Exception {
        String padre = login("padre1");

        mockMvc.perform(get("/api/v1/reportes").header("Authorization", "Bearer " + padre))
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
