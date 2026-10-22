package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.LogAuditoria;
import ar.edu.utn.centroeducativo.gestion.repository.LogAuditoriaRepository;
import ar.edu.utn.centroeducativo.gestion.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra la auditoría dentro de la transacción de la operación auditada:
 * si la operación se revierte, el registro también.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioActual usuarioActual;

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String accion, String entidad, Long entidadId, String detalle) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuarioId(usuarioActual.id().orElse(null));
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setDetalle(detalle);
        logAuditoriaRepository.save(log);
    }
}
