package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.domain.Factura;
import ar.edu.utn.centroeducativo.gestion.domain.FacturaItem;
import ar.edu.utn.centroeducativo.gestion.domain.Padre;
import ar.edu.utn.centroeducativo.gestion.domain.Pago;
import ar.edu.utn.centroeducativo.gestion.domain.PagoArchivo;
import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import ar.edu.utn.centroeducativo.gestion.exception.NoAutorizadoException;
import ar.edu.utn.centroeducativo.gestion.exception.RecursoNoEncontradoException;
import ar.edu.utn.centroeducativo.gestion.exception.ValidacionException;
import ar.edu.utn.centroeducativo.gestion.repository.FacturaRepository;
import ar.edu.utn.centroeducativo.gestion.repository.PagoArchivoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.PagoRepository;
import ar.edu.utn.centroeducativo.gestion.repository.UsuarioRepository;
import ar.edu.utn.centroeducativo.gestion.web.dto.FacturaDTO;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Pagos que informan los padres: eligen ítems de una factura y adjuntan la transferencia. */
@Service
@RequiredArgsConstructor
public class PagoService {

    private static final long MAXIMO_BYTES = 5L * 1024 * 1024;
    private static final int NOMBRE_MAXIMO = 120;

    private final AccesoFamilia acceso;
    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final PagoArchivoRepository pagoArchivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final FacturaMapper mapper;
    private final DocumentoPdf documentoPdf;
    private final Clock reloj;

    public record Archivo(String tipoContenido, String nombre, byte[] contenido) {
    }

    @Transactional
    public FacturaDTO.PagoResumen registrar(Long facturaId, List<Long> itemIds, LocalDate fechaPago,
            MultipartFile archivo) {
        Padre padre = acceso.padre();
        Factura factura = facturaRepository.bloquearPorId(facturaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura con ID " + facturaId + " no encontrada"));
        boolean esHijo = padre.getAlumnos().stream().anyMatch(a -> a.getId().equals(factura.getAlumno().getId()));
        if (!esHijo) {
            throw new NoAutorizadoException("La factura no corresponde a un hijo vinculado a tu cuenta");
        }

        Map<String, String> errores = new LinkedHashMap<>();
        List<FacturaItem> elegidos = elegirItems(factura, itemIds, errores);
        validarFecha(fechaPago, errores);
        byte[] contenido = leerArchivo(archivo, errores);
        String tipo = contenido == null ? null : tipoContenido(contenido);
        if (contenido != null && tipo == null) {
            errores.put("archivo", "Solo se admiten archivos PDF, PNG o JPG");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo registrar el pago", errores);
        }

        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(elegidos.stream().map(FacturaItem::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));
        pago.setFechaPago(fechaPago);
        pago.setRegistradoEn(LocalDateTime.now(reloj));
        pago.setRegistradoPor(acceso.usuario().getId());
        pago.setNombreArchivo(nombreSeguro(archivo.getOriginalFilename()));
        pago.setTipoContenido(tipo);
        pagoRepository.save(pago);
        pago.setNumeroComprobante("C-" + fechaPago.getYear() + "-" + String.format("%06d", pago.getId()));
        for (FacturaItem item : elegidos) {
            item.setPago(pago);
            pago.getItems().add(item);
        }
        factura.getPagos().add(pago);

        PagoArchivo guardado = new PagoArchivo();
        guardado.setPagoId(pago.getId());
        guardado.setContenido(contenido);
        pagoArchivoRepository.save(guardado);

        auditoriaService.registrar("ALTA", "PAGO", pago.getId(),
                "Factura " + FacturaMapper.numero(factura) + ": pago de " + pago.getMonto()
                        + " (" + elegidos.size() + " ítems)");
        return resumen(pago);
    }

    /** Comprobante de pago que emite el sistema, en PDF. */
    @Transactional(readOnly = true)
    public byte[] comprobantePdf(Long pagoId) {
        Pago pago = pagoVisible(pagoId);
        String registradoPor = pago.getRegistradoPor() == null ? "-"
                : usuarioRepository.findById(pago.getRegistradoPor()).map(Usuario::getUsername).orElse("-");
        return documentoPdf.comprobante(mapper.aDTO(pago.getFactura()), resumen(pago), registradoPor);
    }

    /** Archivo de la transferencia que adjuntó el padre. */
    @Transactional(readOnly = true)
    public Archivo archivo(Long pagoId) {
        Pago pago = pagoVisible(pagoId);
        PagoArchivo guardado = pagoArchivoRepository.findById(pagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El pago no tiene archivo adjunto"));
        return new Archivo(pago.getTipoContenido(), pago.getNombreArchivo(), guardado.getContenido());
    }

    private Pago pagoVisible(Long pagoId) {
        Pago pago = pagoRepository.findWithDetalleById(pagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago con ID " + pagoId + " no encontrado"));
        acceso.alumnoVisible(pago.getFactura().getAlumno().getId());
        return pago;
    }

    private static List<FacturaItem> elegirItems(Factura factura, List<Long> itemIds, Map<String, String> errores) {
        if (itemIds == null || itemIds.isEmpty()) {
            errores.put("items", "Elegí al menos un ítem para pagar");
            return List.of();
        }
        Set<Long> ids = new LinkedHashSet<>(itemIds);
        List<FacturaItem> elegidos = factura.getItems().stream().filter(i -> ids.contains(i.getId())).toList();
        if (elegidos.size() != ids.size()) {
            errores.put("items", "Hay ítems que no pertenecen a la factura");
        } else if (elegidos.stream().anyMatch(i -> i.getPago() != null)) {
            errores.put("items", "Hay ítems que ya fueron pagados");
        }
        return elegidos;
    }

    private void validarFecha(LocalDate fechaPago, Map<String, String> errores) {
        if (fechaPago == null) {
            errores.put("fechaPago", "Indicá la fecha de la transferencia");
        } else if (fechaPago.isAfter(LocalDate.now(reloj))) {
            errores.put("fechaPago", "La fecha de la transferencia no puede ser futura");
        }
    }

    private static byte[] leerArchivo(MultipartFile archivo, Map<String, String> errores) {
        if (archivo == null || archivo.isEmpty()) {
            errores.put("archivo", "Adjuntá el comprobante de la transferencia");
            return null;
        }
        if (archivo.getSize() > MAXIMO_BYTES) {
            errores.put("archivo", "El archivo no puede superar los 5 MB");
            return null;
        }
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            errores.put("archivo", "No se pudo leer el archivo");
            return null;
        }
    }

    /** El tipo se deduce del contenido real y no del nombre ni del encabezado que manda el cliente. */
    static String tipoContenido(byte[] b) {
        if (b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') {
            return "application/pdf";
        }
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        return null;
    }

    static String nombreSeguro(String original) {
        String nombre = original == null || original.isBlank() ? "comprobante" : original;
        nombre = nombre.replaceAll("[\\\\/]", "_").replaceAll("[^\\p{L}\\p{N} ._-]", "_").strip();
        if (nombre.isEmpty()) {
            nombre = "comprobante";
        }
        return nombre.length() > NOMBRE_MAXIMO ? nombre.substring(0, NOMBRE_MAXIMO) : nombre;
    }

    private static FacturaDTO.PagoResumen resumen(Pago pago) {
        return new FacturaDTO.PagoResumen(pago.getId(), pago.getNumeroComprobante(), pago.getMonto(),
                pago.getFechaPago(), pago.getRegistradoEn(), pago.getNombreArchivo(),
                pago.getItems().stream().map(FacturaItem::getId).toList());
    }
}
