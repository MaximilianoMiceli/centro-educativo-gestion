package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.EstadoInstalacion;
import ar.edu.utn.centroeducativo.gestion.domain.Instalacion;
import ar.edu.utn.centroeducativo.gestion.domain.TipoInstalacion;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.InstalacionRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.InstalacionDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.SolicitudInstalacion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstalacionService {

    private static final int NOMBRE_MIN = 3;
    private static final int NOMBRE_MAX = 80;
    private static final int UBICACION_MAX = 120;
    private static final int DESCRIPCION_MAX = 500;

    private final InstalacionRepository instalacionRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<InstalacionDTO> listar() {
        return instalacionRepository.findAllByOrderByNombreAsc().stream().map(InstalacionService::toDTO).toList();
    }

    @Transactional
    public InstalacionDTO crear(SolicitudInstalacion solicitud) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarNombre(solicitud, null, errores);
        TipoInstalacion tipo = resolver(TipoInstalacion.class, solicitud.tipo(), "tipo", "el tipo", errores);
        EstadoInstalacion estado = resolver(EstadoInstalacion.class, solicitud.estado(), "estado", "el estado", errores);
        validarResto(solicitud, errores);
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en la instalación", errores);
        }

        Instalacion instalacion = new Instalacion();
        aplicar(instalacion, solicitud, tipo, estado);
        instalacionRepository.save(instalacion);

        auditoriaService.registrar("ALTA", "INSTALACION", instalacion.getId(),
                "Instalación creada: " + instalacion.getNombre() + " (" + tipo + ")");
        return toDTO(instalacion);
    }

    @Transactional
    public InstalacionDTO modificar(Long id, SolicitudInstalacion solicitud) {
        Instalacion instalacion = instalacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Instalación con ID " + id + " no encontrada"));

        Map<String, String> errores = new LinkedHashMap<>();
        validarNombre(solicitud, id, errores);
        TipoInstalacion tipo = resolver(TipoInstalacion.class, solicitud.tipo(), "tipo", "el tipo", errores);
        EstadoInstalacion estado = resolver(EstadoInstalacion.class, solicitud.estado(), "estado", "el estado", errores);
        validarResto(solicitud, errores);
        if (!errores.isEmpty()) {
            throw new ValidacionException("Hay datos inválidos en la instalación", errores);
        }

        List<String> cambios = new ArrayList<>();
        registrarCambio(cambios, "nombre", instalacion.getNombre(), solicitud.nombre());
        registrarCambio(cambios, "tipo", instalacion.getTipo(), tipo);
        registrarCambio(cambios, "ubicacion", instalacion.getUbicacion(), solicitud.ubicacion());
        registrarCambio(cambios, "capacidad", instalacion.getCapacidad(), solicitud.capacidad());
        registrarCambio(cambios, "estado", instalacion.getEstado(), estado);
        registrarCambio(cambios, "descripcion", instalacion.getDescripcion(), solicitud.descripcion());

        aplicar(instalacion, solicitud, tipo, estado);
        instalacionRepository.save(instalacion);

        if (!cambios.isEmpty()) {
            auditoriaService.registrar("MODIFICACION", "INSTALACION", id, String.join("; ", cambios));
        }
        return toDTO(instalacion);
    }

    private void validarNombre(SolicitudInstalacion solicitud, Long idActual, Map<String, String> errores) {
        String nombre = solicitud.nombre();
        if (nombre == null || nombre.length() < NOMBRE_MIN || nombre.length() > NOMBRE_MAX) {
            errores.put("nombre", "El nombre debe tener entre " + NOMBRE_MIN + " y " + NOMBRE_MAX + " caracteres");
        } else if (idActual == null
                ? instalacionRepository.existsByNombreIgnoreCase(nombre)
                : instalacionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, idActual)) {
            errores.put("nombre", "Ya existe una instalación con ese nombre");
        }
    }

    private static void validarResto(SolicitudInstalacion solicitud, Map<String, String> errores) {
        if (solicitud.capacidad() != null && solicitud.capacidad() < 1) {
            errores.put("capacidad", "La capacidad debe ser mayor a 0");
        }
        if (solicitud.ubicacion() != null && solicitud.ubicacion().length() > UBICACION_MAX) {
            errores.put("ubicacion", "La ubicación no puede superar los " + UBICACION_MAX + " caracteres");
        }
        if (solicitud.descripcion() != null && solicitud.descripcion().length() > DESCRIPCION_MAX) {
            errores.put("descripcion", "La descripción no puede superar los " + DESCRIPCION_MAX + " caracteres");
        }
    }

    private static <E extends Enum<E>> E resolver(Class<E> enumeracion, String valor, String campo,
            String etiqueta, Map<String, String> errores) {
        if (valor == null || valor.isBlank()) {
            errores.put(campo, "Seleccioná " + etiqueta);
            return null;
        }
        try {
            return Enum.valueOf(enumeracion, valor.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            errores.put(campo, "Valor inválido para " + etiqueta);
            return null;
        }
    }

    private static void aplicar(Instalacion instalacion, SolicitudInstalacion solicitud, TipoInstalacion tipo,
            EstadoInstalacion estado) {
        instalacion.setNombre(solicitud.nombre());
        instalacion.setTipo(tipo);
        instalacion.setUbicacion(solicitud.ubicacion());
        instalacion.setCapacidad(solicitud.capacidad());
        instalacion.setEstado(estado);
        instalacion.setDescripcion(solicitud.descripcion());
    }

    private static void registrarCambio(List<String> cambios, String campo, Object anterior, Object nuevo) {
        if (!Objects.equals(anterior, nuevo)) {
            cambios.add(campo + ": '" + anterior + "' -> '" + nuevo + "'");
        }
    }

    private static InstalacionDTO toDTO(Instalacion instalacion) {
        return new InstalacionDTO(
                instalacion.getId(),
                instalacion.getNombre(),
                instalacion.getTipo().name(),
                instalacion.getUbicacion(),
                instalacion.getCapacidad(),
                instalacion.getEstado().name(),
                instalacion.getDescripcion());
    }
}
