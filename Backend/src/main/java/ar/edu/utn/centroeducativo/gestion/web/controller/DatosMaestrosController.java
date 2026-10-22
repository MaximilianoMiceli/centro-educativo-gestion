package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.DatosMaestrosService;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearCursoRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearMateriaRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearNivelRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CursoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.MateriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.NivelDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Datos maestros", description = "Niveles, cursos y materias. Las altas requieren DATOS_MAESTROS_ADMINISTRAR")
@SecurityRequirement(name = "bearerAuth")
public class DatosMaestrosController {

    private final DatosMaestrosService datosMaestrosService;

    @GetMapping("/api/v1/niveles")
    @Operation(summary = "Listar niveles educativos")
    public ResponseEntity<List<NivelDTO>> listarNiveles() {
        return ResponseEntity.ok(datosMaestrosService.listarNiveles());
    }

    @PostMapping("/api/v1/niveles")
    @Operation(summary = "Crear nivel educativo")
    public ResponseEntity<NivelDTO> crearNivel(@RequestBody CrearNivelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datosMaestrosService.crearNivel(request));
    }

    @GetMapping("/api/v1/cursos")
    @Operation(summary = "Listar cursos", description = "Filtrable por nivel con el parámetro nivelId.")
    public ResponseEntity<List<CursoDTO>> listarCursos(@RequestParam(required = false) Short nivelId) {
        return ResponseEntity.ok(datosMaestrosService.listarCursos(nivelId));
    }

    @PostMapping("/api/v1/cursos")
    @Operation(summary = "Crear curso dentro de un nivel")
    public ResponseEntity<CursoDTO> crearCurso(@RequestBody CrearCursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datosMaestrosService.crearCurso(request));
    }

    @GetMapping("/api/v1/materias")
    @Operation(summary = "Listar materias")
    public ResponseEntity<List<MateriaDTO>> listarMaterias() {
        return ResponseEntity.ok(datosMaestrosService.listarMaterias());
    }

    @PostMapping("/api/v1/materias")
    @Operation(summary = "Crear materia")
    public ResponseEntity<MateriaDTO> crearMateria(@RequestBody CrearMateriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datosMaestrosService.crearMateria(request));
    }
}
