package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.centroeducativo.gestion.domain.Permiso;
import ar.edu.utn.centroeducativo.gestion.domain.Rol;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.PermisoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.RolRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermisosServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    private PermisosService permisosService;

    @BeforeEach
    void setUp() {
        permisosService = new PermisosService(rolRepository, permisoRepository, auditoriaService);
    }

    @Test
    void codigosPorRolDevuelveLosPermisosDelRol() {
        Rol profesor = rol((short) 3, "PROFESOR", permiso("ALUMNOS_CONSULTAR"));
        when(rolRepository.findByNombre("PROFESOR")).thenReturn(Optional.of(profesor));

        assertThat(permisosService.codigosPorRol("PROFESOR")).containsExactly("ALUMNOS_CONSULTAR");
    }

    @Test
    void codigosPorRolInexistenteNoDaPermisos() {
        when(rolRepository.findByNombre("FANTASMA")).thenReturn(Optional.empty());

        assertThat(permisosService.codigosPorRol("FANTASMA")).isEmpty();
    }

    @Test
    void actualizarPermisosReemplazaLosPermisosYAudita() {
        Rol profesor = rol((short) 3, "PROFESOR", permiso("ALUMNOS_CONSULTAR"));
        when(rolRepository.findByNombre("PROFESOR")).thenReturn(Optional.of(profesor));
        when(permisoRepository.findByCodigoIn(any())).thenReturn(List.of(permiso("DOCENTES_CONSULTAR")));

        var resultado = permisosService.actualizarPermisos("PROFESOR", List.of("DOCENTES_CONSULTAR"));

        assertThat(resultado.permisos()).containsExactly("DOCENTES_CONSULTAR");
        verify(auditoriaService).registrar(anyString(), anyString(), any(), anyString());
    }

    @Test
    void codigoInexistenteSeRechaza() {
        Rol profesor = rol((short) 3, "PROFESOR");
        when(rolRepository.findByNombre("PROFESOR")).thenReturn(Optional.of(profesor));
        when(permisoRepository.findByCodigoIn(any())).thenReturn(List.of());

        assertThatThrownBy(() -> permisosService.actualizarPermisos("PROFESOR", List.of("INVENTADO")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsEntry("permisos", "Permisos inexistentes: INVENTADO"));
    }

    @Test
    void adminNoPuedePerderPermisosAdministrar() {
        Rol admin = rol((short) 1, "ADMIN", permiso("PERMISOS_ADMINISTRAR"));
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(admin));
        when(permisoRepository.findByCodigoIn(any())).thenReturn(List.of(permiso("ALUMNOS_CONSULTAR")));

        assertThatThrownBy(() -> permisosService.actualizarPermisos("ADMIN", List.of("ALUMNOS_CONSULTAR")))
                .isInstanceOf(ValidacionException.class)
                .satisfies(ex -> assertThat(((ValidacionException) ex).getErrores())
                        .containsEntry("permisos", "El rol ADMIN debe conservar PERMISOS_ADMINISTRAR"));
    }

    private static Permiso permiso(String codigo) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(codigo);
        return permiso;
    }

    private static Rol rol(Short id, String nombre, Permiso... permisos) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        rol.setPermisos(new HashSet<>(Set.of(permisos)));
        return rol;
    }
}
