package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Docente;
import ar.edu.utn.centroeducativo.gestion.exception.EdicionBloqueadaException;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.DocenteRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ControlEdicionDocente {

    static final short MAX_INTENTOS = 3;
    static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final DocenteRepository docenteRepository;

    void verificarNoBloqueado(Docente docente) {
        LocalDateTime hasta = docente.getEdicionBloqueadaHasta();
        if (hasta != null && hasta.isAfter(LocalDateTime.now())) {
            throw new EdicionBloqueadaException(
                    "Edición bloqueada por demasiados intentos inválidos. Intente nuevamente después de las "
                            + hasta.toLocalTime().withSecond(0).withNano(0),
                    hasta);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoFallido(Long docenteId) {
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Docente con ID " + docenteId + " no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        if (docente.getEdicionBloqueadaHasta() != null && !docente.getEdicionBloqueadaHasta().isAfter(ahora)) {
            docente.setEdicionBloqueadaHasta(null);
            docente.setIntentosEdicionFallidos((short) 0);
        }

        short intentos = (short) (docente.getIntentosEdicionFallidos() + 1);
        if (intentos >= MAX_INTENTOS) {
            docente.setEdicionBloqueadaHasta(ahora.plus(DURACION_BLOQUEO));
            intentos = 0;
        }
        docente.setIntentosEdicionFallidos(intentos);
        docenteRepository.save(docente);
    }
}
