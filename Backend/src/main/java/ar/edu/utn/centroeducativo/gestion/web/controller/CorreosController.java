package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.correo.CorreosService;
import ar.edu.utn.centroeducativo.gestion.domain.TipoCorreo;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.web.dto.CorreoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ResultadoCorreosDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/correos")
@RequiredArgsConstructor
@Tag(name = "Correos a los padres",
        description = "Recordatorio de cuotas el último día hábil y aviso de deuda el día 20. Requiere FINANZAS_ADMINISTRAR")
@SecurityRequirement(name = "bearerAuth")
public class CorreosController {

    private final CorreosService correosService;
    private final Clock reloj;

    @PostMapping("/ejecutar")
    @Operation(summary = "Preparar los correos de un tipo",
            description = "Es lo mismo que hace la tarea programada, pero a pedido. Con fecha se simula otro día, por "
                    + "ejemplo 2026-11-30 para el recordatorio de fin de mes. No repite los correos ya enviados ese mes.")
    public ResultadoCorreosDTO ejecutar(@RequestParam String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return correosService.ejecutar(parsearTipo(tipo), fecha != null ? fecha : LocalDate.now(reloj));
    }

    @GetMapping
    @Operation(summary = "Correos preparados, del más reciente al más antiguo")
    public List<CorreoDTO> listar() {
        return correosService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Un correo completo, con su texto y la lista de adjuntos")
    public CorreoDTO detalle(@PathVariable Long id) {
        return correosService.detalle(id);
    }

    @GetMapping("/{id}/adjuntos/{adjuntoId}")
    @Operation(summary = "Descargar un adjunto del correo (la factura en PDF)")
    public ResponseEntity<byte[]> adjunto(@PathVariable Long id, @PathVariable Long adjuntoId) {
        CorreosService.Descarga descarga = correosService.adjunto(id, adjuntoId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + descarga.nombre() + "\"")
                .body(descarga.contenido());
    }

    private static TipoCorreo parsearTipo(String tipo) {
        try {
            return TipoCorreo.valueOf(tipo.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("El tipo de correo no es válido",
                    Map.of("tipo", "Usá RECORDATORIO o DEUDA"));
        }
    }
}
