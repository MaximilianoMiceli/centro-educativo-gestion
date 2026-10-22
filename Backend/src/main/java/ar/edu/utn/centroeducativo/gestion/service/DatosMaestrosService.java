package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.Materia;
import ar.edu.utn.centroeducativo.gestion.domain.NivelEducativo;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.CursoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.MateriaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.NivelRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearCursoRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearMateriaRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearNivelRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.CursoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.MateriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.NivelDTO;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatosMaestrosService {

    private static final int MAX_ANIO = 7;
    private static final Pattern SECCION = Pattern.compile("[A-Z]{1,2}");

    private final NivelRepository nivelRepository;
    private final CursoRepository cursoRepository;
    private final MateriaRepository materiaRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<NivelDTO> listarNiveles() {
        return nivelRepository.findAll().stream()
                .sorted(Comparator.comparing(NivelEducativo::getId))
                .map(n -> new NivelDTO(n.getId(), n.getNombre()))
                .toList();
    }

    @Transactional
    public NivelDTO crearNivel(CrearNivelRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (request.nombre() == null || request.nombre().isEmpty() || request.nombre().length() > 30) {
            errores.put("nombre", "El nombre del nivel debe tener entre 1 y 30 caracteres");
        } else if (nivelRepository.existsByNombreIgnoreCase(request.nombre())) {
            errores.put("nombre", "Ya existe un nivel con ese nombre");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        short siguienteId = (short) (nivelRepository.findAll().stream()
                .map(NivelEducativo::getId)
                .max(Comparator.naturalOrder())
                .orElse((short) 0) + 1);
        NivelEducativo nivel = new NivelEducativo();
        nivel.setId(siguienteId);
        nivel.setNombre(request.nombre());
        nivelRepository.save(nivel);

        auditoriaService.registrar("ALTA", "NIVEL", (long) siguienteId, "Nivel creado: " + request.nombre());
        return new NivelDTO(siguienteId, request.nombre());
    }

    @Transactional(readOnly = true)
    public List<CursoDTO> listarCursos(Short nivelId) {
        List<Curso> cursos = nivelId == null
                ? cursoRepository.findAllByOrderByNivel_IdAscAnioAscSeccionAsc()
                : cursoRepository.findByNivel_IdOrderByAnioAscSeccionAsc(nivelId);
        return cursos.stream().map(this::toDTO).toList();
    }

    @Transactional
    public CursoDTO crearCurso(CrearCursoRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        NivelEducativo nivel = request.nivelId() == null ? null
                : nivelRepository.findById(request.nivelId()).orElse(null);
        if (nivel == null) {
            errores.put("nivelId", "El nivel seleccionado no existe");
        }
        if (request.anio() == null || request.anio() < 1 || request.anio() > MAX_ANIO) {
            errores.put("anio", "El año debe estar entre 1 y " + MAX_ANIO);
        }
        if (request.seccion() == null || !SECCION.matcher(request.seccion()).matches()) {
            errores.put("seccion", "La sección debe tener 1 o 2 letras");
        }
        if (errores.isEmpty() && cursoRepository.existsByNivel_IdAndAnioAndSeccion(
                request.nivelId(), request.anio(), request.seccion())) {
            errores.put("seccion", "Ya existe un curso con ese año y sección en el nivel");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        Curso curso = new Curso();
        curso.setNivel(nivel);
        curso.setAnio(request.anio());
        curso.setSeccion(request.seccion());
        cursoRepository.save(curso);

        auditoriaService.registrar("ALTA", "CURSO", curso.getId(), "Curso creado: " + curso.getDescripcion());
        return toDTO(curso);
    }

    @Transactional(readOnly = true)
    public List<MateriaDTO> listarMaterias() {
        return materiaRepository.findAllByOrderByNombreAsc().stream()
                .map(m -> new MateriaDTO(m.getId(), m.getNombre()))
                .toList();
    }

    @Transactional
    public MateriaDTO crearMateria(CrearMateriaRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (request.nombre() == null || request.nombre().length() < 2 || request.nombre().length() > 80) {
            errores.put("nombre", "El nombre de la materia debe tener entre 2 y 80 caracteres");
        } else if (materiaRepository.existsByNombreIgnoreCase(request.nombre())) {
            errores.put("nombre", "Ya existe una materia con ese nombre");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        Materia materia = new Materia();
        materia.setNombre(request.nombre());
        materiaRepository.save(materia);

        auditoriaService.registrar("ALTA", "MATERIA", materia.getId(), "Materia creada: " + request.nombre());
        return new MateriaDTO(materia.getId(), materia.getNombre());
    }

    private CursoDTO toDTO(Curso curso) {
        return new CursoDTO(
                curso.getId(),
                curso.getNivel().getId(),
                curso.getNivel().getNombre(),
                curso.getAnio(),
                curso.getSeccion(),
                curso.getDescripcion());
    }
}
