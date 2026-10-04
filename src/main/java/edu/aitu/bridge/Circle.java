package edu.aitu.bridge;

import java.io.IOException;

/** Refined Abstraction: owns circle geometry, independent of output format. */
public final class Circle extends Shape {
    private final int centerX;
    private final int centerY;
    private final int radius;

    public Circle(Renderer renderer, int centerX, int centerY, int radius) {
        super(renderer);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = requirePositive(radius, "radius");
    }

    @Override
    public void draw() throws IOException {
        renderer().drawCircle(centerX, centerY, radius);
    }
}
