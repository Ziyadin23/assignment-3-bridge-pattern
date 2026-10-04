package edu.aitu.bridge;

import java.io.IOException;

/** Implementor: drawing primitives shared by every rendering backend. */
public interface Renderer {
    void drawCircle(int centerX, int centerY, int radius) throws IOException;
    void drawSquare(int x, int y, int side) throws IOException;
}
