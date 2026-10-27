package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.MiInformacionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoResumenDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.CalificacionesDTO.MateriaCalificacionesDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.MiInformacionDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.InscribirIdioma;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.InscribirDeporte;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesServicios.AsignarComedor;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesServicios.AsignarTransporte;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mi-informacion")
@RequiredArgsConstructor
@Tag(name = "Mi información", description = "Información de la cuenta. El alumno puede gestionar sus propios servicios; el padre solo consulta")
@SecurityRequirement(name = "bearerAuth")
public class MiInformacionController {

    private final MiInformacionService miInformacionService;

    @GetMapping("/alumnos")
    @Operation(summary = "Alumnos visibles para el usuario (él mismo o sus hijos)")
    public ResponseEntity<List<AlumnoResumenDTO>> alumnos() {
        return ResponseEntity.ok(miInformacionService.alumnos());
    }

    @GetMapping("/alumnos/{id}")
    @Operation(summary = "Ficha, materias y servicios de un alumno visible para el usuario")
    public ResponseEntity<MiInformacionDTO> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(miInformacionService.detalle(id));
    }

    @GetMapping("/alumnos/{id}/calificaciones")
    @Operation(summary = "Calificaciones de un alumno visible para el usuario, por materia")
    public ResponseEntity<List<MateriaCalificacionesDTO>> calificaciones(@PathVariable Long id) {
        return ResponseEntity.ok(miInformacionService.calificaciones(id));
    }

    @GetMapping("/servicios")
    @Operation(summary = "Mis servicios: deportes, transporte, comedor e idiomas")
    public ResponseEntity<AlumnoServiciosDTO> misServicios() {
        return ResponseEntity.ok(miInformacionService.serviciosPropios());
    }

    @PostMapping("/servicios/deportes")
    @Operation(summary = "Inscribirme en un grupo de deporte", description = "Máximo dos deportes, sin superponer horarios.")
    public ResponseEntity<AlumnoServiciosDTO> inscribirmeDeporte(@RequestBody InscribirDeporte request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(miInformacionService.inscribirDeporte(request.grupoId()));
    }

    @DeleteMapping("/servicios/deportes/{inscripcionId}")
    @Operation(summary = "Darme de baja de un deporte")
    public ResponseEntity<AlumnoServiciosDTO> quitarmeDeporte(@PathVariable Long inscripcionId) {
        return ResponseEntity.ok(miInformacionService.quitarDeporte(inscripcionId));
    }

    @PutMapping("/servicios/transporte")
    @Operation(summary = "Elegir recorrido de transporte")
    public ResponseEntity<AlumnoServiciosDTO> asignarmeTransporte(@RequestBody AsignarTransporte request) {
        return ResponseEntity.ok(miInformacionService.asignarTransporte(request.recorridoId()));
    }

    @DeleteMapping("/servicios/transporte")
    @Operation(summary = "Darme de baja del transporte")
    public ResponseEntity<AlumnoServiciosDTO> quitarmeTransporte() {
        return ResponseEntity.ok(miInformacionService.quitarTransporte());
    }

    @PutMapping("/servicios/comedor")
    @Operation(summary = "Inscribirme o darme de baja del comedor")
    public ResponseEntity<AlumnoServiciosDTO> asignarmeComedor(@RequestBody AsignarComedor request) {
        return ResponseEntity.ok(miInformacionService.asignarComedor(request.inscripto()));
    }

    @PostMapping("/servicios/idiomas")
    @Operation(summary = "Inscribirme en un idioma")
    public ResponseEntity<AlumnoServiciosDTO> inscribirmeIdioma(@RequestBody InscribirIdioma request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(miInformacionService.inscribirIdioma(request.idiomaId()));
    }

    @DeleteMapping("/servicios/idiomas/{inscripcionId}")
    @Operation(summary = "Darme de baja de un idioma")
    public ResponseEntity<AlumnoServiciosDTO> quitarmeIdioma(@PathVariable Long inscripcionId) {
        return ResponseEntity.ok(miInformacionService.quitarIdioma(inscripcionId));
    }
}
