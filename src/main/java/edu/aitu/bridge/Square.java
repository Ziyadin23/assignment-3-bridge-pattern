package edu.aitu.bridge;

import java.io.IOException;

/** Refined Abstraction: owns square geometry, independent of output format. */
public final class Square extends Shape {
    private final int x;
    private final int y;
    private final int side;

    public Square(Renderer renderer, int x, int y, int side) {
        super(renderer);
        this.x = x;
        this.y = y;
        this.side = requirePositive(side, "side");
    }

    @Override
    public void draw() throws IOException {
        renderer().drawSquare(x, y, side);
    }
}
