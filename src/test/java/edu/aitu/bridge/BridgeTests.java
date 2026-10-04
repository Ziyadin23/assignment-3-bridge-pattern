package edu.aitu.bridge;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/** Dependency-free behavioral checks; failures throw even without -ea. */
public final class BridgeTests {
    private BridgeTests() { }

    public static void main(String[] args) throws Exception {
        Path temporary = Files.createTempDirectory("bridge-tests-");
        try {
            delegatesGeometry();
            switchesExistingObjects();
            rejectsInvalidInputs();
            writesVectorGeometry(temporary);
            writesRasterPixels(temporary);
            propagatesOutputFailures(temporary);
            runsCompleteDemo(temporary);
            System.out.println("PASS: all 7 behavioral test groups");
        } finally {
            try (var paths = Files.walk(temporary)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void delegatesGeometry() throws IOException {
        RecordingRenderer recorder = new RecordingRenderer();
        new Circle(recorder, 100, 110, 25).draw();
        new Square(recorder, 40, 50, 80).draw();
        check(recorder.calls.equals(List.of("circle:100,110,25", "square:40,50,80")),
                "Shape geometry must be delegated unchanged");
    }

    private static void switchesExistingObjects() throws IOException {
        RecordingRenderer first = new RecordingRenderer();
        RecordingRenderer second = new RecordingRenderer();
        List<Shape> shapes = List.of(new Circle(first, 160, 160, 90),
                new Square(first, 70, 70, 180));
        for (Shape shape : shapes) {
            shape.draw();
            shape.setRenderer(second);
            shape.draw();
            shape.setRenderer(first);
            shape.draw();
        }
        check(second.calls.equals(List.of("circle:160,160,90", "square:70,70,180")),
                "Replacement renderer must receive the existing geometry");
        check(first.calls.equals(List.of("circle:160,160,90", "circle:160,160,90",
                        "square:70,70,180", "square:70,70,180")),
                "Switching back must restore delegation to the original renderer");
    }

    private static void rejectsInvalidInputs() throws Exception {
        RecordingRenderer recorder = new RecordingRenderer();
        expect(NullPointerException.class, () -> new Circle(null, 1, 1, 1));
        expect(IllegalArgumentException.class, () -> new Circle(recorder, 1, 1, 0));
        expect(IllegalArgumentException.class, () -> new Square(recorder, 1, 1, -1));
        expect(NullPointerException.class, () -> new VectorRenderer(null));
        expect(NullPointerException.class, () -> new RasterRenderer(null));
        Shape circle = new Circle(recorder, 1, 1, 1);
        expect(NullPointerException.class, () -> circle.setRenderer(null));
        circle.draw();
        check(recorder.calls.size() == 1, "A rejected replacement must retain the old renderer");
    }

    private static void writesVectorGeometry(Path temporary) throws Exception {
        Path directory = temporary.resolve("vector/nested");
        Renderer renderer = new VectorRenderer(directory);
        new Circle(renderer, 160, 160, 90).draw();
        new Square(renderer, 70, 70, 180).draw();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Element circle = (Element) factory.newDocumentBuilder()
                .parse(directory.resolve("circle.svg").toFile())
                .getElementsByTagNameNS("http://www.w3.org/2000/svg", "circle").item(0);
        check(circle != null && circle.getAttribute("cx").equals("160")
                && circle.getAttribute("cy").equals("160")
                && circle.getAttribute("r").equals("90"), "SVG circle must retain its geometry");
        var rectangles = factory.newDocumentBuilder().parse(directory.resolve("square.svg").toFile())
                .getElementsByTagNameNS("http://www.w3.org/2000/svg", "rect");
        Element square = (Element) rectangles.item(1); // First rectangle is the white background.
        check(square != null && square.getAttribute("x").equals("70")
                && square.getAttribute("y").equals("70")
                && square.getAttribute("width").equals("180")
                && square.getAttribute("height").equals("180"), "SVG square must retain its geometry");
    }

    private static void writesRasterPixels(Path temporary) throws IOException {
        Path directory = temporary.resolve("raster/nested");
        Renderer renderer = new RasterRenderer(directory);
        new Circle(renderer, 160, 160, 90).draw();
        new Square(renderer, 70, 70, 180).draw();
        for (String name : List.of("circle", "square")) {
            BufferedImage image = ImageIO.read(directory.resolve(name + ".png").toFile());
            check(image != null && image.getWidth() == 320 && image.getHeight() == 320,
                    "PNG must be readable and have the expected canvas size");
            check((image.getRGB(160, 160) & 0xffffff) == 0x2563eb, "Interior must be blue");
            check((image.getRGB(5, 5) & 0xffffff) == 0xffffff, "Background must be white");
            int expected = name.equals("circle") ? 0xffffff : 0x2563eb;
            check((image.getRGB(80, 80) & 0xffffff) == expected,
                    "Circle and square must have distinct pixel geometry");
        }
    }

    private static void propagatesOutputFailures(Path temporary) throws Exception {
        Path blockedDirectory = temporary.resolve("regular-file");
        Files.writeString(blockedDirectory, "This path is a file, not a directory.");
        for (Renderer renderer : List.of(new VectorRenderer(blockedDirectory),
                new RasterRenderer(blockedDirectory))) {
            expect(IOException.class, () -> new Circle(renderer, 160, 160, 90).draw());
        }
    }

    private static void runsCompleteDemo(Path temporary) throws IOException {
        Path directory = temporary.resolve("demo");
        BridgeDemo.main(new String[] { directory.toString() });
        for (String file : List.of("vector/circle.svg", "vector/square.svg",
                "raster/circle.png", "raster/square.png")) {
            check(Files.size(directory.resolve(file)) > 0, "Demo must produce " + file);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Exception> type, CheckedAction action) throws Exception {
        try {
            action.run();
        } catch (Exception error) {
            if (type.isInstance(error)) {
                return;
            }
            throw error;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    /** A third Implementor proves that the abstraction needs no backend edits. */
    private static final class RecordingRenderer implements Renderer {
        private final List<String> calls = new ArrayList<>();

        @Override
        public void drawCircle(int centerX, int centerY, int radius) {
            calls.add("circle:" + centerX + "," + centerY + "," + radius);
        }

        @Override
        public void drawSquare(int x, int y, int side) {
            calls.add("square:" + x + "," + y + "," + side);
        }
    }
}
