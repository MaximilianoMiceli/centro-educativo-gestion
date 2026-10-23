package ar.edu.utn.centroeducativo.gestion.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

final class SesionPruebas {

    static final long ID_PROFESOR1 = 2L;

    private SesionPruebas() {
    }

    static void restablecerPassword(MockMvc mockMvc, String tokenAdmin, long usuarioId, String password)
            throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/" + usuarioId + "/password")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + password + "\",\"debeCambiarPassword\":false}"))
                .andExpect(status().isOk());
    }
}
