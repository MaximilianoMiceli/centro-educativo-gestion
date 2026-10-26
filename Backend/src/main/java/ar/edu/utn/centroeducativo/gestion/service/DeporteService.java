package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Deporte;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.GrupoDeporte;
import ar.edu.utn.centroeducativo.gestion.domain.NivelEducativo;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.DeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.GrupoDeporteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.NivelRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.DeporteDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DeporteDTO.GrupoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.AsignarResponsable;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.CrearDeporte;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudesDeportes.CrearGrupo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeporteService {

    private final DeporteRepository deporteRepository;
    private final GrupoDeporteRepository grupoRepository;
    private final NivelRepository nivelRepository;
    private final DocenteRepository docenteRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<DeporteDTO> listar() {
        return deporteRepository.findAllByOrderByNombreAsc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public DeporteDTO crear(CrearDeporte request) {
        Map<String, String> errores = new LinkedHashMap<>();
        String nombre = request.nombre() == null ? null : request.nombre().strip();
        if (nombre == null || nombre.length() < 2 || nombre.length() > 80) {
            errores.put("nombre", "El nombre del deporte debe tener entre 2 y 80 caracteres");
        } else if (deporteRepository.existsByNombreIgnoreCase(nombre)) {
            errores.put("nombre", "Ya existe un deporte con ese nombre");
        }
        if (request.docenteResponsableId() == null) {
            errores.put("docenteResponsableId", "Asigná un profesor responsable: todo deporte debe tener uno");
        }
        Docente responsable = resolverDocente(request.docenteResponsableId(), errores);
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        Deporte deporte = new Deporte();
        deporte.setNombre(nombre);
        deporte.setDocenteResponsable(responsable);
        deporteRepository.save(deporte);

        auditoriaService.registrar("ALTA", "DEPORTE", deporte.getId(), "Deporte creado: " + nombre);
        return toDTO(deporte);
    }

    @Transactional
    public DeporteDTO asignarResponsable(Long deporteId, AsignarResponsable request) {
        Deporte deporte = buscar(deporteId);
        Map<String, String> errores = new LinkedHashMap<>();
        if (request.docenteId() == null) {
            errores.put("docenteResponsableId", "Todo deporte debe tener un profesor responsable");
        }
        Docente responsable = resolverDocente(request.docenteId(), errores);
        if (responsable != null) {
            for (GrupoDeporte grupo : grupoRepository.findByDeporte_IdOrderByNivel_IdAscDiaSemanaAscHoraInicioAsc(deporteId)) {
                List<GrupoDeporte> conflictos = grupoRepository.findConflictosDocenteEnOtrosDeportes(
                        responsable.getId(), deporteId, grupo.getDiaSemana(), grupo.getHoraInicio(), grupo.getHoraFin());
                if (!conflictos.isEmpty()) {
                    errores.put("docenteResponsableId", "El docente ya es responsable de "
                            + conflictos.get(0).getDeporte().getNombre() + " en ese horario");
                    break;
                }
            }
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo asignar el responsable", errores);
        }

        deporte.setDocenteResponsable(responsable);
        deporteRepository.save(deporte);
        auditoriaService.registrar("MODIFICACION", "DEPORTE", deporte.getId(),
                "Responsable del deporte " + deporte.getNombre() + ": "
                        + (responsable == null ? "sin asignar" : responsable.getApellido() + ", " + responsable.getNombre()));
        return toDTO(deporte);
    }

    @Transactional
    public DeporteDTO crearGrupo(Long deporteId, CrearGrupo request) {
        Deporte deporte = buscar(deporteId);
        Map<String, String> errores = new LinkedHashMap<>();
        NivelEducativo nivel = request.nivelId() == null ? null
                : nivelRepository.findById(request.nivelId()).orElse(null);
        if (nivel == null) {
            errores.put("nivelId", "El nivel seleccionado no existe");
        }
        if (request.diaSemana() == null || request.diaSemana() < 1 || request.diaSemana() > 7) {
            errores.put("diaSemana", "El día debe estar entre 1 (lunes) y 7 (domingo)");
        }
        if (request.horaInicio() == null || request.horaFin() == null) {
            errores.put("horaInicio", "Ingresá el horario del grupo");
        } else if (!request.horaInicio().isBefore(request.horaFin())) {
            errores.put("horaFin", "La hora de fin debe ser posterior a la de inicio");
        }
        if (errores.isEmpty() && deporte.getDocenteResponsable() != null) {
            List<GrupoDeporte> conflictos = grupoRepository.findConflictosDocente(
                    deporte.getDocenteResponsable().getId(), request.diaSemana(),
                    request.horaInicio(), request.horaFin());
            if (!conflictos.isEmpty()) {
                errores.put("horaInicio", "El docente responsable ya tiene un grupo en ese horario");
            }
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el grupo", errores);
        }

        GrupoDeporte grupo = new GrupoDeporte();
        grupo.setDeporte(deporte);
        grupo.setNivel(nivel);
        grupo.setDiaSemana(request.diaSemana());
        grupo.setHoraInicio(request.horaInicio());
        grupo.setHoraFin(request.horaFin());
        grupoRepository.save(grupo);

        auditoriaService.registrar("ALTA", "GRUPO_DEPORTE", grupo.getId(),
                "Grupo de " + deporte.getNombre() + " (" + nivel.getNombreLegible() + "): "
                        + FormatoHorario.rango(grupo.getDiaSemana(), grupo.getHoraInicio(), grupo.getHoraFin()));
        return toDTO(deporte);
    }

    private Deporte buscar(Long id) {
        return deporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Deporte con ID " + id + " no encontrado"));
    }

    private Docente resolverDocente(Long docenteId, Map<String, String> errores) {
        if (docenteId == null) {
            return null;
        }
        Docente docente = docenteRepository.findById(docenteId).orElse(null);
        if (docente == null) {
            errores.put("docenteResponsableId", "El docente seleccionado no existe");
        }
        return docente;
    }

    private DeporteDTO toDTO(Deporte deporte) {
        List<GrupoDTO> grupos = grupoRepository.findByDeporte_IdOrderByNivel_IdAscDiaSemanaAscHoraInicioAsc(deporte.getId())
                .stream()
                .map(g -> new GrupoDTO(g.getId(), g.getNivel().getNombreLegible(),
                        FormatoHorario.rango(g.getDiaSemana(), g.getHoraInicio(), g.getHoraFin())))
                .toList();
        Docente responsable = deporte.getDocenteResponsable();
        return new DeporteDTO(
                deporte.getId(),
                deporte.getNombre(),
                responsable == null ? null : responsable.getId(),
                responsable == null ? null : responsable.getApellido() + ", " + responsable.getNombre(),
                grupos);
    }
}
