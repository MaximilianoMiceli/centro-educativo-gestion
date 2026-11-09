package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FacturacionPagosApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final String PERIODO = "2026-12";
    private static final byte[] PDF = "%PDF-1.4 transferencia de prueba".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generaLasFacturasDelMesYNoLasDuplica() throws Exception {
        String admin = login("admin");

        MvcResult primera = generar(admin, PERIODO).andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value(PERIODO))
                .andExpect(jsonPath("$.generadas").value(greaterThan(0)))
                .andExpect(jsonPath("$.omitidas").value(0))
                .andReturn();
        int generadas = JsonPath.read(primera.getResponse().getContentAsString(), "$.generadas");

        generar(admin, PERIODO).andExpect(status().isOk())
                .andExpect(jsonPath("$.generadas").value(0))
                .andExpect(jsonPath("$.omitidas").value(generadas));
    }

    @Test
    void laFacturaSumaLaCuotaBaseYLosServiciosDelAlumno() throws Exception {
        String admin = login("admin");
        inscribirEnServicios(admin);
        generar(admin, PERIODO).andExpect(status().isOk());

        String padre = login("padre1");
        MvcResult resultado = mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/facturas")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].periodo").value(PERIODO))
                .andExpect(jsonPath("$[0].vencimiento").value("2026-12-20"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].items.length()").value(4))
                .andReturn();
        String json = resultado.getResponse().getContentAsString();
        List<String> conceptos = JsonPath.read(json, "$[0].items[*].concepto");
        assertThat(conceptos).containsExactlyInAnyOrder("CUOTA_BASE", "DEPORTE", "TRANSPORTE", "COMEDOR");
        List<Number> montos = JsonPath.read(json, "$[0].items[*].monto");
        double suma = montos.stream().mapToDouble(Number::doubleValue).sum();
        assertThat(((Number) JsonPath.read(json, "$[0].total")).doubleValue()).isEqualTo(suma);
        assertThat(((Number) JsonPath.read(json, "$[0].saldo")).doubleValue()).isEqualTo(suma);
    }

    @Test
    void elPadrePagaEnDosPartesYLaFacturaQuedaCompleta() throws Exception {
        String admin = login("admin");
        inscribirEnServicios(admin);
        generar(admin, PERIODO).andExpect(status().isOk());
        String padre = login("padre1");

        MvcResult factura = facturaDeAlumno1(padre);
        long facturaId = ((Number) JsonPath.read(factura.getResponse().getContentAsString(), "$[0].id")).longValue();
        long cuota = itemId(factura, "CUOTA_BASE");

        pagar(padre, facturaId, List.of(cuota), hoy(), PDF).andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroComprobante").value(containsString("C-")))
                .andExpect(jsonPath("$.itemIds.length()").value(1));
        mockMvc.perform(get("/api/v1/mi-informacion/facturas/" + facturaId).header("Authorization", "Bearer " + padre))
                .andExpect(jsonPath("$.estado").value("PARCIAL"))
                .andExpect(jsonPath("$.pagos.length()").value(1));

        List<Long> resto = List.of(itemId(factura, "DEPORTE"), itemId(factura, "TRANSPORTE"),
                itemId(factura, "COMEDOR"));
        pagar(padre, facturaId, resto, hoy(), PDF).andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/mi-informacion/facturas/" + facturaId).header("Authorization", "Bearer " + padre))
                .andExpect(jsonPath("$.estado").value("PAGADA"))
                .andExpect(jsonPath("$.saldo").value(0.0))
                .andExpect(jsonPath("$.pagos.length()").value(2));
    }

    @Test
    void rechazaPagosInvalidos() throws Exception {
        String admin = login("admin");
        inscribirEnServicios(admin);
        generar(admin, PERIODO).andExpect(status().isOk());
        String padre = login("padre1");
        MvcResult factura = facturaDeAlumno1(padre);
        long facturaId = ((Number) JsonPath.read(factura.getResponse().getContentAsString(), "$[0].id")).longValue();
        long cuota = itemId(factura, "CUOTA_BASE");

        pagar(padre, facturaId, List.of(cuota), hoy().plusDays(1), PDF).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.fechaPago").value("La fecha de la transferencia no puede ser futura"));
        pagar(padre, facturaId, List.of(cuota), hoy(), "esto no es un comprobante".getBytes(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.archivo").value("Solo se admiten archivos PDF, PNG o JPG"));
        pagar(padre, facturaId, List.of(999999L), hoy(), PDF).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.items").value("Hay ítems que no pertenecen a la factura"));

        pagar(padre, facturaId, List.of(cuota), hoy(), PDF).andExpect(status().isCreated());
        pagar(padre, facturaId, List.of(cuota), hoy(), PDF).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.items").value("Hay ítems que ya fueron pagados"));
    }

    @Test
    void soloLosPadresPuedenPagarYSoloLaFacturaDeSusHijos() throws Exception {
        String admin = login("admin");
        generar(admin, PERIODO).andExpect(status().isOk());
        String padre = login("padre1");
        MvcResult factura = facturaDeAlumno1(padre);
        long facturaId = ((Number) JsonPath.read(factura.getResponse().getContentAsString(), "$[0].id")).longValue();
        long cuota = itemId(factura, "CUOTA_BASE");

        pagar(login("alumno1"), facturaId, List.of(cuota), hoy(), PDF).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/3/facturas").header("Authorization", "Bearer " + padre))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAlumnoVeSusFacturasPeroUnProfesorNoAccede() throws Exception {
        String admin = login("admin");
        generar(admin, PERIODO).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/facturas")
                        .header("Authorization", "Bearer " + login("alumno1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/facturas")
                        .header("Authorization", "Bearer " + login("profesor2")))
                .andExpect(status().isForbidden());
    }

    @Test
    void generarLaFacturacionRequierePermisoDeAdministracion() throws Exception {
        for (String usuario : new String[] {"direccion1", "padre1", "profesor2"}) {
            generar(login(usuario), PERIODO).andExpect(status().isForbidden());
        }
        mockMvc.perform(post("/api/v1/facturacion/generar").param("periodo", PERIODO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validaElFormatoDelPeriodo() throws Exception {
        String admin = login("admin");

        generar(admin, "2026-13").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.periodo").value("Usá el formato AAAA-MM, por ejemplo 2026-12"));
        generar(admin, "diciembre").andExpect(status().isBadRequest());
    }

    @Test
    void descargaLaFacturaElComprobanteYElArchivoAdjunto() throws Exception {
        String admin = login("admin");
        generar(admin, PERIODO).andExpect(status().isOk());
        String padre = login("padre1");
        MvcResult factura = facturaDeAlumno1(padre);
        long facturaId = ((Number) JsonPath.read(factura.getResponse().getContentAsString(), "$[0].id")).longValue();
        MvcResult pago = pagar(padre, facturaId, List.of(itemId(factura, "CUOTA_BASE")), hoy(), PDF)
                .andExpect(status().isCreated()).andReturn();
        long pagoId = ((Number) JsonPath.read(pago.getResponse().getContentAsString(), "$.id")).longValue();

        byte[] facturaPdf = mockMvc.perform(get("/api/v1/mi-informacion/facturas/" + facturaId + "/pdf")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(facturaPdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");

        byte[] comprobante = mockMvc.perform(get("/api/v1/mi-informacion/pagos/" + pagoId + "/comprobante")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(comprobante, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");

        byte[] adjunto = mockMvc.perform(get("/api/v1/mi-informacion/pagos/" + pagoId + "/archivo")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(adjunto).isEqualTo(PDF);
    }

    @Test
    void elHistorialDeDeudaDetallaCadaItemConSuEstado() throws Exception {
        String admin = login("admin");
        inscribirEnServicios(admin);
        generar(admin, PERIODO).andExpect(status().isOk());
        String padre = login("padre1");
        MvcResult factura = facturaDeAlumno1(padre);
        long facturaId = ((Number) JsonPath.read(factura.getResponse().getContentAsString(), "$[0].id")).longValue();
        pagar(padre, facturaId, List.of(itemId(factura, "CUOTA_BASE")), hoy(), PDF).andExpect(status().isCreated());

        MvcResult deuda = mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/deuda")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(4))
                .andReturn();
        String json = deuda.getResponse().getContentAsString();
        List<String> estados = JsonPath.read(json, "$.items[*].estado");
        assertThat(estados.stream().filter("PAGADO"::equals).count()).isEqualTo(1);
        assertThat(estados.stream().filter("PENDIENTE"::equals).count()).isEqualTo(3);
        double facturado = ((Number) JsonPath.read(json, "$.totalFacturado")).doubleValue();
        double pagado = ((Number) JsonPath.read(json, "$.totalPagado")).doubleValue();
        double adeudado = ((Number) JsonPath.read(json, "$.totalAdeudado")).doubleValue();
        assertThat(facturado - pagado).isEqualTo(adeudado);
    }

    @Test
    void filtraLasFacturasPorFechaDeEmision() throws Exception {
        String admin = login("admin");
        generar(admin, PERIODO).andExpect(status().isOk());
        String padre = login("padre1");

        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/facturas").param("desde", "2999-01-01")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/facturas")
                        .param("desde", "2026-12-31").param("hasta", "2026-01-01")
                        .header("Authorization", "Bearer " + padre))
                .andExpect(status().isBadRequest());
    }

    private ResultActions generar(String token, String periodo) throws Exception {
        return mockMvc.perform(post("/api/v1/facturacion/generar").param("periodo", periodo)
                .header("Authorization", "Bearer " + token));
    }

    private ResultActions pagar(String token, long facturaId, List<Long> itemIds, LocalDate fecha, byte[] contenido)
            throws Exception {
        var solicitud = multipart("/api/v1/mi-informacion/facturas/{id}/pagos", facturaId)
                .file(new MockMultipartFile("archivo", "transferencia.pdf", "application/pdf", contenido))
                .param("fechaPago", fecha.toString())
                .header("Authorization", "Bearer " + token);
        for (Long id : itemIds) {
            solicitud.param("itemIds", String.valueOf(id));
        }
        return mockMvc.perform(solicitud);
    }

    private MvcResult facturaDeAlumno1(String tokenPadre) throws Exception {
        return mockMvc.perform(get("/api/v1/mi-informacion/alumnos/1/facturas")
                        .header("Authorization", "Bearer " + tokenPadre))
                .andExpect(status().isOk()).andReturn();
    }

    private static long itemId(MvcResult facturas, String concepto) throws Exception {
        List<Number> ids = JsonPath.read(facturas.getResponse().getContentAsString(),
                "$[0].items[?(@.concepto=='" + concepto + "')].id");
        return ids.get(0).longValue();
    }

    private static LocalDate hoy() {
        return LocalDate.now(RelojConfig.ZONA);
    }

    /** Deja al alumno 1 con un deporte, un recorrido y comedor, para que su factura tenga cuatro ítems. */
    private void inscribirEnServicios(String admin) throws Exception {
        MvcResult deportes = mockMvc.perform(get("/api/v1/deportes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andReturn();
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
