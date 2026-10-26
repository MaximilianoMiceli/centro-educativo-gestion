package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.service.DocenteAgendaService;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteAgendaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mi agenda", description = "Materias, cursos y horarios del docente que consulta")
@SecurityRequirement(name = "bearerAuth")
public class MiAgendaController {

    private final DocenteAgendaService docenteAgendaService;
    private final UsuarioActual usuarioActual;

    @GetMapping("/api/v1/mi-agenda")
    @Operation(summary = "Mi agenda", description = "Requiere DOCENTES_CONSULTAR y un usuario vinculado a un docente.")
    public DocenteAgendaDTO miAgenda() {
        Long usuarioId = usuarioActual.id()
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida"));
        return docenteAgendaService.miAgenda(usuarioId);
    }
}
