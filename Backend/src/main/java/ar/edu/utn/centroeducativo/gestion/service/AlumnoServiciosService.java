package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.AtencionEnfermeria;
import ar.edu.utn.centroeducativo.gestion.domain.GrupoDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.Idioma;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionApoyo;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionComedor;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionIdioma;
import ar.edu.utn.centroeducativo.gestion.domain.InscripcionTransporte;
import ar.edu.utn.centroeducativo.gestion.domain.RecorridoTransporte;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.AtencionEnfermeriaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.GrupoDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.IdiomaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionApoyoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionComedorRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionIdiomaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.InscripcionTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RecorridoTransporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.ApoyoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.IdiomaInscriptoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.InscripcionDeporteDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoServiciosDTO.RecorridoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.AtencionEnfermeriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ServiciosComplementariosDTO.IdiomaDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final int OBSERVACIONES_MAX = 500;
    private static final int MOTIVO_MAX = 200;

    private final AlumnoRepository alumnoRepository;
    private final GrupoDeporteRepository grupoRepository;
    private final InscripcionDeporteRepository inscripcionDeporteRepository;
    private final RecorridoTransporteRepository recorridoRepository;
    private final InscripcionTransporteRepository inscripcionTransporteRepository;
    private final InscripcionComedorRepository inscripcionComedorRepository;
    private final IdiomaRepository idiomaRepository;
    private final InscripcionIdiomaRepository inscripcionIdiomaRepository;
    private final InscripcionApoyoRepository inscripcionApoyoRepository;
    private final AtencionEnfermeriaRepository atencionEnfermeriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
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

    @Transactional
    public AlumnoServiciosDTO inscribirIdioma(Long alumnoId, Short idiomaId) {
        Alumno alumno = buscarAlumno(alumnoId);
        Idioma idioma = idiomaId == null ? null : idiomaRepository.findById(idiomaId).orElse(null);
        if (idioma == null) {
            throw new ValidacionException("No se pudo inscribir al alumno",
                    Map.of("idiomaId", "El idioma seleccionado no existe"));
        }
        if (inscripcionIdiomaRepository.existsByAlumno_IdAndIdioma_Id(alumnoId, idiomaId)) {
            throw new ValidacionException("No se pudo inscribir al alumno",
                    Map.of("idiomaId", "El alumno ya cursa " + idioma.getNombre()));
        }

        InscripcionIdioma inscripcion = new InscripcionIdioma();
        inscripcion.setAlumno(alumno);
        inscripcion.setIdioma(idioma);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcionIdiomaRepository.save(inscripcion);

        auditoriaService.registrar("INSCRIPCION", "IDIOMA", alumnoId, "Inscripción a " + idioma.getNombre());
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO quitarIdioma(Long alumnoId, Long inscripcionId) {
        Alumno alumno = buscarAlumno(alumnoId);
        InscripcionIdioma inscripcion = inscripcionIdiomaRepository.findById(inscripcionId)
                .filter(i -> i.getAlumno().getId().equals(alumnoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("La inscripción no existe para este alumno"));
        String idioma = inscripcion.getIdioma().getNombre();
        inscripcionIdiomaRepository.delete(inscripcion);

        auditoriaService.registrar("BAJA", "IDIOMA", alumnoId, "Baja de " + idioma);
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO asignarApoyo(Long alumnoId, String observaciones) {
        Alumno alumno = buscarAlumno(alumnoId);
        String texto = observaciones == null || observaciones.isBlank() ? null : observaciones.strip();
        if (texto != null && texto.length() > OBSERVACIONES_MAX) {
            throw new ValidacionException("No se pudo registrar el apoyo",
                    Map.of("observaciones", "Las observaciones no pueden superar los " + OBSERVACIONES_MAX + " caracteres"));
        }

        InscripcionApoyo apoyo = inscripcionApoyoRepository.findById(alumnoId).orElseGet(() -> {
            InscripcionApoyo nuevo = new InscripcionApoyo();
            nuevo.setAlumnoId(alumnoId);
            nuevo.setFechaInicio(LocalDate.now());
            return nuevo;
        });
        apoyo.setObservaciones(texto);
        inscripcionApoyoRepository.save(apoyo);

        auditoriaService.registrar("ALTA", "APOYO_ESTUDIANTIL", alumnoId, "Inscripción al Servicio de Apoyo Estudiantil");
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoServiciosDTO quitarApoyo(Long alumnoId) {
        Alumno alumno = buscarAlumno(alumnoId);
        inscripcionApoyoRepository.deleteById(alumnoId);
        auditoriaService.registrar("BAJA", "APOYO_ESTUDIANTIL", alumnoId, "Baja del Servicio de Apoyo Estudiantil");
        return toDTO(alumno);
    }

    @Transactional
    public AtencionEnfermeriaDTO registrarAtencion(Long alumnoId, String motivo, String observaciones) {
        Alumno alumno = buscarAlumno(alumnoId);
        String texto = motivo == null ? null : motivo.strip();
        Map<String, String> errores = new LinkedHashMap<>();
        if (texto == null || texto.length() < 3 || texto.length() > MOTIVO_MAX) {
            errores.put("motivo", "El motivo debe tener entre 3 y " + MOTIVO_MAX + " caracteres");
        }
        String obs = observaciones == null || observaciones.isBlank() ? null : observaciones.strip();
        if (obs != null && obs.length() > OBSERVACIONES_MAX) {
            errores.put("observaciones", "Las observaciones no pueden superar los " + OBSERVACIONES_MAX + " caracteres");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo registrar la atención", errores);
        }

        AtencionEnfermeria atencion = new AtencionEnfermeria();
        atencion.setAlumno(alumno);
        atencion.setFechaHora(LocalDateTime.now());
        atencion.setMotivo(texto);
        atencion.setObservaciones(obs);
        atencion.setRegistradoPor(usuarioActual.id().flatMap(usuarioRepository::findById).orElse(null));
        atencionEnfermeriaRepository.save(atencion);

        auditoriaService.registrar("ALTA", "ENFERMERIA", alumnoId, "Atención en enfermería: " + texto);
        return toDTO(atencion);
    }

    @Transactional(readOnly = true)
    public List<AtencionEnfermeriaDTO> atencionesEnfermeria(Long alumnoId) {
        buscarAlumno(alumnoId);
        return atencionEnfermeriaRepository.findByAlumno_IdOrderByFechaHoraDesc(alumnoId).stream()
                .map(AlumnoServiciosService::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IdiomaDTO> idiomas() {
        return idiomaRepository.findAllByOrderByNombreAsc().stream()
                .map(i -> new IdiomaDTO(i.getId(), i.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecorridoDTO> recorridos() {
        return recorridoRepository.findAllByOrderByIdAsc().stream()
                .map(r -> new RecorridoDTO(r.getId(), r.getNombre()))
                .toList();
    }

    /** Los recorridos son exactamente cuatro (RN-002): se pueden renombrar, no crear ni borrar. */
    @Transactional
    public RecorridoDTO renombrarRecorrido(Short id, String nombre) {
        RecorridoTransporte recorrido = recorridoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recorrido con ID " + id + " no encontrado"));
        String nuevo = nombre == null ? null : nombre.strip();
        Map<String, String> errores = new LinkedHashMap<>();
        if (nuevo == null || nuevo.length() < 3 || nuevo.length() > 80) {
            errores.put("nombre", "El nombre debe tener entre 3 y 80 caracteres");
        } else if (recorridoRepository.existsByNombreIgnoreCaseAndIdNot(nuevo, id)) {
            errores.put("nombre", "Ya existe un recorrido con ese nombre");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo renombrar el recorrido", errores);
        }

        String anterior = recorrido.getNombre();
        recorrido.setNombre(nuevo);
        recorridoRepository.save(recorrido);
        auditoriaService.registrar("MODIFICACION", "RECORRIDO", null,
                "Recorrido renombrado: '" + anterior + "' -> '" + nuevo + "'");
        return new RecorridoDTO(recorrido.getId(), recorrido.getNombre());
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
        List<IdiomaInscriptoDTO> idiomas = inscripcionIdiomaRepository
                .findByAlumno_IdOrderByIdioma_NombreAsc(alumno.getId()).stream()
                .map(i -> new IdiomaInscriptoDTO(i.getId(), i.getIdioma().getId(), i.getIdioma().getNombre(),
                        i.getFechaInscripcion()))
                .toList();
        ApoyoDTO apoyo = inscripcionApoyoRepository.findById(alumno.getId())
                .map(a -> new ApoyoDTO(a.getFechaInicio(), a.getObservaciones()))
                .orElse(null);
        return new AlumnoServiciosDTO(alumno.getId(),
                alumno.getApellido() + ", " + alumno.getNombre(),
                alumno.getCurso().getNivel().getNombreLegible(),
                deportes, transporte, comedor, idiomas, apoyo);
    }

    private static AtencionEnfermeriaDTO toDTO(AtencionEnfermeria atencion) {
        return new AtencionEnfermeriaDTO(
                atencion.getId(),
                atencion.getFechaHora(),
                atencion.getMotivo(),
                atencion.getObservaciones(),
                atencion.getRegistradoPor() == null ? null : atencion.getRegistradoPor().getUsername());
    }
}
