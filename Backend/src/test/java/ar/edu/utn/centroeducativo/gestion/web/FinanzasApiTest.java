package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos de cada prueba: el alumno 1 (García, Carlos) tiene fútbol, transporte (recorrido 2) y comedor, y paga la
 * cuota y el deporte en un solo pago; el alumno 2 (Martínez, Ana) paga su cuota completa (dos pagos en total). Los demás alumnos quedan sin pagar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FinanzasApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final byte[] PDF = "%PDF-1.4 transferencia de prueba".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    private double cuota;
    private double futbol;
    private double recorrido2;
    private double comedor;

    @Test
    void losIngresosSumanLosPagosYSeSubtotalizanPorMes() throws Exception {
        String admin = prepararPagos();

        mockMvc.perform(get("/api/v1/finanzas/ingresos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.total").value(cuota + futbol + cuota))
                .andExpect(jsonPath("$.porMes.length()").value(1))
                .andExpect(jsonPath("$.porMes[0].total").value(cuota + futbol + cuota))
                .andExpect(jsonPath("$.filas.length()").value(2))
                .andExpect(jsonPath("$.filas[0].numeroComprobante").exists());
    }

    @Test
    void elRangoDeFechasFiltraLosIngresos() throws Exception {
        String admin = prepararPagos();

        mockMvc.perform(get("/api/v1/finanzas/ingresos").param("desde", "2999-01-01")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.cantidad").value(0))
                .andExpect(jsonPath("$.total").value(0));
        mockMvc.perform(get("/api/v1/finanzas/ingresos").param("desde", hoy().toString()).param("hasta", hoy().toString())
                        .header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.cantidad").value(2));
        mockMvc.perform(get("/api/v1/finanzas/ingresos").param("desde", "2026-12-31").param("hasta", "2026-01-01")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lasFacturasPagadasPorCompletoSeAgrupanPorAnioYAlumno() throws Exception {
        String admin = prepararPagos();

        mockMvc.perform(get("/api/v1/finanzas/pagos-completos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filas.length()").value(1))
                .andExpect(jsonPath("$.filas[0].anio").value(hoy().getYear()))
                .andExpect(jsonPath("$.filas[0].alumno").value("Martínez, Ana"))
                .andExpect(jsonPath("$.filas[0].legajo").value("ALU002"))
                .andExpect(jsonPath("$.filas[0].total").value(cuota))
                .andExpect(jsonPath("$.filas[0].saldo").value(0))
                .andExpect(jsonPath("$.totalPagado").value(cuota));
    }

    @Test
    void lasFacturasConPagoParcialMuestranLoPagadoYElSaldo() throws Exception {
        String admin = prepararPagos();

        mockMvc.perform(get("/api/v1/finanzas/pagos-incompletos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filas.length()").value(1))
                .andExpect(jsonPath("$.filas[0].alumno").value("García, Carlos"))
                .andExpect(jsonPath("$.filas[0].anio").value(hoy().getYear()))
                .andExpect(jsonPath("$.filas[0].total").value(cuota + futbol + recorrido2 + comedor))
                .andExpect(jsonPath("$.filas[0].pagado").value(cuota + futbol))
                .andExpect(jsonPath("$.filas[0].saldo").value(recorrido2 + comedor))
                .andExpect(jsonPath("$.totalSaldo").value(recorrido2 + comedor));
    }

    @Test
    void losPagosPorDeporteIncluyenNivelHorarioYProfesorACargo() throws Exception {
        String admin = prepararPagos();

        MvcResult resultado = mockMvc.perform(get("/api/v1/finanzas/pagos-por-deporte")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andReturn();
        String json = resultado.getResponse().getContentAsString();
        List<String> estados = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].estado");
        List<String> deportes = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].deporte");
        List<String> horarios = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].horario");
        List<String> niveles = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].nivel");
        List<String> profesores = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].profesor");
        List<String> fechas = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].fechaPago");

        assertThat(estados).containsExactly("PAGADO");
        assertThat(deportes).containsExactly("Fútbol");
        assertThat(horarios).containsExactly("Jueves 14:00 a 15:30");
        assertThat(niveles).containsExactly("Secundario");
        assertThat(profesores.get(0)).isNotBlank().isNotEqualTo("-");
        assertThat(fechas).containsExactly(hoy().toString());
        assertThat(((Number) JsonPath.read(json, "$.totalPagado")).doubleValue()).isEqualTo(futbol);
        assertThat(((Number) JsonPath.read(json, "$.totalPendiente")).doubleValue()).isGreaterThan(0);
    }

    @Test
    void losPagosPorRecorridoSeparanLoPagadoDeLoPendiente() throws Exception {
        String admin = prepararPagos();

        MvcResult resultado = mockMvc.perform(get("/api/v1/finanzas/pagos-por-recorrido")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andReturn();
        String json = resultado.getResponse().getContentAsString();
        List<String> recorridos = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].recorrido");
        List<String> estados = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].estado");
        List<Number> montos = JsonPath.read(json, "$.filas[?(@.legajo=='ALU001')].monto");

        assertThat(recorridos).containsExactly("Recorrido 2 - Sur");
        assertThat(estados).containsExactly("PENDIENTE");
        assertThat(montos.get(0).doubleValue()).isEqualTo(recorrido2);
        assertThat(((Number) JsonPath.read(json, "$.totalPagado")).doubleValue()).isZero();
        assertThat(((Number) JsonPath.read(json, "$.totalPendiente")).doubleValue()).isGreaterThanOrEqualTo(recorrido2);
    }

    @Test
    void direccionConsultaLosListadosPeroLasFamiliasYLosProfesoresNo() throws Exception {
        String[] rutas = {"ingresos", "pagos-completos", "pagos-incompletos", "pagos-por-deporte", "pagos-por-recorrido"};
        for (String ruta : rutas) {
            mockMvc.perform(get("/api/v1/finanzas/" + ruta).header("Authorization", "Bearer " + login("direccion1")))
                    .andExpect(status().isOk());
            for (String usuario : new String[] {"padre1", "alumno1", "profesor2"}) {
                mockMvc.perform(get("/api/v1/finanzas/" + ruta).header("Authorization", "Bearer " + login(usuario)))
                        .andExpect(status().isForbidden());
            }
            mockMvc.perform(get("/api/v1/finanzas/" + ruta)).andExpect(status().isUnauthorized());
        }
    }

    /** Arma el escenario descrito arriba y devuelve el token del administrador. */
    private String prepararPagos() throws Exception {
        String admin = login("admin");
        MvcResult precios = mockMvc.perform(get("/api/v1/precios").header("Authorization", "Bearer " + admin))
                .andReturn();
        String json = precios.getResponse().getContentAsString();
        cuota = ((Number) JsonPath.read(json, "$.cuotaBase")).doubleValue();
        comedor = ((Number) JsonPath.read(json, "$.comedor")).doubleValue();
        futbol = ((List<Number>) JsonPath.read(json, "$.deportes[?(@.deporte=='Fútbol')].monto")).get(0).doubleValue();
        recorrido2 = ((List<Number>) JsonPath.read(json, "$.recorridos[?(@.recorridoId==2)].monto")).get(0).doubleValue();

        MvcResult deportes = mockMvc.perform(get("/api/v1/deportes").header("Authorization", "Bearer " + admin))
                .andReturn();
        List<Integer> grupos = JsonPath.read(deportes.getResponse().getContentAsString(),
                "$[?(@.nombre=='Fútbol')].grupos[?(@.nivel=='Secundario')].id");
        mockMvc.perform(post("/api/v1/alumnos/1/deportes").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"grupoId\":" + grupos.get(0) + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/v1/alumnos/1/transporte").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"recorridoId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/alumnos/1/comedor").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"inscripto\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/facturacion/generar").param("periodo", "2026-12")
                .header("Authorization", "Bearer " + admin)).andExpect(status().isOk());

        String padre = login("padre1");
        pagar(padre, 1, "CUOTA_BASE", "DEPORTE");
        pagar(padre, 2, "CUOTA_BASE");
        return admin;
    }

    private void pagar(String tokenPadre, long alumnoId, String... conceptos) throws Exception {
        String json = mockMvc.perform(get("/api/v1/mi-informacion/alumnos/" + alumnoId + "/facturas")
                        .header("Authorization", "Bearer " + tokenPadre))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long facturaId = ((Number) JsonPath.read(json, "$[0].id")).longValue();
        var solicitud = multipart("/api/v1/mi-informacion/facturas/{id}/pagos", facturaId)
                .file(new MockMultipartFile("archivo", "transferencia.pdf", "application/pdf", PDF))
                .param("fechaPago", hoy().toString())
                .header("Authorization", "Bearer " + tokenPadre);
        for (String concepto : conceptos) {
            List<Number> ids = JsonPath.read(json, "$[0].items[?(@.concepto=='" + concepto + "')].id");
            solicitud.param("itemIds", String.valueOf(ids.get(0).longValue()));
        }
        ResultActions resultado = mockMvc.perform(solicitud);
        resultado.andExpect(status().isCreated());
    }

    private static LocalDate hoy() {
        return LocalDate.now(RelojConfig.ZONA);
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
