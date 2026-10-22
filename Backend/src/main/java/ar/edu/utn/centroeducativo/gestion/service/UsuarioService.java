package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.AlumnoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import ar.edu.utn.centroeducativo.gestion.repository.PadreRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RolRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.CrearUsuarioRequest;
import ar.edu.utn.centroeducativo.gestion.web.dto.UsuarioCreadoDTO;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
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
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LARGO_PASSWORD_TEMPORAL = 12;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final DocenteRepository docenteRepository;
    private final AlumnoRepository alumnoRepository;
    private final PadreRepository padreRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public UsuarioCreadoDTO crear(CrearUsuarioRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarFormato(request, errores);
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

        String passwordTemporal = generarPasswordTemporal();
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setTipoDocumento(request.tipoDocumento());
        usuario.setNumeroDocumento(request.numeroDocumento());
        usuario.setRol(rol);
        usuario.setPasswordHash(passwordEncoder.encode(passwordTemporal));
        usuario.setDebeCambiarPassword(true);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        auditoriaService.registrar("ALTA", "USUARIO", usuario.getId(),
                "Usuario creado: " + usuario.getUsername() + " (rol " + rol.getNombre() + ")");

        return new UsuarioCreadoDTO(usuario.getId(), usuario.getUsername(), usuario.getEmail(),
                rol.getNombre(), passwordTemporal);
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

    private String generarPasswordTemporal() {
        StringBuilder password = new StringBuilder(LARGO_PASSWORD_TEMPORAL);
        for (int i = 0; i < LARGO_PASSWORD_TEMPORAL; i++) {
            password.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return password.toString();
    }

    private static boolean coincide(Pattern patron, String valor) {
        return valor != null && patron.matcher(valor).matches();
    }
}
