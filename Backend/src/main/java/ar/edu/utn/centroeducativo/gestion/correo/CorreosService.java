package ar.edu.utn.centroeducativo.gestion.correo;

import ar.edu.utn.centroeducativo.gestion.domain.EnvioCorreo;
import ar.edu.utn.centroeducativo.gestion.domain.EnvioCorreoAdjunto;
import ar.edu.utn.centroeducativo.gestion.domain.EstadoCorreo;
import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.domain.Padre;
import ar.edu.utn.centroeducativo.gestion.domain.TipoCorreo;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.repository.EnvioCorreoAdjuntoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.EnvioCorreoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.FacturaRepository;
import ar.edu.utn.centroeducativo.gestion.service.AuditoriaService;
import ar.edu.utn.centroeducativo.gestion.service.DocumentoPdf;
import ar.edu.utn.centroeducativo.gestion.service.FacturaMapper;
import ar.edu.utn.centroeducativo.gestion.service.FacturacionService;
import ar.edu.utn.centroeducativo.gestion.web.dto.CorreoDTO;
import ar.edu.utn.centroeducativo.gestion.web.dto.ResultadoCorreosDTO;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma y entrega los correos mensuales a los padres. Cada ejecución es repetible: un padre que ya recibió
 * el correo del mes no lo recibe de nuevo, salvo que el intento anterior haya terminado en error.
 */
@Service
@RequiredArgsConstructor
public class CorreosService {

    private final EntityManager entityManager;
    private final FacturaRepository facturaRepository;
    private final FacturacionService facturacionService;
    private final FacturaMapper mapper;
    private final DocumentoPdf documentoPdf;
    private final EnvioCorreoRepository envioRepository;
    private final EnvioCorreoAdjuntoRepository adjuntoRepository;
    private final CorreoSender sender;
    private final AuditoriaService auditoriaService;
    private final Clock reloj;

    @Value("${app.correo.modo:SIN_ENVIO}")
    private String modo;

    /**
     * Recordatorio: emite las facturas del mes siguiente (si faltan) y le manda a cada padre el total y su
     * composición, con la factura de cada hijo adjunta. Deuda: avisa lo que sigue sin pagarse de las facturas vencidas.
     */
    @Transactional
    public ResultadoCorreosDTO ejecutar(TipoCorreo tipo, LocalDate fecha) {
        String periodoEnvio = YearMonth.from(fecha).toString();
        int generadas = 0;
        List<Factura> facturas;
        if (tipo == TipoCorreo.RECORDATORIO) {
            String periodoFacturas = YearMonth.from(fecha).plusMonths(1).toString();
            generadas = facturacionService.generar(periodoFacturas).generadas();
            facturas = facturaRepository.findDelPeriodo(periodoFacturas);
        } else {
            facturas = facturaRepository.findVencidasHasta(fecha).stream()
                    .filter(f -> !FacturaMapper.PAGADA.equals(FacturaMapper.estado(f))).toList();
        }
        Map<Long, List<Factura>> porAlumno = facturas.stream().collect(Collectors.groupingBy(f -> f.getAlumno().getId()));
        Map<Long, EnvioCorreo> previos = new HashMap<>();
        envioRepository.findDelPeriodo(tipo, periodoEnvio).forEach(e -> previos.put(e.getPadre().getId(), e));

        int enviados = 0;
        int conError = 0;
        int yaEnviados = 0;
        int sinCorreo = 0;
        int sinDeuda = 0;
        List<Padre> padres = entityManager
                .createQuery("select distinct p from Padre p left join fetch p.alumnos", Padre.class).getResultList();
        padres.sort(Comparator.comparing(Padre::getApellido).thenComparing(Padre::getNombre));
        for (Padre padre : padres) {
            List<Factura> deLosHijos = padre.getAlumnos().stream()
                    .flatMap(a -> porAlumno.getOrDefault(a.getId(), List.of()).stream())
                    .sorted(Comparator.comparing((Factura f) -> f.getAlumno().getApellido())
                            .thenComparing(f -> f.getAlumno().getNombre()).thenComparing(Factura::getPeriodo))
                    .toList();
            if (deLosHijos.isEmpty()) {
                if (tipo == TipoCorreo.DEUDA && !padre.getAlumnos().isEmpty()) {
                    sinDeuda++;
                }
                continue;
            }
            EnvioCorreo previo = previos.get(padre.getId());
            if (previo != null && previo.getEstado() != EstadoCorreo.ERROR) {
                yaEnviados++;
                continue;
            }
            if (padre.getEmail() == null || padre.getEmail().isBlank()) {
                sinCorreo++;
                continue;
            }
            if (guardarYEnviar(tipo, periodoEnvio, padre, deLosHijos, previo) == EstadoCorreo.ERROR) {
                conError++;
            } else {
                enviados++;
            }
        }

        auditoriaService.registrar("ENVIO", "CORREO", null, tipo + " del " + fecha + " (" + modo + "): "
                + enviados + " correos, " + conError + " con error, " + yaEnviados + " ya enviados, " + sinCorreo
                + " sin correo");
        return new ResultadoCorreosDTO(tipo.name(), fecha.toString(), modo, generadas, enviados, conError, yaEnviados,
                sinCorreo, sinDeuda);
    }

