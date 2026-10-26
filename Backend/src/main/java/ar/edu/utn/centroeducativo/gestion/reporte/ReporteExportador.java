package ar.edu.utn.centroeducativo.gestion.reporte;

import ar.edu.utn.centroeducativo.gestion.reporte.Reporte.Seccion;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ReporteExportador {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color AZUL = new Color(0x094a98);
    private static final Color GRIS = new Color(0x4b5d72);
    private static final Color TEXTO = new Color(0x1a2636);
    private static final Color ZEBRA = new Color(0xf3f6fa);
    private static final Color BORDE = new Color(0xdde4ee);

    private static final Font FUENTE_TITULO = new Font(Font.HELVETICA, 14, Font.BOLD, AZUL);
    private static final Font FUENTE_SECCION = new Font(Font.HELVETICA, 11, Font.BOLD, AZUL);
    private static final Font FUENTE_CABECERA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font FUENTE_TEXTO = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXTO);

    private static final int LADO_LOGO_PDF = 44;
    private static final int LADO_LOGO_EXCEL = 60;
    private static final int EMU_POR_PIXEL = 9525;
    private static final int ANCHO_MAXIMO_EXCEL = 55;
    private static final int FILA_INICIO_EXCEL = 4;

    public byte[] pdf(Reporte reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 120, 40);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            escritor.setPageEvent(new EncabezadoPagina(Image.getInstance(LogoInstitucional.png(160)), emision()));
            documento.addTitle(reporte.codigo() + " · " + reporte.titulo());
            documento.addAuthor(InstitucionDatos.NOMBRE);
            documento.open();

            documento.add(new Paragraph(reporte.codigo() + " · " + reporte.titulo(), FUENTE_TITULO));
            if (!reporte.filtros().isEmpty()) {
                documento.add(new Paragraph("Filtros: " + String.join(" · ", reporte.filtros()), FUENTE_TEXTO));
            }
            for (Seccion seccion : reporte.secciones()) {
                Paragraph titulo = new Paragraph(seccion.titulo(), FUENTE_SECCION);
                titulo.setSpacingBefore(12);
                titulo.setSpacingAfter(6);
                documento.add(titulo);
                documento.add(tabla(seccion));
            }
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", e);
        }
    }

    public byte[] excel(Reporte reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet(reporte.codigo());
            Estilos estilos = new Estilos(libro);
            insertarLogo(libro, hoja);

            int fila = FILA_INICIO_EXCEL;
            fila = escribirFila(hoja, fila, List.of(InstitucionDatos.NOMBRE), estilos.institucion);
            fila = escribirFila(hoja, fila, List.of(InstitucionDatos.SUBTITULO + " · " + InstitucionDatos.LOCALIDAD),
                    estilos.normal);
            fila = escribirFila(hoja, fila, List.of("Emitido: " + emision()), estilos.normal);
            fila++;
            fila = escribirFila(hoja, fila, List.of(reporte.codigo() + " · " + reporte.titulo()), estilos.titulo);
            if (!reporte.filtros().isEmpty()) {
                fila = escribirFila(hoja, fila, List.of("Filtros: " + String.join(" · ", reporte.filtros())),
                        estilos.normal);
            }
            for (Seccion seccion : reporte.secciones()) {
                fila++;
                fila = escribirFila(hoja, fila, List.of(seccion.titulo()), estilos.seccion);
                fila = escribirFila(hoja, fila, seccion.columnas(), estilos.cabecera);
                if (seccion.filas().isEmpty()) {
                    fila = escribirFila(hoja, fila, List.of("Sin resultados"), estilos.celda);
                }
                for (List<String> valores : seccion.filas()) {
                    fila = escribirFila(hoja, fila, valores, estilos.celda);
                }
            }
            ajustarAnchos(hoja);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static PdfPTable tabla(Seccion seccion) {
        PdfPTable tabla = new PdfPTable(seccion.columnas().size());
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String columna : seccion.columnas()) {
            tabla.addCell(celdaPdf(columna, FUENTE_CABECERA, AZUL));
        }
        if (seccion.filas().isEmpty()) {
            PdfPCell vacia = celdaPdf("Sin resultados", FUENTE_TEXTO, null);
            vacia.setColspan(seccion.columnas().size());
            tabla.addCell(vacia);
        }
        for (int i = 0; i < seccion.filas().size(); i++) {
            Color fondo = i % 2 == 0 ? null : ZEBRA;
            for (String valor : seccion.filas().get(i)) {
                tabla.addCell(celdaPdf(valor, FUENTE_TEXTO, fondo));
            }
        }
        return tabla;
    }

    private static PdfPCell celdaPdf(String texto, Font fuente, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(5);
        celda.setBorderColor(BORDE);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private static void insertarLogo(XSSFWorkbook libro, Sheet hoja) throws IOException {
        int indice = libro.addPicture(LogoInstitucional.png(180), Workbook.PICTURE_TYPE_PNG);
        Drawing<?> dibujo = hoja.createDrawingPatriarch();
        XSSFClientAnchor ancla = new XSSFClientAnchor(0, 0,
                LADO_LOGO_EXCEL * EMU_POR_PIXEL, LADO_LOGO_EXCEL * EMU_POR_PIXEL, 0, 0, 0, 0);
        dibujo.createPicture(ancla, indice);
    }

    private static int escribirFila(Sheet hoja, int numero, List<String> valores, CellStyle estilo) {
        Row fila = hoja.createRow(numero);
        for (int columna = 0; columna < valores.size(); columna++) {
            Cell celda = fila.createCell(columna);
            celda.setCellValue(valores.get(columna));
            celda.setCellStyle(estilo);
        }
        return numero + 1;
    }

    private static void ajustarAnchos(Sheet hoja) {
        List<Integer> anchos = new ArrayList<>();
        for (Row fila : hoja) {
            if (fila.getLastCellNum() <= 1) {
                continue;
            }
            for (Cell celda : fila) {
                int largo = celda.getStringCellValue().length();
                int columna = celda.getColumnIndex();
                while (anchos.size() <= columna) {
                    anchos.add(0);
                }
                anchos.set(columna, Math.max(anchos.get(columna), largo));
            }
        }
        for (int columna = 0; columna < anchos.size(); columna++) {
            int caracteres = Math.min(Math.max(anchos.get(columna) + 2, 12), ANCHO_MAXIMO_EXCEL);
            hoja.setColumnWidth(columna, caracteres * 256);
        }
    }

    private static String emision() {
        return LocalDateTime.now(ZONA).format(FECHA_HORA);
    }

    /** Encabezado de cada página: logo, nombre del centro, fecha de emisión y "Página N de M". */
    private static final class EncabezadoPagina extends PdfPageEventHelper {

        private final Image logo;
        private final String emision;
        private final BaseFont fuente;
        private final BaseFont fuenteNegrita;
        private PdfTemplate totalPaginas;
        private int paginas;

        EncabezadoPagina(Image logo, String emision) {
            this.logo = logo;
            this.emision = emision;
            try {
                this.fuente = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                this.fuenteNegrita = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED);
            } catch (DocumentException | IOException e) {
                throw new IllegalStateException("No se pudo cargar la fuente del encabezado", e);
            }
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document documento) {
            totalPaginas = writer.getDirectContent().createTemplate(16, 10);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            paginas++;
            PdfContentByte cb = writer.getDirectContent();
            float izquierda = documento.leftMargin();
            float derecha = documento.getPageSize().getWidth() - documento.rightMargin();
            float tope = documento.getPageSize().getHeight() - 24;

            logo.scaleToFit(LADO_LOGO_PDF, LADO_LOGO_PDF);
            logo.setAbsolutePosition(izquierda, tope - LADO_LOGO_PDF);
            cb.addImage(logo);

            texto(cb, fuenteNegrita, 13, AZUL, izquierda + LADO_LOGO_PDF + 10, tope - 14, Element.ALIGN_LEFT,
                    InstitucionDatos.NOMBRE);
            texto(cb, fuente, 8, GRIS, izquierda + LADO_LOGO_PDF + 10, tope - 28, Element.ALIGN_LEFT,
                    InstitucionDatos.SUBTITULO + " · " + InstitucionDatos.LOCALIDAD);
            texto(cb, fuente, 8, GRIS, derecha, tope - 14, Element.ALIGN_RIGHT, "Emitido: " + emision);

            String prefijo = "Página " + paginas + " de ";
            float xTotal = derecha - 16;
            float xPrefijo = xTotal - fuente.getWidthPoint(prefijo, 8);
            texto(cb, fuente, 8, GRIS, xPrefijo, tope - 28, Element.ALIGN_LEFT, prefijo);
            cb.addTemplate(totalPaginas, xTotal, tope - 28);

            cb.setLineWidth(0.5f);
            cb.setColorStroke(AZUL);
            cb.moveTo(izquierda, tope - LADO_LOGO_PDF - 6);
            cb.lineTo(derecha, tope - LADO_LOGO_PDF - 6);
            cb.stroke();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document documento) {
            totalPaginas.beginText();
            totalPaginas.setFontAndSize(fuente, 8);
            totalPaginas.setColorFill(GRIS);
            totalPaginas.setTextMatrix(0, 0);
            totalPaginas.showText(String.valueOf(paginas));
            totalPaginas.endText();
        }

        private static void texto(PdfContentByte cb, BaseFont base, float tamanio, Color color, float x, float y,
                int alineacion, String valor) {
            ColumnText.showTextAligned(cb, alineacion,
                    new Phrase(valor, new Font(base, tamanio, Font.NORMAL, color)), x, y, 0);
        }
    }

    /** Estilos de la planilla Excel, creados una sola vez por libro. */
    private static final class Estilos {

        final CellStyle institucion;
        final CellStyle titulo;
        final CellStyle seccion;
        final CellStyle cabecera;
        final CellStyle celda;
        final CellStyle normal;

        Estilos(XSSFWorkbook libro) {
            institucion = fuente(libro, estilo(libro), 12, true, AZUL);
            titulo = fuente(libro, estilo(libro), 13, true, AZUL);
            seccion = fuente(libro, estilo(libro), 11, true, AZUL);
            normal = estilo(libro);
            celda = bordes(estilo(libro));

            XSSFCellStyle cab = fuente(libro, estilo(libro), 10, true, Color.WHITE);
            cab.setFillForegroundColor(new XSSFColor(AZUL, null));
            cab.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cab.setWrapText(true);
            cabecera = bordes(cab);
        }

        private static XSSFCellStyle estilo(XSSFWorkbook libro) {
            return libro.createCellStyle();
        }

        private static XSSFCellStyle fuente(XSSFWorkbook libro, XSSFCellStyle estilo, int tamanio,
                boolean negrita, Color color) {
            XSSFFont fuente = libro.createFont();
            fuente.setFontHeightInPoints((short) tamanio);
            fuente.setBold(negrita);
            fuente.setColor(new XSSFColor(color, null));
            estilo.setFont(fuente);
            return estilo;
        }

        private static XSSFCellStyle bordes(XSSFCellStyle estilo) {
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
            return estilo;
        }
    }
}
