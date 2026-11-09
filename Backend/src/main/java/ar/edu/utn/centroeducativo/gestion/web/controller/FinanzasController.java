package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.FinanzasService;
import ar.edu.utn.centroeducativo.gestion.web.dto.ListadosFinanzasDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finanzas")
@RequiredArgsConstructor
@Tag(name = "Finanzas", description = "Listados de ingresos y pagos. Requiere FINANZAS_CONSULTAR")
@SecurityRequirement(name = "bearerAuth")
public class FinanzasController {

    private final FinanzasService finanzasService;

    @GetMapping("/ingresos")
    @Operation(summary = "Ingresos por período",
            description = "Pagos recibidos entre desde y hasta (por fecha de la transferencia), con subtotal por mes.")
    public ListadosFinanzasDTO.Ingresos ingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return finanzasService.ingresos(desde, hasta);
    }

    @GetMapping("/pagos-completos")
    @Operation(summary = "Facturas pagadas por completo, por año y alumno",
            description = "Incluye las facturas cuyo último pago cae entre desde y hasta. El año es el del último pago.")
    public ListadosFinanzasDTO.PagosPorFactura pagosCompletos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return finanzasService.pagosCompletos(desde, hasta);
    }

    @GetMapping("/pagos-incompletos")
    @Operation(summary = "Facturas con pagos parciales, por año y alumno",
            description = "Incluye las facturas con saldo pendiente cuyo último pago cae entre desde y hasta.")
    public ListadosFinanzasDTO.PagosPorFactura pagosIncompletos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return finanzasService.pagosIncompletos(desde, hasta);
    }

    @GetMapping("/pagos-por-deporte")
    @Operation(summary = "Pagos por alumno, deporte, nivel, horario y profesor a cargo",
            description = "Ítems de deporte de las facturas emitidas entre desde y hasta, pagados y pendientes.")
    public ListadosFinanzasDTO.PagosPorDeporte pagosPorDeporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return finanzasService.pagosPorDeporte(desde, hasta);
    }

    @GetMapping("/pagos-por-recorrido")
    @Operation(summary = "Pagos por alumno y recorrido del servicio de transporte",
            description = "Ítems de transporte de las facturas emitidas entre desde y hasta, pagados y pendientes.")
    public ListadosFinanzasDTO.PagosPorRecorrido pagosPorRecorrido(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return finanzasService.pagosPorRecorrido(desde, hasta);
    }
}
