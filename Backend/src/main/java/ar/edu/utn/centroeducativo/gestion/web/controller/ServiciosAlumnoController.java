package ar.edu.utn.centroeducativo.gestion.web.controller;

import ar.edu.utn.centroeducativo.gestion.service.AlumnoServiciosService;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.RecorridoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.AtencionEnfermeriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.IdiomaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.InscribirIdioma;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.RegistrarAtencion;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.RenombrarRecorrido;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.SolicitudApoyo;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Servicios del alumno", description = "Deportes, transporte, comedor, idiomas, apoyo estudiantil y enfermería")
@SecurityRequirement(name = "bearerAuth")
public class ServiciosAlumnoController {

    private final AlumnoServiciosService alumnoServiciosService;

    @GetMapping("/api/v1/alumnos/{id}/servicios")
    @Operation(summary = "Servicios del alumno: deportes, transporte, comedor, idiomas y apoyo estudiantil")
    public ResponseEntity<AlumnoServiciosDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoServiciosService.obtener(id));
    }

    @PostMapping("/api/v1/alumnos/{id}/deportes")
    @Operation(summary = "Inscribir al alumno en un grupo de deporte",
            description = "Máximo dos deportes, sin repetir el deporte y sin superponer horarios.")
    public ResponseEntity<AlumnoServiciosDTO> inscribirDeporte(@PathVariable Long id,
                                                               @RequestBody InscribirDeporte request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alumnoServiciosService.inscribirDeporte(id, request.grupoId()));
    }

    @DeleteMapping("/api/v1/alumnos/{id}/deportes/{inscripcionId}")
    @Operation(summary = "Dar de baja una inscripción a deporte")
    public ResponseEntity<AlumnoServiciosDTO> quitarDeporte(@PathVariable Long id, @PathVariable Long inscripcionId) {
        return ResponseEntity.ok(alumnoServiciosService.quitarDeporte(id, inscripcionId));
    }

    @PutMapping("/api/v1/alumnos/{id}/transporte")
    @Operation(summary = "Asignar recorrido de transporte")
    public ResponseEntity<AlumnoServiciosDTO> asignarTransporte(@PathVariable Long id,
                                                                @RequestBody AsignarTransporte request) {
        return ResponseEntity.ok(alumnoServiciosService.asignarTransporte(id, request.recorridoId()));
    }

    @DeleteMapping("/api/v1/alumnos/{id}/transporte")
    @Operation(summary = "Dar de baja el transporte")
    public ResponseEntity<AlumnoServiciosDTO> quitarTransporte(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoServiciosService.quitarTransporte(id));
    }

    @PutMapping("/api/v1/alumnos/{id}/comedor")
    @Operation(summary = "Inscribir o dar de baja del comedor")
    public ResponseEntity<AlumnoServiciosDTO> asignarComedor(@PathVariable Long id, @RequestBody AsignarComedor request) {
        return ResponseEntity.ok(alumnoServiciosService.asignarComedor(id, request.inscripto()));
    }

    @PostMapping("/api/v1/alumnos/{id}/idiomas")
    @Operation(summary = "Inscribir al alumno en un idioma (Inglés, Portugués o Francés)")
    public ResponseEntity<AlumnoServiciosDTO> inscribirIdioma(@PathVariable Long id, @RequestBody InscribirIdioma request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alumnoServiciosService.inscribirIdioma(id, request.idiomaId()));
    }

    @DeleteMapping("/api/v1/alumnos/{id}/idiomas/{inscripcionId}")
    @Operation(summary = "Dar de baja un idioma")
    public ResponseEntity<AlumnoServiciosDTO> quitarIdioma(@PathVariable Long id, @PathVariable Long inscripcionId) {
        return ResponseEntity.ok(alumnoServiciosService.quitarIdioma(id, inscripcionId));
    }

    @PutMapping("/api/v1/alumnos/{id}/apoyo")
    @Operation(summary = "Inscribir al alumno en el Servicio de Apoyo Estudiantil, o actualizar sus observaciones")
    public ResponseEntity<AlumnoServiciosDTO> asignarApoyo(@PathVariable Long id, @RequestBody SolicitudApoyo request) {
        return ResponseEntity.ok(alumnoServiciosService.asignarApoyo(id, request.observaciones()));
    }

    @DeleteMapping("/api/v1/alumnos/{id}/apoyo")
    @Operation(summary = "Dar de baja del Servicio de Apoyo Estudiantil")
    public ResponseEntity<AlumnoServiciosDTO> quitarApoyo(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoServiciosService.quitarApoyo(id));
    }

    @GetMapping("/api/v1/alumnos/{id}/enfermeria")
    @Operation(summary = "Atenciones de enfermería del alumno",
            description = "Datos de salud: requiere ALUMNOS_ADMINISTRAR.")
    public ResponseEntity<List<AtencionEnfermeriaDTO>> atencionesEnfermeria(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoServiciosService.atencionesEnfermeria(id));
    }

    @PostMapping("/api/v1/alumnos/{id}/enfermeria")
    @Operation(summary = "Registrar una atención o un acceso a enfermería")
    public ResponseEntity<AtencionEnfermeriaDTO> registrarAtencion(@PathVariable Long id,
                                                                   @RequestBody RegistrarAtencion request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alumnoServiciosService.registrarAtencion(id, request.motivo(), request.observaciones()));
    }

    @GetMapping("/api/v1/idiomas")
    @Operation(summary = "Listar los idiomas que se ofrecen")
    public ResponseEntity<List<IdiomaDTO>> idiomas() {
        return ResponseEntity.ok(alumnoServiciosService.idiomas());
    }

    @GetMapping("/api/v1/recorridos")
    @Operation(summary = "Listar recorridos de transporte disponibles")
    public ResponseEntity<List<RecorridoDTO>> recorridos() {
        return ResponseEntity.ok(alumnoServiciosService.recorridos());
    }

    @PutMapping("/api/v1/recorridos/{id}")
    @Operation(summary = "Renombrar un recorrido de transporte",
            description = "Hay exactamente cuatro recorridos: no se crean ni se borran. Requiere DATOS_MAESTROS_ADMINISTRAR.")
    public ResponseEntity<RecorridoDTO> renombrarRecorrido(@PathVariable Short id,
                                                           @RequestBody RenombrarRecorrido request) {
        return ResponseEntity.ok(alumnoServiciosService.renombrarRecorrido(id, request.nombre()));
    }
}