    @Transactional(readOnly = true)
    public List<CorreoDTO> listar() {
        return envioRepository.findAllByOrderByIdDesc().stream().map(e -> aDTO(e, false)).toList();
    }

    @Transactional(readOnly = true)
    public CorreoDTO detalle(Long id) {
        return aDTO(envioConAdjuntos(id), true);
    }

    public record Descarga(String nombre, byte[] contenido) {
    }

    @Transactional(readOnly = true)
    public Descarga adjunto(Long envioId, Long adjuntoId) {
        EnvioCorreoAdjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .filter(a -> a.getEnvio().getId().equals(envioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("El correo no tiene ese adjunto"));
        return new Descarga(adjunto.getNombre(), adjunto.getContenido());
    }

    private EstadoCorreo guardarYEnviar(TipoCorreo tipo, String periodoEnvio, Padre padre, List<Factura> facturas,
            EnvioCorreo previo) {
        String periodoFacturas = facturas.get(0).getPeriodo();
        BigDecimal total = TextoCorreos.total(facturas, i -> tipo == TipoCorreo.RECORDATORIO || i.getPago() == null);
        String asunto = tipo == TipoCorreo.RECORDATORIO
                ? TextoCorreos.asuntoRecordatorio(periodoFacturas, total)
                : TextoCorreos.asuntoDeuda(total);
        String cuerpo = tipo == TipoCorreo.RECORDATORIO
                ? TextoCorreos.cuerpoRecordatorio(padre, periodoFacturas, facturas)
                : TextoCorreos.cuerpoDeuda(padre, facturas);
        List<CorreoSaliente.Adjunto> adjuntos = new ArrayList<>();
        if (tipo == TipoCorreo.RECORDATORIO) {
            facturas.forEach(f -> adjuntos.add(new CorreoSaliente.Adjunto(FacturaMapper.numero(f) + ".pdf",
                    documentoPdf.factura(mapper.aDTO(f)))));
        }

        ResultadoEnvio resultado;
        try {
            resultado = sender.enviar(new CorreoSaliente(padre.getEmail(), asunto, cuerpo, adjuntos));
        } catch (RuntimeException e) {
            resultado = new ResultadoEnvio(EstadoCorreo.ERROR, abreviar(e.getMessage()));
        }

        EnvioCorreo envio = previo != null ? previo : new EnvioCorreo();
        envio.setTipo(tipo);
        envio.setPadre(padre);
        envio.setPeriodo(periodoEnvio);
        envio.setDestinatario(padre.getEmail());
        envio.setAsunto(asunto);
        envio.setCuerpo(cuerpo);
        envio.setEstado(resultado.estado());
        envio.setDetalleError(resultado.detalleError());
        envio.setCreadoEn(LocalDateTime.now(reloj));
        envio.getAdjuntos().clear();
        for (CorreoSaliente.Adjunto a : adjuntos) {
            EnvioCorreoAdjunto guardado = new EnvioCorreoAdjunto();
            guardado.setEnvio(envio);
            guardado.setNombre(a.nombre());
            guardado.setContenido(a.contenido());
            envio.getAdjuntos().add(guardado);
        }
        envioRepository.save(envio);
        return resultado.estado();
    }

    private EnvioCorreo envioConAdjuntos(Long id) {
        return envioRepository.findWithAdjuntosById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Correo con ID " + id + " no encontrado"));
    }

    private static CorreoDTO aDTO(EnvioCorreo e, boolean completo) {
        List<CorreoDTO.Adjunto> adjuntos = completo
                ? e.getAdjuntos().stream().map(a -> new CorreoDTO.Adjunto(a.getId(), a.getNombre())).toList()
                : List.of();
        return new CorreoDTO(e.getId(), e.getTipo().name(), e.getPeriodo(), e.getPadre().getId(),
                e.getPadre().getApellido() + ", " + e.getPadre().getNombre(), e.getDestinatario(), e.getAsunto(),
                e.getEstado().name(), e.getDetalleError(), e.getCreadoEn(), completo ? e.getCuerpo() : null, adjuntos);
    }

    private static String abreviar(String mensaje) {
        if (mensaje == null) {
            return "Error al enviar el correo";
        }
        return mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje;
    }
}
