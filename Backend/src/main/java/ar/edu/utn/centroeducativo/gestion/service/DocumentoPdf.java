package ar.edu.utn.centroeducativo.gestion.service;

import ar.edu.utn.centroeducativo.gestion.reporte.InstitucionDatos;
import ar.edu.utn.centroeducativo.gestion.reporte.LogoInstitucional;
import ar.edu.utn.centroeducativo.gestion.web.dto.FacturaDTO;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

/** Arma los PDF de la factura mensual y del comprobante de pago. */
@Component
public class DocumentoPdf {

    private static final Color AZUL = new Color(0x094a98);
    private static final Color GRIS = new Color(0x4b5d72);
    private static final Color TEXTO = new Color(0x1a2636);
    private static final Color ZEBRA = new Color(0xf3f6fa);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, AZUL);
    private static final Font INSTITUCION = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.NORMAL, GRIS);
    private static final Font CABECERA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font TEXTO_NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO);
    private static final Font TEXTO_NEGRITA = new Font(Font.HELVETICA, 10, Font.BOLD, TEXTO);

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] factura(FacturaDTO factura) {
        return armar("Factura " + factura.numero(), documento -> {
            datos(documento, new String[][] {
                {"Alumno", factura.alumno()},
                {"Período", factura.periodo()},
                {"Emisión", FECHA.format(factura.emision())},
                {"Vencimiento", FECHA.format(factura.vencimiento())},
                {"Estado", factura.estado() + (factura.vencida() ? " (vencida)" : "")},
            });
            PdfPTable tabla = tabla(new float[] {5, 2, 2}, "Detalle", "Importe", "Estado");
            int fila = 0;
            for (FacturaDTO.Item item : factura.items()) {
                celda(tabla, item.descripcion(), Element.ALIGN_LEFT, fila);
                celda(tabla, moneda(item.monto()), Element.ALIGN_RIGHT, fila);
                celda(tabla, item.pagado() ? "Pagado" : "Pendiente", Element.ALIGN_LEFT, fila);
                fila++;
            }
            documento.add(tabla);
            totales(documento, new String[][] {
                {"Total", moneda(factura.total())},
                {"Pagado", moneda(factura.pagado())},
                {"Saldo", moneda(factura.saldo())},
            });
        });
    }

    public byte[] comprobante(FacturaDTO factura, FacturaDTO.PagoResumen pago, String registradoPor) {
        return armar("Comprobante de pago " + pago.numeroComprobante(), documento -> {
            datos(documento, new String[][] {
                {"Factura", factura.numero() + " (" + factura.periodo() + ")"},
                {"Alumno", factura.alumno()},
                {"Fecha de la transferencia", FECHA.format(pago.fechaPago())},
                {"Registrado el", FECHA_HORA.format(pago.registradoEn())},
                {"Registrado por", registradoPor},
                {"Archivo adjunto", pago.nombreArchivo()},
            });
            PdfPTable tabla = tabla(new float[] {6, 2}, "Ítems pagados", "Importe");
            int fila = 0;
            List<FacturaDTO.Item> pagados = factura.items().stream()
                    .filter(i -> pago.itemIds().contains(i.id())).toList();
            for (FacturaDTO.Item item : pagados) {
                celda(tabla, item.descripcion(), Element.ALIGN_LEFT, fila);
                celda(tabla, moneda(item.monto()), Element.ALIGN_RIGHT, fila);
                fila++;
            }
            documento.add(tabla);
            totales(documento, new String[][] {
                {"Total pagado", moneda(pago.monto())},
                {"Saldo de la factura", moneda(factura.saldo())},
            });
        });
    }

    private interface Cuerpo {
        void escribir(Document documento) throws DocumentException;
    }

    private byte[] armar(String titulo, Cuerpo cuerpo) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.addAuthor(InstitucionDatos.NOMBRE);
            documento.addTitle(titulo);
            documento.open();
            encabezado(documento, titulo);
            cuerpo.escribir(documento);
            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return salida.toByteArray();
    }

    private void encabezado(Document documento, String titulo) throws DocumentException, IOException {
        PdfPTable cabecera = new PdfPTable(new float[] {1, 8});
        cabecera.setWidthPercentage(100);
        Image logo = Image.getInstance(LogoInstitucional.png(160));
        logo.scaleToFit(44, 44);
        PdfPCell celdaLogo = new PdfPCell(logo, false);
        celdaLogo.setBorder(PdfPCell.NO_BORDER);
        cabecera.addCell(celdaLogo);
        PdfPCell celdaNombre = new PdfPCell();
        celdaNombre.setBorder(PdfPCell.NO_BORDER);
        celdaNombre.addElement(new Paragraph(InstitucionDatos.NOMBRE, INSTITUCION));
        celdaNombre.addElement(new Paragraph(InstitucionDatos.SUBTITULO + " · " + InstitucionDatos.LOCALIDAD, SUBTITULO));
        cabecera.addCell(celdaNombre);
        documento.add(cabecera);
        Paragraph parrafo = new Paragraph(titulo, TITULO);
        parrafo.setSpacingBefore(18);
        parrafo.setSpacingAfter(10);
        documento.add(parrafo);
    }

    private void datos(Document documento, String[][] filas) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {3, 7});
        tabla.setWidthPercentage(100);
        for (String[] fila : filas) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila[0], TEXTO_NEGRITA));
            etiqueta.setBorder(PdfPCell.NO_BORDER);
            etiqueta.setPaddingBottom(3);
            PdfPCell valor = new PdfPCell(new Phrase(fila[1], TEXTO_NORMAL));
            valor.setBorder(PdfPCell.NO_BORDER);
            valor.setPaddingBottom(3);
            tabla.addCell(etiqueta);
            tabla.addCell(valor);
        }
        tabla.setSpacingAfter(12);
        documento.add(tabla);
    }

    private PdfPTable tabla(float[] anchos, String... titulos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        for (String titulo : titulos) {
            PdfPCell celda = new PdfPCell(new Phrase(titulo, CABECERA));
            celda.setBackgroundColor(AZUL);
            celda.setPadding(5);
            tabla.addCell(celda);
        }
        return tabla;
    }

    private void celda(PdfPTable tabla, String texto, int alineacion, int fila) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, TEXTO_NORMAL));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        if (fila % 2 == 1) {
            celda.setBackgroundColor(ZEBRA);
        }
        tabla.addCell(celda);
    }

    private void totales(Document documento, String[][] filas) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {6, 2});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        for (String[] fila : filas) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila[0], TEXTO_NEGRITA));
            etiqueta.setBorder(PdfPCell.NO_BORDER);
            etiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell valor = new PdfPCell(new Phrase(fila[1], TEXTO_NEGRITA));
            valor.setBorder(PdfPCell.NO_BORDER);
            valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(etiqueta);
            tabla.addCell(valor);
        }
        documento.add(tabla);
    }

    private static String moneda(BigDecimal monto) {
        return MONEDA.format(monto);
    }
}
