package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.centroeducativo.gestion.config.RelojConfig;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CorreosApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final String FIN_DE_MES = "2026-11-30";
    private static final String DIA_DE_DEUDA = "2026-12-21";
    private static final byte[] PDF = "%PDF-1.4 transferencia de prueba".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void elRecordatorioDeFinDeMesEmiteLasFacturasYArmaElCorreoConLaFacturaAdjunta() throws Exception {
        String admin = login("admin");

        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(status().isOk())
                .andExpect(jsonPath("$.modo").value("SIN_ENVIO"))
                .andExpect(jsonPath("$.facturasGeneradas").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.enviados").value(1))
                .andExpect(jsonPath("$.conError").value(0));

        MvcResult lista = mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("RECORDATORIO"))
                .andExpect(jsonPath("$[0].estado").value("SIMULADO"))
                .andExpect(jsonPath("$[0].destinatario").value("padre1@familia.local"))
                .andExpect(jsonPath("$[0].asunto").value(containsString("Diciembre 2026")))
                .andExpect(jsonPath("$[0].asunto").value(containsString("300.000,00")))
                .andReturn();
        long id = ((Number) JsonPath.read(lista.getResponse().getContentAsString(), "$[0].id")).longValue();

        MvcResult detalle = mockMvc.perform(get("/api/v1/correos/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuerpo").value(containsString("García, Carlos")))
                .andExpect(jsonPath("$.cuerpo").value(containsString("Martínez, Ana")))
                .andExpect(jsonPath("$.cuerpo").value(containsString("Cuota base")))
                .andExpect(jsonPath("$.cuerpo").value(containsString("Total a abonar")))
                .andExpect(jsonPath("$.adjuntos.length()").value(2))
                .andReturn();
        long adjuntoId = ((Number) JsonPath.read(detalle.getResponse().getContentAsString(), "$.adjuntos[0].id"))
                .longValue();

        byte[] factura = mockMvc.perform(get("/api/v1/correos/" + id + "/adjuntos/" + adjuntoId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(factura, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void noRepiteElMismoCorreoDentroDelMes() throws Exception {
        String admin = login("admin");
        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(jsonPath("$.enviados").value(1));

        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(0))
                .andExpect(jsonPath("$.yaEnviados").value(1))
                .andExpect(jsonPath("$.facturasGeneradas").value(0));
        mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void elAvisoDeDeudaDetallaLoQueSigueSinPagarYNoLlevaAdjuntos() throws Exception {
        String admin = login("admin");
        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(status().isOk());

        ejecutar(admin, "DEUDA", DIA_DE_DEUDA).andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(1))
                .andExpect(jsonPath("$.facturasGeneradas").value(0));

        MvcResult lista = mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("DEUDA"))
                .andExpect(jsonPath("$[0].asunto").value(containsString("300.000,00")))
                .andReturn();
        long id = ((Number) JsonPath.read(lista.getResponse().getContentAsString(), "$[0].id")).longValue();
        mockMvc.perform(get("/api/v1/correos/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.cuerpo").value(containsString("Total adeudado")))
                .andExpect(jsonPath("$.cuerpo").value(containsString("vencida el 20/12/2026")))
                .andExpect(jsonPath("$.adjuntos.length()").value(0));
    }

    @Test
    void despuesDePagarSoloSeAvisaLoQueFalta() throws Exception {
        String admin = login("admin");
        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(status().isOk());
        String padre = login("padre1");
        List<Cuota> cuotas = cuotasDeLosHijos(padre);

        pagar(padre, cuotas.get(0)).andExpect(status().isCreated());
        ejecutar(admin, "DEUDA", DIA_DE_DEUDA).andExpect(jsonPath("$.enviados").value(1));
        mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$[0].tipo").value("DEUDA"))
                .andExpect(jsonPath("$[0].asunto").value(containsString("150.000,00")));
    }

    @Test
    void siNoQuedaDeudaNoSeEnviaNada() throws Exception {
        String admin = login("admin");
        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(status().isOk());
        String padre = login("padre1");
        for (Cuota cuota : cuotasDeLosHijos(padre)) {
            pagar(padre, cuota).andExpect(status().isCreated());
        }

        ejecutar(admin, "DEUDA", DIA_DE_DEUDA).andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(0))
                .andExpect(jsonPath("$.sinDeuda").value(1));
    }

    @Test
    void unPadreSinCorreoCargadoNoRecibeNadaYSeCuenta() throws Exception {
        String admin = login("admin");
        jdbc.update("update padre set email = null where dni = '40000097'");

        ejecutar(admin, "RECORDATORIO", FIN_DE_MES).andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(0))
                .andExpect(jsonPath("$.sinCorreo").value(1));
        mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void soloElAdministradorEjecutaYConsultaLosCorreos() throws Exception {
        for (String usuario : new String[] {"direccion1", "padre1", "profesor2"}) {
            String token = login(usuario);
            ejecutar(token, "RECORDATORIO", FIN_DE_MES).andExpect(status().isForbidden());
            mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(get("/api/v1/correos")).andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaUnTipoDeCorreoInexistente() throws Exception {
        ejecutar(login("admin"), "OTRO", FIN_DE_MES).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.tipo").value("Usá RECORDATORIO o DEUDA"));
    }

    private ResultActions ejecutar(String token, String tipo, String fecha) throws Exception {
        return mockMvc.perform(post("/api/v1/correos/ejecutar").param("tipo", tipo).param("fecha", fecha)
                .header("Authorization", "Bearer " + token));
    }

    private record Cuota(long facturaId, long itemId) {
    }

    /** La cuota base de cada hijo del padre en el período que emitió el recordatorio de fin de mes. */
    private List<Cuota> cuotasDeLosHijos(String tokenPadre) throws Exception {
        List<Cuota> cuotas = new java.util.ArrayList<>();
        for (long alumnoId = 1; alumnoId <= 2; alumnoId++) {
            String json = mockMvc.perform(get("/api/v1/mi-informacion/alumnos/" + alumnoId + "/facturas")
                            .header("Authorization", "Bearer " + tokenPadre))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            List<Number> facturas = JsonPath.read(json, "$[?(@.periodo=='2026-12')].id");
            List<Number> items = JsonPath.read(json,
                    "$[?(@.periodo=='2026-12')].items[?(@.concepto=='CUOTA_BASE')].id");
            cuotas.add(new Cuota(facturas.get(0).longValue(), items.get(0).longValue()));
        }
        return cuotas;
    }

    private ResultActions pagar(String tokenPadre, Cuota cuota) throws Exception {
        return mockMvc.perform(multipart("/api/v1/mi-informacion/facturas/{id}/pagos", cuota.facturaId())
                .file(new MockMultipartFile("archivo", "transferencia.pdf", "application/pdf", PDF))
                .param("itemIds", String.valueOf(cuota.itemId()))
                .param("fechaPago", LocalDate.now(RelojConfig.ZONA).toString())
                .header("Authorization", "Bearer " + tokenPadre));
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
