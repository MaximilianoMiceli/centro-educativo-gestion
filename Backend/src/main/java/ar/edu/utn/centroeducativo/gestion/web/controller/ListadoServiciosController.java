package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.ListadoServiciosService;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosServiciosDTO.AtencionListadoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosServiciosDTO.InscriptoApoyoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosServiciosDTO.InscriptoIdiomaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
@Tag(name = "Listados de servicios", description = "Inscriptos a idiomas, apoyo estudiantil y atenciones de enfermería")
@SecurityRequirement(name = "bearerAuth")
public class ListadoServiciosController {

    private final ListadoServiciosService listadoServiciosService;

    @GetMapping("/idiomas")
    @Operation(summary = "Alumnos inscriptos en idiomas", description = "Filtros opcionales: nivelId, cursoId, idiomaId.")
    public List<InscriptoIdiomaDTO> idiomas(@RequestParam(required = false) Short nivelId,
                                            @RequestParam(required = false) Long cursoId,
                                            @RequestParam(required = false) Short idiomaId) {
        return listadoServiciosService.idiomas(nivelId, cursoId, idiomaId);
    }

    @GetMapping("/apoyo")
    @Operation(summary = "Alumnos en el Servicio de Apoyo Estudiantil", description = "Filtros opcionales: nivelId, cursoId.")
    public List<InscriptoApoyoDTO> apoyo(@RequestParam(required = false) Short nivelId,
                                         @RequestParam(required = false) Long cursoId) {
        return listadoServiciosService.apoyo(nivelId, cursoId);
    }

    @GetMapping("/enfermeria")
    @Operation(summary = "Atenciones de enfermería", description = "Datos de salud: requiere ALUMNOS_ADMINISTRAR.")
    public List<AtencionListadoDTO> enfermeria(@RequestParam(required = false) Short nivelId,
                                               @RequestParam(required = false) Long cursoId) {
        return listadoServiciosService.enfermeria(nivelId, cursoId);
    }
}
