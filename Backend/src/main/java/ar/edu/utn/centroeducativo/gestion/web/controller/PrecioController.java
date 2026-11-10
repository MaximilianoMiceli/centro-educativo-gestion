package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.PrecioService;
import ar.edu.utn.centroeducativo.gestion.web.dto.ActualizarPreciosRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.PreciosDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/precios")
@RequiredArgsConstructor
@Tag(name = "Precios", description = "Valores de la cuota, deportes, transporte y comedor. Consultar requiere FINANZAS_CONSULTAR y modificar FINANZAS_ADMINISTRAR")
@SecurityRequirement(name = "bearerAuth")
public class PrecioController {

    private final PrecioService precioService;

    @GetMapping
    @Operation(summary = "Ver los valores vigentes",
            description = "Incluye todos los deportes y recorridos; el monto es null si todavía no tiene valor.")
    public PreciosDTO obtener() {
        return precioService.obtener();
    }

    @PutMapping
    @Operation(summary = "Modificar valores",
            description = "Los campos que no se envían no cambian. Cada importe es mayor o igual a cero y con hasta dos decimales.")
    public PreciosDTO actualizar(@RequestBody ActualizarPreciosRequest request) {
        return precioService.actualizar(request);
    }
}
