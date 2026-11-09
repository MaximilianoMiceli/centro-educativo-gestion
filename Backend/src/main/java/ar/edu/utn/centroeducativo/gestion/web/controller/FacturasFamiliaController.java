package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.FacturasFamiliaService;
import ar.edu.utn.centroeducativo.gestion.service.PagoService;
import ar.edu.utn.centroeducativo.gestion.web.dto.DeudaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.FacturaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/mi-informacion")
@RequiredArgsConstructor
@Tag(name = "Facturas y pagos de la familia",
        description = "El alumno o sus padres consultan facturas y deuda. Solo los padres registran pagos")
@SecurityRequirement(name = "bearerAuth")
public class FacturasFamiliaController {

    private final FacturasFamiliaService facturasService;
    private final PagoService pagoService;

    @GetMapping("/alumnos/{alumnoId}/facturas")
    @Operation(summary = "Facturas del alumno con sus comprobantes",
            description = "Filtra por fecha de emisión, con desde y hasta opcionales (AAAA-MM-DD).")
    public List<FacturaDTO> facturas(@PathVariable Long alumnoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return facturasService.facturas(alumnoId, desde, hasta);
    }

    @GetMapping("/alumnos/{alumnoId}/deuda")
    @Operation(summary = "Historial de deuda del alumno, por ítem de pago")
    public DeudaDTO deuda(@PathVariable Long alumnoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return facturasService.deuda(alumnoId, desde, hasta);
    }

    @GetMapping("/facturas/{facturaId}")
    @Operation(summary = "Detalle de una factura")
    public FacturaDTO factura(@PathVariable Long facturaId) {
        return facturasService.factura(facturaId);
    }

    @GetMapping("/facturas/{facturaId}/pdf")
    @Operation(summary = "Factura en PDF")
    public ResponseEntity<byte[]> facturaPdf(@PathVariable Long facturaId) {
        return pdf(facturasService.facturaPdf(facturaId), "factura-" + facturaId + ".pdf");
    }

    @PostMapping(path = "/facturas/{facturaId}/pagos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar un pago de ítems de una factura",
            description = "Solo para padres. Se eligen ítems pendientes y se adjunta la transferencia (PDF, PNG o JPG, "
                    + "hasta 5 MB). Una factura puede tener varios pagos. Responde con el comprobante emitido.")
    public ResponseEntity<FacturaDTO.PagoResumen> pagar(@PathVariable Long facturaId,
            @RequestParam("itemIds") List<Long> itemIds,
            @RequestParam("fechaPago") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPago,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoService.registrar(facturaId, itemIds, fechaPago, archivo));
    }

    @GetMapping("/pagos/{pagoId}/comprobante")
    @Operation(summary = "Comprobante de pago emitido por el sistema, en PDF")
    public ResponseEntity<byte[]> comprobante(@PathVariable Long pagoId) {
        return pdf(pagoService.comprobantePdf(pagoId), "comprobante-" + pagoId + ".pdf");
    }

    @GetMapping("/pagos/{pagoId}/archivo")
    @Operation(summary = "Archivo de la transferencia que adjuntó el padre")
    public ResponseEntity<byte[]> archivo(@PathVariable Long pagoId) {
        PagoService.Archivo archivo = pagoService.archivo(pagoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.nombre() + "\"")
                .body(archivo.contenido());
    }

    private static ResponseEntity<byte[]> pdf(byte[] contenido, String nombre) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                .body(contenido);
    }
}
