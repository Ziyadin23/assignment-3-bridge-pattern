package edu.aitu.bridge;

import java.io.IOException;
import java.util.Objects;

/** Abstraction: holds a replaceable Implementor through composition. */
public abstract class Shape {
    private Renderer renderer;

    protected Shape(Renderer renderer) {
        setRenderer(renderer);
    }

    public final void setRenderer(Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    }

    protected final Renderer renderer() {
        return renderer;
    }

    protected static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    public abstract void draw() throws IOException;
}
