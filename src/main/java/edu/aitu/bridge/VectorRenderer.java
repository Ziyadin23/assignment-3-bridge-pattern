package edu.aitu.bridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Concrete Implementor: serializes geometry as scalable SVG elements. */
public final class VectorRenderer implements Renderer {
    private final RenderTarget target;

    public VectorRenderer(Path directory) {
        target = new RenderTarget(directory);
    }

    @Override
    public void drawCircle(int centerX, int centerY, int radius) throws IOException {
        writeSvg("circle", "<circle cx=\"%d\" cy=\"%d\" r=\"%d\"/>"
                .formatted(centerX, centerY, radius));
    }

    @Override
    public void drawSquare(int x, int y, int side) throws IOException {
        writeSvg("square", "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>"
                .formatted(x, y, side, side));
    }

    private void writeSvg(String shapeName, String element) throws IOException {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d">
                  <rect width="100%%" height="100%%" fill="white"/>
                  <g fill="#2563eb">%s</g>
                </svg>
                """.formatted(RenderTarget.SIZE, RenderTarget.SIZE,
                        RenderTarget.SIZE, RenderTarget.SIZE, element);
        Files.writeString(target.file(shapeName, "svg"), svg, StandardCharsets.UTF_8);
    }
}
