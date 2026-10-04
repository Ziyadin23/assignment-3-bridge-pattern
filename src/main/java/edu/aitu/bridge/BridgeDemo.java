package edu.aitu.bridge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Client: constructs both hierarchies and switches the existing objects. */
public final class BridgeDemo {
    private BridgeDemo() { }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("rendered") : Path.of(args[0]);
        Renderer vector = new VectorRenderer(output.resolve("vector"));
        Renderer raster = new RasterRenderer(output.resolve("raster"));
        List<Shape> shapes = List.of(new Circle(vector, 160, 160, 90),
                new Square(vector, 70, 70, 180));

        System.out.println("1. Draw Circle and Square with VectorRenderer");
        drawAll(shapes);
        System.out.println("2. Switch the SAME shape objects to RasterRenderer");
        for (Shape shape : shapes) {
            shape.setRenderer(raster);
        }
        drawAll(shapes);
        System.out.println("3. Switch the SAME shape objects back to VectorRenderer");
        for (Shape shape : shapes) {
            shape.setRenderer(vector);
        }
        drawAll(shapes);
        System.out.println("Complete: circle.svg, square.svg, circle.png, square.png");
        System.out.println("Output directory: " + output.toAbsolutePath());
    }

    private static void drawAll(List<Shape> shapes) throws IOException {
        for (Shape shape : shapes) {
            shape.draw();
        }
    }
}
