package edu.aitu.bridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Shared output configuration and directory preparation for both backends. */
final class RenderTarget {
    static final int SIZE = 320;
    private final Path directory;

    RenderTarget(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory must not be null");
    }

    Path file(String shapeName, String extension) throws IOException {
        Files.createDirectories(directory);
        return directory.resolve(shapeName + "." + extension);
    }
}
