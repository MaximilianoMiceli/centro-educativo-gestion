package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.reporte.FiltrosReporte;
import ar.edu.utn.centroeducativo.gestion.reporte.Reporte;
import ar.edu.utn.centroeducativo.gestion.reporte.ReporteExportador;
import ar.edu.utn.centroeducativo.gestion.reporte.ReporteService;
import ar.edu.utn.centroeducativo.gestion.reporte.TipoReporte;
import ar.edu.utn.centroeducativo.gestion.web.dto.CatalogoReporteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes del sistema en JSON, PDF o Excel. Requiere REPORTES_GENERAR")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private static final MediaType EXCEL = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteService reporteService;
    private final ReporteExportador exportador;

    @GetMapping
    @Operation(summary = "Listar los reportes disponibles")
    public List<CatalogoReporteDTO> catalogo() {
        return Arrays.stream(TipoReporte.values())
                .map(tipo -> new CatalogoReporteDTO(tipo.codigo(), tipo.titulo(), tipo.slug()))
                .toList();
    }

    @GetMapping("/{tipo}")
    @Operation(summary = "Generar un reporte en JSON",
            description = "Los filtros son opcionales salvo los que indica cada reporte (alumnoId en REP-001, nivelId en REP-007).")
    public Reporte generar(@PathVariable String tipo, @ModelAttribute FiltrosReporte filtros) {
        return reporteService.generar(TipoReporte.desde(tipo), filtros);
    }

    @GetMapping("/{tipo}/pdf")
    @Operation(summary = "Descargar un reporte en PDF")
    public ResponseEntity<byte[]> pdf(@PathVariable String tipo, @ModelAttribute FiltrosReporte filtros) {
        TipoReporte reporte = TipoReporte.desde(tipo);
        Reporte datos = reporteService.generar(reporte, filtros);
        return descarga(exportador.pdf(datos), MediaType.APPLICATION_PDF, reporte, "pdf");
    }

    @GetMapping("/{tipo}/excel")
    @Operation(summary = "Descargar un reporte en Excel (.xlsx)")
    public ResponseEntity<byte[]> excel(@PathVariable String tipo, @ModelAttribute FiltrosReporte filtros) {
        TipoReporte reporte = TipoReporte.desde(tipo);
        Reporte datos = reporteService.generar(reporte, filtros);
        return descarga(exportador.excel(datos), EXCEL, reporte, "xlsx");
    }

    private static ResponseEntity<byte[]> descarga(byte[] contenido, MediaType tipoMedia, TipoReporte reporte,
            String extension) {
        String nombre = reporte.codigo() + "-" + reporte.slug() + "." + extension;
        return ResponseEntity.ok()
                .contentType(tipoMedia)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }
}
