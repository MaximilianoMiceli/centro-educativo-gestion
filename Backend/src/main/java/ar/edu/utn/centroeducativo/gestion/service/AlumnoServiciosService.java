package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.GrupoDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionComedor;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionTransporte;
import ar.edu.utn.centroeducativo.gestion.domain.RecorridoTransporte;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.GrupoDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionComedorRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RecorridoTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.InscripcionDeporteDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.RecorridoDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlumnoServiciosService {

    static final int MAX_DEPORTES = 2;

    private final AlumnoRepository alumnoRepository;
    private final GrupoDeporteRepository grupoRepository;
    private final InscripcionDeporteRepository inscripcionDeporteRepository;
    private final RecorridoTransporteRepository recorridoRepository;
    private final InscripcionTransporteRepository inscripcionTransporteRepository;
    private final InscripcionComedorRepository inscripcionComedorRepository;
    private final AccesoAlumnos accesoAlumnos;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public AlumnoServiciosDTO obtener(Long alumnoId) {
        Alumno alumno = buscarAlumno(alumnoId);
        accesoAlumnos.verificar(alumno);
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO inscribirDeporte(Long alumnoId, Long grupoId) {
        Alumno alumno = buscarAlumno(alumnoId);
        Map<String, String> errores = new LinkedHashMap<>();
        GrupoDeporte grupo = grupoId == null ? null : grupoRepository.findById(grupoId).orElse(null);
        if (grupo == null) {
            errores.put("grupoId", "El grupo seleccionado no existe");
        } else {
            validarInscripcion(alumno, grupo, errores);
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo inscribir al alumno", errores);
        }

        InscripcionDeporte inscripcion = new InscripcionDeporte();
        inscripcion.setAlumno(alumno);
        inscripcion.setGrupo(grupo);
        inscripcionDeporteRepository.save(inscripcion);

        auditoriaService.registrar("INSCRIPCION", "DEPORTE", alumnoId,
                "Inscripción a " + grupo.getDeporte().getNombre() + " ("
                        + FormatoHorario.rango(grupo.getDiaSemana(), grupo.getHoraInicio(), grupo.getHoraFin()) + ")");
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO quitarDeporte(Long alumnoId, Long inscripcionId) {
        Alumno alumno = buscarAlumno(alumnoId);
        InscripcionDeporte inscripcion = inscripcionDeporteRepository.findById(inscripcionId)
                .filter(i -> i.getAlumno().getId().equals(alumnoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("La inscripción no existe para este alumno"));
        String deporte = inscripcion.getGrupo().getDeporte().getNombre();
        inscripcionDeporteRepository.delete(inscripcion);

        auditoriaService.registrar("BAJA", "DEPORTE", alumnoId, "Baja de " + deporte);
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO asignarTransporte(Long alumnoId, Short recorridoId) {
        Alumno alumno = buscarAlumno(alumnoId);
        RecorridoTransporte recorrido = recorridoId == null ? null
                : recorridoRepository.findById(recorridoId).orElse(null);
        if (recorrido == null) {
            throw new ValidacionException("No se pudo asignar el transporte",
                    Map.of("recorridoId", "El recorrido seleccionado no existe"));
        }

        InscripcionTransporte inscripcion = inscripcionTransporteRepository.findById(alumnoId)
                .orElseGet(InscripcionTransporte::new);
        inscripcion.setAlumnoId(alumnoId);
        inscripcion.setRecorrido(recorrido);
        inscripcionTransporteRepository.save(inscripcion);

        auditoriaService.registrar("ASIGNACION", "TRANSPORTE", alumnoId, "Transporte: " + recorrido.getNombre());
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO quitarTransporte(Long alumnoId) {
        Alumno alumno = buscarAlumno(alumnoId);
        inscripcionTransporteRepository.deleteById(alumnoId);
        auditoriaService.registrar("BAJA", "TRANSPORTE", alumnoId, "Baja del servicio de transporte");
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO asignarComedor(Long alumnoId, boolean inscripto) {
        Alumno alumno = buscarAlumno(alumnoId);
        if (inscripto) {
            if (!inscripcionComedorRepository.existsById(alumnoId)) {
                inscripcionComedorRepository.save(new InscripcionComedor(alumnoId));
            }
            auditoriaService.registrar("ALTA", "COMEDOR", alumnoId, "Inscripción al comedor");
        } else {
            inscripcionComedorRepository.deleteById(alumnoId);
            auditoriaService.registrar("BAJA", "COMEDOR", alumnoId, "Baja del comedor");
        }
        return toDTO(alumno);
    }

    @Transactional(readOnly = true)
    public List<RecorridoDTO> recorridos() {
        return recorridoRepository.findAllByOrderByIdAsc().stream()
                .map(r -> new RecorridoDTO(r.getId(), r.getNombre()))
                .toList();
    }

    private void validarInscripcion(Alumno alumno, GrupoDeporte grupo, Map<String, String> errores) {
        if (!grupo.getNivel().getId().equals(alumno.getCurso().getNivel().getId())) {
            errores.put("grupoId", "El grupo es para el nivel " + grupo.getNivel().getNombreLegible());
            return;
        }
        Long deporteId = grupo.getDeporte().getId();
        if (inscripcionDeporteRepository.existsByAlumno_IdAndGrupo_Deporte_Id(alumno.getId(), deporteId)) {
            errores.put("grupoId", "El alumno ya está inscripto en " + grupo.getDeporte().getNombre());
            return;
        }
        List<InscripcionDeporte> actuales = inscripcionDeporteRepository.findByAlumno_Id(alumno.getId());
        if (actuales.size() >= MAX_DEPORTES) {
            errores.put("grupoId", "El alumno ya tiene " + MAX_DEPORTES + " deportes; quitá uno para inscribirse en otro");
            return;
        }
        actuales.stream()
                .map(InscripcionDeporte::getGrupo)
                .filter(otro -> otro.getDiaSemana().equals(grupo.getDiaSemana())
                        && otro.getHoraInicio().isBefore(grupo.getHoraFin())
                        && otro.getHoraFin().isAfter(grupo.getHoraInicio()))
                .findFirst()
                .ifPresent(otro -> errores.put("grupoId", "Conflicto de horario con " + otro.getDeporte().getNombre()
                        + " (" + FormatoHorario.rango(otro.getDiaSemana(), otro.getHoraInicio(), otro.getHoraFin()) + ")"));
    }

    private Alumno buscarAlumno(Long alumnoId) {
        return alumnoRepository.findWithCursoById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno con ID " + alumnoId + " no encontrado"));
    }

    private AlumnoServiciosDTO toDTO(Alumno alumno) {
        List<InscripcionDeporteDTO> deportes = inscripcionDeporteRepository.findByAlumno_Id(alumno.getId()).stream()
                .map(i -> new InscripcionDeporteDTO(i.getId(), i.getGrupo().getDeporte().getId(),
                        i.getGrupo().getDeporte().getNombre(),
                        FormatoHorario.rango(i.getGrupo().getDiaSemana(), i.getGrupo().getHoraInicio(),
                                i.getGrupo().getHoraFin())))
                .toList();
        RecorridoDTO transporte = inscripcionTransporteRepository.findById(alumno.getId())
                .map(t -> new RecorridoDTO(t.getRecorrido().getId(), t.getRecorrido().getNombre()))
                .orElse(null);
        boolean comedor = inscripcionComedorRepository.existsById(alumno.getId());
        return new AlumnoServiciosDTO(alumno.getId(),
                alumno.getApellido() + ", " + alumno.getNombre(),
                alumno.getCurso().getNivel().getNombreLegible(),
                deportes, transporte, comedor);
    }
}
