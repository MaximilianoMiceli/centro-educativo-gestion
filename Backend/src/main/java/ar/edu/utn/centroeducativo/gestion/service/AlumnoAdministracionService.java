package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import ar.edu.utn.centroeducativo.gestion.domain.Curso;
import ar.edu.utn.centroeducativo.gestion.domain.EstadoAlumno;
import ar.edu.utn.centroeducativo.gestion.domain.Horario;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.CursoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DictadoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.HorarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoFichaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.AlumnoMateriaDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.DatosAlumnoRequest;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlumnoAdministracionService {

    private static final Pattern LEGAJO = Pattern.compile("[A-Z0-9]{3,20}");
    private static final Pattern DNI = Pattern.compile("\\d{7,8}");
    private static final Pattern NOMBRE = Pattern.compile("\\p{L}+(?:[ '\\-]\\p{L}+)*");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+");
    private static final Pattern TELEFONO = Pattern.compile("\\d{10}");
    private static final int EDAD_MINIMA = 3;
    private static final int EDAD_MAXIMA = 25;

    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final DictadoRepository dictadoRepository;
    private final HorarioRepository horarioRepository;
    private final AuditoriaService auditoriaService;
    private final AccesoAlumnos accesoAlumnos;

    @Transactional(readOnly = true)
    public List<AlumnoFichaDTO> listar(String busqueda, Short nivelId, Long cursoId) {
        List<Alumno> alumnos = cursoId == null
                ? alumnoRepository.findAll()
                : alumnoRepository.findByCurso_Id(cursoId);
        String termino = busqueda == null ? "" : busqueda.strip().toLowerCase();
        return alumnos.stream()
                .filter(accesoAlumnos.criterio())
                .filter(alumno -> nivelId == null || nivelId.equals(alumno.getCurso().getNivel().getId()))
                .filter(alumno -> termino.isEmpty() || coincide(alumno, termino))
                .sorted(Comparator.comparing(Alumno::getApellido).thenComparing(Alumno::getNombre))
                .map(this::toDTO)
                .toList();
    }

    private static boolean coincide(Alumno alumno, String termino) {
        return alumno.getLegajo().toLowerCase().contains(termino)
                || alumno.getDni().contains(termino)
                || alumno.getNombre().toLowerCase().contains(termino)
                || alumno.getApellido().toLowerCase().contains(termino);
    }

    @Transactional
    public AlumnoFichaDTO crear(DatosAlumnoRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarCampos(request, errores);
        if (!errores.containsKey("legajo") && alumnoRepository.existsByLegajoIgnoreCase(request.legajo())) {
            errores.put("legajo", "Ya existe un alumno con ese legajo");
        }
        if (!errores.containsKey("dni") && alumnoRepository.existsByDni(request.dni())) {
            errores.put("dni", "Ya existe un alumno con ese DNI");
        }
        if (!errores.containsKey("email") && request.email() != null
                && alumnoRepository.existsByEmailIgnoreCase(request.email())) {
            errores.put("email", "Ya existe un alumno con ese email");
        }
        Curso curso = resolverCurso(request, errores);
        EstadoAlumno estado = resolverEstado(request.estado(), errores);
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        Alumno alumno = new Alumno();
        alumno.setLegajo(request.legajo().toUpperCase());
        aplicarDatos(alumno, request, curso, estado);
        alumnoRepository.save(alumno);

        auditoriaService.registrar("ALTA", "ALUMNO", alumno.getId(),
                "Alumno creado: " + alumno.getLegajo() + " " + alumno.getApellido() + ", " + alumno.getNombre());
        return toDTO(alumno);
    }

    @Transactional
    public AlumnoFichaDTO modificar(Long id, DatosAlumnoRequest request) {
        Alumno alumno = buscar(id);

        Map<String, String> errores = new LinkedHashMap<>();
        validarCampos(request, errores);
        if (!errores.containsKey("dni") && !request.dni().equals(alumno.getDni())
                && alumnoRepository.existsByDniAndIdNot(request.dni(), id)) {
            errores.put("dni", "Ya existe un alumno con ese DNI");
        }
        if (!errores.containsKey("email") && request.email() != null
                && !request.email().equals(alumno.getEmail())
                && alumnoRepository.existsByEmailAndIdNot(request.email(), id)) {
            errores.put("email", "Ya existe un alumno con ese email");
        }
        Curso curso = resolverCurso(request, errores);
        EstadoAlumno estado = resolverEstado(request.estado(), errores);
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        List<String> cambios = new ArrayList<>();
        registrarCambio(cambios, "dni", alumno.getDni(), request.dni());
        registrarCambio(cambios, "nombre", alumno.getNombre(), request.nombre());
        registrarCambio(cambios, "apellido", alumno.getApellido(), request.apellido());
        registrarCambio(cambios, "fechaNacimiento", alumno.getFechaNacimiento(), request.fechaNacimiento());
        registrarCambio(cambios, "domicilio", alumno.getDomicilio(), request.domicilio());
        registrarCambio(cambios, "telefono", alumno.getTelefono(), request.telefono());
        registrarCambio(cambios, "email", alumno.getEmail(), request.email());
        registrarCambio(cambios, "curso", alumno.getCurso().getDescripcion(), curso.getDescripcion());
        registrarCambio(cambios, "estado", alumno.getEstado(), estado);

        aplicarDatos(alumno, request, curso, estado);
        alumnoRepository.save(alumno);

        if (!cambios.isEmpty()) {
            auditoriaService.registrar("MODIFICACION", "ALUMNO", id, String.join("; ", cambios));
        }
        return toDTO(alumno);
    }

    @Transactional(readOnly = true)
    public List<AlumnoMateriaDTO> materias(Long alumnoId) {
        Alumno alumno = buscar(alumnoId);
        accesoAlumnos.verificar(alumno);
        return dictadoRepository.findByCurso_IdOrderByMateria_NombreAsc(alumno.getCurso().getId()).stream()
                .map(dictado -> new AlumnoMateriaDTO(
                        dictado.getMateria().getNombre(),
                        dictado.getDocente().getApellido() + ", " + dictado.getDocente().getNombre(),
                        horarioRepository.findByDictado_IdOrderByDiaSemanaAscHoraInicioAsc(dictado.getId()).stream()
                                .map(FormatoHorario::formatear)
                                .toList()))
                .toList();
    }

    private Alumno buscar(Long id) {
        return alumnoRepository.findWithCursoById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno con ID " + id + " no encontrado"));
    }

    private void validarCampos(DatosAlumnoRequest request, Map<String, String> errores) {
        if (request.legajo() == null || !LEGAJO.matcher(request.legajo().toUpperCase()).matches()) {
            errores.put("legajo", "El legajo debe tener entre 3 y 20 letras o números");
        }
        if (request.dni() == null || !DNI.matcher(request.dni()).matches()) {
            errores.put("dni", "DNI inválido. Debe tener 7 u 8 dígitos");
        }
        if (!coincide(NOMBRE, request.nombre()) || request.nombre().length() > 80) {
            errores.put("nombre", "Nombre inválido. Solo letras, espacios, guiones y apóstrofos");
        }
        if (!coincide(NOMBRE, request.apellido()) || request.apellido().length() > 80) {
            errores.put("apellido", "Apellido inválido. Solo letras, espacios, guiones y apóstrofos");
        }
        validarFechaNacimiento(request.fechaNacimiento(), errores);
        if (request.domicilio() != null && request.domicilio().length() > 200) {
            errores.put("domicilio", "El domicilio no puede superar los 200 caracteres");
        }
        if (request.telefono() != null && !request.telefono().isEmpty()
                && !TELEFONO.matcher(request.telefono()).matches()) {
            errores.put("telefono", "Teléfono inválido. Debe tener 10 dígitos");
        }
        if (request.email() != null && !request.email().isEmpty()
                && (!EMAIL.matcher(request.email()).matches() || request.email().length() > 120)) {
            errores.put("email", "Email inválido. Use formato: ejemplo@dominio.com");
        }
    }

    private static void validarFechaNacimiento(LocalDate fecha, Map<String, String> errores) {
        if (fecha == null) {
            errores.put("fechaNacimiento", "Ingresá la fecha de nacimiento");
            return;
        }
        int edad = Period.between(fecha, LocalDate.now()).getYears();
        if (fecha.isAfter(LocalDate.now()) || edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
            errores.put("fechaNacimiento", "La edad debe estar entre " + EDAD_MINIMA + " y " + EDAD_MAXIMA + " años");
        }
    }

    private Curso resolverCurso(DatosAlumnoRequest request, Map<String, String> errores) {
        Curso curso = request.cursoId() == null ? null : cursoRepository.findById(request.cursoId()).orElse(null);
        if (curso == null) {
            errores.put("cursoId", "El curso seleccionado no existe");
        }
        return curso;
    }

    private static EstadoAlumno resolverEstado(String estado, Map<String, String> errores) {
        if (estado == null) {
            errores.put("estado", "Seleccioná un estado");
            return null;
        }
        try {
            return EstadoAlumno.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            errores.put("estado", "Estado inválido. Use Activo, Inactivo o Egresado");
            return null;
        }
    }

    private static void aplicarDatos(Alumno alumno, DatosAlumnoRequest request, Curso curso, EstadoAlumno estado) {
        alumno.setDni(request.dni());
        alumno.setNombre(request.nombre());
        alumno.setApellido(request.apellido());
        alumno.setFechaNacimiento(request.fechaNacimiento());
        alumno.setDomicilio(blankAVacio(request.domicilio()));
        alumno.setTelefono(blankAVacio(request.telefono()));
        alumno.setEmail(blankAVacio(request.email()));
        alumno.setCurso(curso);
        alumno.setEstado(estado);
    }

    private static String blankAVacio(String valor) {
        return valor == null || valor.isEmpty() ? null : valor;
    }

    private static void registrarCambio(List<String> cambios, String campo, Object anterior, Object nuevo) {
        if (!Objects.equals(anterior, nuevo)) {
            cambios.add(campo + ": '" + anterior + "' -> '" + nuevo + "'");
        }
    }

    private static boolean coincide(Pattern patron, String valor) {
        return valor != null && patron.matcher(valor).matches();
    }

    private AlumnoFichaDTO toDTO(Alumno alumno) {
        Curso curso = alumno.getCurso();
        return new AlumnoFichaDTO(
                alumno.getId(),
                alumno.getLegajo(),
                alumno.getDni(),
                alumno.getNombre(),
                alumno.getApellido(),
                alumno.getFechaNacimiento(),
                alumno.getDomicilio(),
                alumno.getTelefono(),
                alumno.getEmail(),
                curso.getId(),
                curso.getDescripcion(),
                curso.getNivel().getNombre(),
                alumno.getEstado().name());
    }
}
