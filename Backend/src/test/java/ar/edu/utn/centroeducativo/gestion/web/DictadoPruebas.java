package ar.edu.utn.centroeducativo.gestion.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Crea una materia con horario para un docente dentro de la propia prueba, así el test no depende
 * de los datos de demostración que cargan las migraciones ni de la base local.
 */
final class DictadoPruebas {

    static final String MATERIA = "Materia de prueba docente";

    private DictadoPruebas() {
    }

    static void asignarMateriaConHorario(MockMvc mockMvc, String tokenAdmin, long docenteId) throws Exception {
        long materiaId = crearMateria(mockMvc, tokenAdmin);
        long cursoId = cursoSecundario(mockMvc, tokenAdmin);

        MvcResult dictado = mockMvc.perform(post("/api/v1/dictados")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"docenteId\":" + docenteId + ",\"materiaId\":" + materiaId
                                + ",\"cursoId\":" + cursoId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        long dictadoId = ((Number) JsonPath.read(dictado.getResponse().getContentAsString(), "$.id")).longValue();

        // Domingo 7:00 a 8:00: ninguna migración ocupa ese horario
        mockMvc.perform(post("/api/v1/horarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictadoId\":" + dictadoId + ",\"diaSemana\":7,"
                                + "\"horaInicio\":\"07:00\",\"horaFin\":\"08:00\"}"))
                .andExpect(status().isCreated());
    }

    private static long crearMateria(MockMvc mockMvc, String tokenAdmin) throws Exception {
        MvcResult materia = mockMvc.perform(post("/api/v1/materias")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + MATERIA + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(materia.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static long cursoSecundario(MockMvc mockMvc, String tokenAdmin) throws Exception {
        MvcResult cursos = mockMvc.perform(get("/api/v1/cursos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn();
        List<?> ids = JsonPath.read(cursos.getResponse().getContentAsString(), "$[?(@.nivel =~ /secundario/i)].id");
        return ((Number) ids.get(0)).longValue();
    }
}
