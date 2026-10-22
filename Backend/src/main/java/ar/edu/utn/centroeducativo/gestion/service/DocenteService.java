package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.domain.EstadoDocente;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearDocenteRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteUpdateRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ControlEdicionDocente controlEdicion;

    @Transactional
    public DocenteDTO crear(CrearDocenteRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        String legajo = request.legajo() == null ? null : request.legajo().toUpperCase();
        if (legajo == null || !legajo.matches("[A-Z0-9]{3,20}")) {
            errores.put("legajo", "El legajo debe tener entre 3 y 20 letras o números");
        } else if (docenteRepository.findByLegajoIgnoreCase(legajo).isPresent()) {
            errores.put("legajo", "Ya existe un docente con ese legajo");
        }

        DocenteUpdateRequest campos = new DocenteUpdateRequest(request.dni(), request.nombre(), request.apellido(),
                request.especialidad(), request.email(), request.telefono(), request.estado());
        errores.putAll(DocenteValidador.validar(campos));
        if (!errores.containsKey("dni") && docenteRepository.existsByDni(campos.dni())) {
            errores.put("dni", "Ya existe un docente con ese DNI");
        }
        if (!errores.containsKey("email") && campos.email() != null && docenteRepository.existsByEmail(campos.email())) {
            errores.put("email", "Ya existe un docente con ese email");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        Docente docente = new Docente();
        docente.setLegajo(legajo);
        docente.setDni(campos.dni());
        docente.setNombre(campos.nombre());
        docente.setApellido(campos.apellido());
        docente.setEspecialidad(campos.especialidad());
        docente.setEmail(campos.email());
        docente.setTelefono(campos.telefono());
        docente.setEstado(DocenteValidador.estadoValido(campos.estado()));
        docenteRepository.save(docente);

        auditoriaService.registrar("ALTA", "DOCENTE", docente.getId(),
                "Docente creado: " + legajo + " " + campos.apellido() + ", " + campos.nombre());
        return toDTO(docente);
    }

    @Transactional(readOnly = true)
    public List<DocenteDTO> listar() {
        return docenteRepository.findAll().stream()
                .sorted(Comparator.comparing(Docente::getApellido).thenComparing(Docente::getNombre))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocenteDTO buscarPorLegajo(String legajo) {
        Docente docente = docenteRepository.findByLegajoIgnoreCase(legajo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Docente con legajo " + legajo + " no encontrado"));
        return toDTO(docente);
    }

    @Transactional(readOnly = true)
    public DocenteDTO buscarPorDni(String dni) {
        Docente docente = docenteRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Docente con DNI " + dni + " no encontrado"));
        return toDTO(docente);
    }

    @Transactional
    public DocenteDTO actualizar(Long id, DocenteUpdateRequest request) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente con ID " + id + " no encontrado"));
        controlEdicion.verificarNoBloqueado(docente);

        Map<String, String> errores = new LinkedHashMap<>(DocenteValidador.validar(request));
        validarUnicidad(docente, request, errores);
        if (!errores.isEmpty()) {
            controlEdicion.registrarIntentoFallido(id);
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        String cambios = aplicarCambios(docente, request);
        docente.setIntentosEdicionFallidos((short) 0);
        docenteRepository.save(docente);

        if (!cambios.isEmpty()) {
            auditoriaService.registrar("MODIFICACION", "DOCENTE", id, cambios);
        }
        return toDTO(docente);
    }

    private void validarUnicidad(Docente docente, DocenteUpdateRequest request, Map<String, String> errores) {
        if (!errores.containsKey("dni") && request.dni() != null && !request.dni().equals(docente.getDni())) {
            if (usuarioRepository.existsByDocente_Id(docente.getId())) {
                errores.put("dni", "El DNI no se puede modificar porque el docente tiene un usuario asociado");
            } else if (docenteRepository.existsByDniAndIdNot(request.dni(), docente.getId())) {
                errores.put("dni", "Ya existe un docente con ese DNI");
            }
        }
        if (!errores.containsKey("email") && request.email() != null
                && !request.email().equals(docente.getEmail())
                && docenteRepository.existsByEmailAndIdNot(request.email(), docente.getId())) {
            errores.put("email", "Ya existe un docente con ese email");
        }
    }

    private String aplicarCambios(Docente docente, DocenteUpdateRequest request) {
        EstadoDocente nuevoEstado = DocenteValidador.estadoValido(request.estado());
        List<String> cambios = new ArrayList<>();

        registrarCambio(cambios, "dni", docente.getDni(), request.dni());
        registrarCambio(cambios, "nombre", docente.getNombre(), request.nombre());
        registrarCambio(cambios, "apellido", docente.getApellido(), request.apellido());
        registrarCambio(cambios, "especialidad", docente.getEspecialidad(), request.especialidad());
        registrarCambio(cambios, "email", docente.getEmail(), request.email());
        registrarCambio(cambios, "telefono", docente.getTelefono(), request.telefono());
        registrarCambio(cambios, "estado", String.valueOf(docente.getEstado()), nuevoEstado.name());

        docente.setDni(request.dni());
        docente.setNombre(request.nombre());
        docente.setApellido(request.apellido());
        docente.setEspecialidad(request.especialidad());
        docente.setEmail(request.email());
        docente.setTelefono(request.telefono());
        docente.setEstado(nuevoEstado);

        return String.join("; ", cambios);
    }

    private static void registrarCambio(List<String> cambios, String campo, String anterior, String nuevo) {
        if (!Objects.equals(anterior, nuevo)) {
            cambios.add(campo + ": '" + anterior + "' -> '" + nuevo + "'");
        }
    }

    private DocenteDTO toDTO(Docente docente) {
        List<String> cursos = docente.getCursos().stream()
                .map(Curso::getDescripcion)
                .sorted()
                .toList();
        boolean dniEditable = !usuarioRepository.existsByDocente_Id(docente.getId());
        return new DocenteDTO(
                docente.getId(),
                docente.getLegajo(),
                docente.getDni(),
                docente.getNombre(),
                docente.getApellido(),
                docente.getEspecialidad(),
                docente.getEmail(),
                docente.getTelefono(),
                docente.getEstado().name(),
                dniEditable,
                cursos);
    }
}
