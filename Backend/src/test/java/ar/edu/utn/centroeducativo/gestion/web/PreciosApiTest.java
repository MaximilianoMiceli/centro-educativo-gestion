package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
class PreciosApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elAdministradorVeTodosLosValoresVigentes() throws Exception {
        String admin = login("admin");

        mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuotaBase").value(150000.0))
                .andExpect(jsonPath("$.comedor").value(45000.0))
                .andExpect(jsonPath("$.deportes.length()").value(8))
                .andExpect(jsonPath("$.recorridos.length()").value(4))
                .andExpect(jsonPath("$.recorridos[0].monto").value(28000.0))
                .andExpect(jsonPath("$.recorridos[3].monto").value(40000.0));
    }

    @Test
    void cadaDeporteYRecorridoTieneUnValorDistinto() throws Exception {
        String admin = login("admin");

        MvcResult resultado = mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        java.util.List<Number> deportes = JsonPath.read(resultado.getResponse().getContentAsString(),
                "$.deportes[*].monto");
        java.util.List<Number> recorridos = JsonPath.read(resultado.getResponse().getContentAsString(),
                "$.recorridos[*].monto");
        assertThat(deportes.stream().map(Number::doubleValue).distinct().count()).isGreaterThan(1);
        assertThat(recorridos.stream().map(Number::doubleValue).distinct().count()).isEqualTo(4);
    }

    @Test
    void elAdministradorCambiaLaCuotaYUnDeporte() throws Exception {
        String admin = login("admin");
        long deporteId = primerDeporte(admin);

        mockMvc.perform(put("/api/v1/precios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuotaBase\":160000.50,\"deportes\":[{\"deporteId\":" + deporteId
                                + ",\"monto\":21000}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuotaBase").value(160000.5))
                .andExpect(jsonPath("$.comedor").value(45000.0))
                .andExpect(jsonPath("$.deportes[?(@.deporteId==" + deporteId + ")].monto").value(21000.0));

        mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.cuotaBase").value(160000.5));
    }

    @Test
    void rechazaImportesNegativosYConMasDeDosDecimales() throws Exception {
        String admin = login("admin");

        mockMvc.perform(put("/api/v1/precios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuotaBase\":-1,\"comedor\":10.123}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cuotaBase").value("El valor no puede ser negativo"))
                .andExpect(jsonPath("$.errores.comedor").value("El valor admite como máximo dos decimales"));
    }

    @Test
    void rechazaDeportesYRecorridosInexistentes() throws Exception {
        String admin = login("admin");

        mockMvc.perform(put("/api/v1/precios")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deportes\":[{\"deporteId\":99999,\"monto\":1}],"
                                + "\"recorridos\":[{\"recorridoId\":99,\"monto\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.deporte_99999").value("El deporte no existe"))
                .andExpect(jsonPath("$.errores.recorrido_99").value("El recorrido no existe"));
    }

    @Test
    void direccionConsultaPeroNoModifica() throws Exception {
        String direccion = login("direccion1");

        mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + direccion))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/precios")
                        .header("Authorization", "Bearer " + direccion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cuotaBase\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void profesoresYFamiliasNoAccedenALosPrecios() throws Exception {
        for (String usuario : new String[] {"profesor2", "padre1", "alumno1"}) {
            mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + login(usuario)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void sinSesionNoSePuedeConsultar() throws Exception {
        mockMvc.perform(get("/api/v1/precios")).andExpect(status().isUnauthorized());
    }

    private long primerDeporte(String token) throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + token))
                .andReturn();
        return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.deportes[0].deporteId"))
                .longValue();
    }

    private String login(String usuario) throws Exception {
        MvcResult resultado = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"" + usuario + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = TOKEN.matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
