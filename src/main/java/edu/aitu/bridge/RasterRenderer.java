package edu.aitu.bridge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Concrete Implementor: paints geometry into a PNG pixel buffer. */
public final class RasterRenderer implements Renderer {
    private final RenderTarget target;

    public RasterRenderer(Path directory) {
        target = new RenderTarget(directory);
    }

    @Override
    public void drawCircle(int centerX, int centerY, int radius) throws IOException {
        writePng("circle", new Ellipse2D.Double((double) centerX - radius,
                (double) centerY - radius, 2.0 * radius, 2.0 * radius));
    }

    @Override
    public void drawSquare(int x, int y, int side) throws IOException {
        writePng("square", new Rectangle2D.Double(x, y, side, side));
    }

    private void writePng(String shapeName, java.awt.Shape geometry) throws IOException {
        BufferedImage image = new BufferedImage(RenderTarget.SIZE, RenderTarget.SIZE,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(0x2563eb));
            graphics.fill(geometry);
        } finally {
            graphics.dispose();
        }
        if (!ImageIO.write(image, "png", target.file(shapeName, "png").toFile())) {
            throw new IOException("No PNG writer is available");
        }
    }
}
