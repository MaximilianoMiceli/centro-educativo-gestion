package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Permiso;
import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.PermisoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RolRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.PermisoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.RolPermisosDTO;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisosService {

    static final String PERMISO_ADMINISTRAR = "PERMISOS_ADMINISTRAR";
    private static final String ROL_ADMIN = "ADMIN";

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Set<String> codigosPorRol(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .map(rol -> rol.getPermisos().stream().map(Permiso::getCodigo).collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    @Transactional(readOnly = true)
    public List<PermisoDTO> listarPermisos() {
        return permisoRepository.findAll().stream()
                .sorted(Comparator.comparing(Permiso::getCodigo))
                .map(p -> new PermisoDTO(p.getCodigo(), p.getDescripcion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RolPermisosDTO> listarRoles() {
        return rolRepository.findAll().stream()
                .sorted(Comparator.comparing(Rol::getNombre))
                .map(rol -> new RolPermisosDTO(rol.getNombre(), codigosOrdenados(rol)))
                .toList();
    }

    @Transactional
    public RolPermisosDTO actualizarPermisos(String nombreRol, List<String> codigos) {
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol " + nombreRol + " no encontrado"));

        Set<String> solicitados = codigos == null ? new TreeSet<>() : codigos.stream()
                .filter(Objects::nonNull)
                .map(String::strip)
                .collect(Collectors.toCollection(TreeSet::new));
        List<Permiso> encontrados = permisoRepository.findByCodigoIn(solicitados);

        Set<String> existentes = encontrados.stream().map(Permiso::getCodigo).collect(Collectors.toSet());
        Set<String> inexistentes = new TreeSet<>(solicitados);
        inexistentes.removeAll(existentes);

        Map<String, String> errores = new LinkedHashMap<>();
        if (!inexistentes.isEmpty()) {
            errores.put("permisos", "Permisos inexistentes: " + String.join(", ", inexistentes));
        } else if (ROL_ADMIN.equals(nombreRol) && !solicitados.contains(PERMISO_ADMINISTRAR)) {
            errores.put("permisos", "El rol ADMIN debe conservar " + PERMISO_ADMINISTRAR);
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudieron guardar los permisos", errores);
        }

        String antes = String.join(", ", codigosOrdenados(rol));
        rol.setPermisos(new HashSet<>(encontrados));
        rolRepository.save(rol);
        List<String> despues = codigosOrdenados(rol);

        auditoriaService.registrar("MODIFICACION", "ROL", rol.getId().longValue(),
                "Permisos del rol " + nombreRol + ": [" + antes + "] -> [" + String.join(", ", despues) + "]");

        return new RolPermisosDTO(nombreRol, despues);
    }

    private static List<String> codigosOrdenados(Rol rol) {
        return rol.getPermisos().stream().map(Permiso::getCodigo).sorted().toList();
    }
}
