package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.PadreRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RolRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearUsuarioRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.RestablecerPasswordRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.UsuarioCreadoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.UsuarioListaDTO;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Set<String> ROLES_ASIGNABLES = Set.of("ADMIN", "DIRECCION", "PROFESOR", "PADRE", "ALUMNO");
    private static final Set<String> TIPOS_DOCUMENTO = Set.of("DNI", "PASAPORTE", "LC", "LE");
    private static final Pattern USERNAME = Pattern.compile("\\S{5,20}");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+");
    private static final Pattern DNI = Pattern.compile("\\d{7,8}");
    private static final Pattern OTRO_DOCUMENTO = Pattern.compile("[A-Za-z0-9]{5,15}");
    private static final int LARGO_MINIMO_PASSWORD = 8;
    private static final int LARGO_MAXIMO_PASSWORD = 72;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final DocenteRepository docenteRepository;
    private final AlumnoRepository alumnoRepository;
    private final PadreRepository padreRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<UsuarioListaDTO> listar() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getUsername))
                .map(u -> new UsuarioListaDTO(u.getId(), u.getUsername(), u.getEmail(), u.getRol().getNombre(),
                        u.getNumeroDocumento(), u.isActivo(), u.isDebeCambiarPassword()))
                .toList();
    }

    @Transactional
    public UsuarioCreadoDTO crear(CrearUsuarioRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarFormato(request, errores);
        validarPassword(request.password(), errores, "password");
        if (!errores.containsKey("username") && usuarioRepository.existsByUsernameIgnoreCase(request.username())) {
            errores.put("username", "Ya existe un usuario con ese username");
        }
        if (!errores.containsKey("email") && usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            errores.put("email", "Ya existe un usuario con ese email");
        }
        if (!errores.containsKey("numeroDocumento")
                && usuarioRepository.existsByNumeroDocumento(request.numeroDocumento())) {
            errores.put("numeroDocumento", "Ya existe un usuario con ese documento");
        }

        Rol rol = null;
        if (!errores.containsKey("rol")) {
            rol = rolRepository.findByNombre(request.rol()).orElse(null);
            if (rol == null) {
                errores.put("rol", "Rol inválido");
            }
        }

        Usuario usuario = new Usuario();
        if (rol != null && !errores.containsKey("numeroDocumento")) {
            vincularPersona(usuario, request.rol(), request.numeroDocumento(), errores);
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en el formulario", errores);
        }

        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setTipoDocumento(request.tipoDocumento());
        usuario.setNumeroDocumento(request.numeroDocumento());
        usuario.setRol(rol);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setDebeCambiarPassword(request.debeCambiarPassword() == null || request.debeCambiarPassword());
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        auditoriaService.registrar("ALTA", "USUARIO", usuario.getId(),
                "Usuario creado: " + usuario.getUsername() + " (rol " + rol.getNombre() + ")");

        return new UsuarioCreadoDTO(usuario.getId(), usuario.getUsername(), usuario.getEmail(), rol.getNombre());
    }

    @Transactional
    public void restablecerPassword(Long usuarioId, RestablecerPasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + usuarioId + " no encontrado"));

        Map<String, String> errores = new LinkedHashMap<>();
        validarPassword(request.password(), errores, "password");
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo restablecer la contraseña", errores);
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setDebeCambiarPassword(request.debeCambiarPassword() == null || request.debeCambiarPassword());
        usuarioRepository.save(usuario);

        auditoriaService.registrar("RESTABLECER_PASSWORD", "USUARIO", usuario.getId(),
                "Contraseña restablecida por un administrador para " + usuario.getUsername());
    }

    private static void validarPassword(String password, Map<String, String> errores, String campo) {
        if (password == null || password.length() < LARGO_MINIMO_PASSWORD || password.length() > LARGO_MAXIMO_PASSWORD) {
            errores.put(campo, "La contraseña debe tener entre 8 y 72 caracteres");
        }
    }

    private static void validarFormato(CrearUsuarioRequest request, Map<String, String> errores) {
        if (!coincide(USERNAME, request.username())) {
            errores.put("username", "El username debe tener entre 5 y 20 caracteres, sin espacios");
        }
        if (!coincide(EMAIL, request.email())) {
            errores.put("email", "Email inválido. Use formato: ejemplo@dominio.com");
        }
        if (request.tipoDocumento() == null || !TIPOS_DOCUMENTO.contains(request.tipoDocumento())) {
            errores.put("tipoDocumento", "Tipo de documento inválido");
        }
        Pattern patronDocumento = "DNI".equals(request.tipoDocumento()) ? DNI : OTRO_DOCUMENTO;
        if (!coincide(patronDocumento, request.numeroDocumento())) {
            errores.put("numeroDocumento", "Número de documento inválido");
        }
        if (request.rol() == null || !ROLES_ASIGNABLES.contains(request.rol())) {
            errores.put("rol", "Rol inválido");
        }
    }

    private void vincularPersona(Usuario usuario, String rol, String dni, Map<String, String> errores) {
        switch (rol) {
            case "PROFESOR" -> docenteRepository.findByDni(dni).ifPresentOrElse(
                    usuario::setDocente,
                    () -> errores.put("numeroDocumento", "No existe un docente con ese DNI"));
            case "ALUMNO" -> alumnoRepository.findByDni(dni).ifPresentOrElse(
                    usuario::setAlumno,
                    () -> errores.put("numeroDocumento", "No existe un alumno con ese DNI"));
            case "PADRE" -> padreRepository.findByDni(dni).ifPresentOrElse(
                    usuario::setPadre,
                    () -> errores.put("numeroDocumento", "No existe un padre con ese DNI"));
            default -> {
            }
        }
    }

    private static boolean coincide(Pattern patron, String valor) {
        return valor != null && patron.matcher(valor).matches();
    }
}
