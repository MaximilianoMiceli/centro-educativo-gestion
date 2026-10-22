package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.LogAuditoria;
import ar.edu.utn.centroeducativo.gestion.repository.LogAuditoriaRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para registrar auditoría de operaciones.
 * Captura el usuario actual, la acción, la entidad y detalles.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioActual usuarioActual;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String entidad, Long entidadId, String detalle) {
        try {
            LogAuditoria log = new LogAuditoria();
            log.setUsuarioId(usuarioActual.id().orElse(null));
            log.setAccion(accion);
            log.setEntidad(entidad);
            log.setEntidadId(entidadId);
            log.setDetalle(detalle);
            logAuditoriaRepository.save(log);
        } catch (Exception e) {
            // No fallar la operación si la auditoría falla
            // En producción, logear esta excepción
        }
    }
}
