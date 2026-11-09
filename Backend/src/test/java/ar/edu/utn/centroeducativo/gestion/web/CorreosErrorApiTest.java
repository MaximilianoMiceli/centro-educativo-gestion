package ar.edu.utn.centroeducativo.gestion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.centroeducativo.gestion.correo.CorreoSaliente;
import ar.edu.utn.centroeducativo.gestion.correo.CorreoSender;
import ar.edu.utn.centroeducativo.gestion.correo.ResultadoEnvio;
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

/** Qué pasa cuando el servicio de correo falla: queda registrado como error y se reintenta en la próxima ejecución. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CorreosErrorApiTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CorreoSender sender;

    @Test
    void unEnvioFallidoQuedaComoErrorYSeReintentaSinDuplicarElRegistro() throws Exception {
        String admin = login();
        when(sender.enviar(any(CorreoSaliente.class))).thenThrow(new IllegalStateException("Servidor de correo caído"));

        ejecutar(admin).andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(0))
                .andExpect(jsonPath("$.conError").value(1));
        mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("ERROR"))
                .andExpect(jsonPath("$[0].detalleError").value("Servidor de correo caído"));

        when(sender.enviar(any(CorreoSaliente.class))).thenReturn(ResultadoEnvio.simulado());
        ejecutar(admin).andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(1))
                .andExpect(jsonPath("$.conError").value(0))
                .andExpect(jsonPath("$.yaEnviados").value(0));
        mockMvc.perform(get("/api/v1/correos").header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("SIMULADO"));
    }

    private org.springframework.test.web.servlet.ResultActions ejecutar(String token) throws Exception {
        return mockMvc.perform(post("/api/v1/correos/ejecutar").param("tipo", "RECORDATORIO")
                .param("fecha", "2026-11-30").header("Authorization", "Bearer " + token));
    }

    private String login() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = TOKEN.matcher(resultado.getResponse().getContentAsString());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
