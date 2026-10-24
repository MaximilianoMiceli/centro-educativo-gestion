package ar.edu.utn.centroeducativo.gestion.reporte;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;

/**
 * Logo del centro dibujado en código con la misma geometría del SVG del sitio institucional (viewBox 0 0 48 48).
 */
public final class LogoInstitucional {

    private static final float VIEWBOX = 48f;
    private static final Color AZUL = new Color(0x1a5fa8);
    private static final Color VERDE = new Color(0x4caf82);
    private static final Color NARANJA = new Color(0xf4894a);

    private LogoInstitucional() {
    }

    public static byte[] png(int tamanio) {
        BufferedImage imagen = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.scale(tamanio / VIEWBOX, tamanio / VIEWBOX);
            dibujar(g);
        } finally {
            g.dispose();
        }
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void dibujar(Graphics2D g) {
        g.setColor(conAlfa(AZUL, 0.2f));
        g.fill(new RoundRectangle2D.Double(6, 19, 16, 20, 6, 6));
        g.setColor(conAlfa(AZUL, 0.3f));
        g.fill(new RoundRectangle2D.Double(26, 19, 16, 20, 6, 6));
        g.setColor(AZUL);
        g.fill(new RoundRectangle2D.Double(22, 18, 4, 22, 4, 4));

        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(conAlfa(AZUL, 0.5f));
        g.draw(new Line2D.Double(9, 25, 19, 25));
        g.draw(new Line2D.Double(9, 29, 19, 29));

        Path2D.Double hojaIzquierda = new Path2D.Double();
        hojaIzquierda.moveTo(24, 18);
        hojaIzquierda.quadTo(20, 10, 13, 8);
        hojaIzquierda.quadTo(18, 14, 24, 18);
        hojaIzquierda.closePath();
        g.setColor(VERDE);
        g.fill(hojaIzquierda);

        Path2D.Double hojaDerecha = new Path2D.Double();
        hojaDerecha.moveTo(24, 18);
        hojaDerecha.quadTo(28, 9, 35, 7);
        hojaDerecha.quadTo(30, 13, 24, 18);
        hojaDerecha.closePath();
        g.setColor(conAlfa(VERDE, 0.85f));
        g.fill(hojaDerecha);

        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        g.setColor(VERDE);
        g.draw(new Line2D.Double(24, 18, 24, 7));

        g.setColor(NARANJA);
        g.fill(new Ellipse2D.Double(24 - 2.2, 6 - 2.2, 4.4, 4.4));

        g.setColor(conAlfa(AZUL, 0.6f));
        g.fill(new Ellipse2D.Double(13, 12, 4, 4));
        g.fill(new Ellipse2D.Double(31, 11, 4, 4));
    }

    private static Color conAlfa(Color color, float alfa) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(alfa * 255));
    }
}
