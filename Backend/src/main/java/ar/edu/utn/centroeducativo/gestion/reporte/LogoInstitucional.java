package ar.edu.utn.centroeducativo.gestion.reporte;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;

/** Logo oficial del centro (PNG con fondo transparente), escalado a demanda para PDF y Excel. */
public final class LogoInstitucional {

    private static final String RECURSO = "/institucion/logo-institucional.png";

    private LogoInstitucional() {
    }

    public static byte[] png(int tamanio) {
        BufferedImage original = cargarOriginal();
        BufferedImage imagen = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(original, 0, 0, tamanio, tamanio, null);
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

    private static BufferedImage cargarOriginal() {
        try (InputStream entrada = LogoInstitucional.class.getResourceAsStream(RECURSO)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró el logo institucional en " + RECURSO);
            }
            return ImageIO.read(entrada);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
