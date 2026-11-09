package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.FacturacionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.ResultadoFacturacionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/facturacion")
@RequiredArgsConstructor
@Tag(name = "Facturación", description = "Emisión de las facturas mensuales. Requiere FINANZAS_ADMINISTRAR")
@SecurityRequirement(name = "bearerAuth")
public class FacturacionController {

    private final FacturacionService facturacionService;

    @PostMapping("/generar")
    @Operation(summary = "Generar las facturas de un mes",
            description = "Una factura por alumno activo, con su cuota base y los servicios en los que está inscripto. "
                    + "Si el alumno ya tiene factura en ese mes se omite, así que se puede repetir sin duplicar.")
    public ResultadoFacturacionDTO generar(@RequestParam String periodo) {
        return facturacionService.generar(periodo);
    }
}
