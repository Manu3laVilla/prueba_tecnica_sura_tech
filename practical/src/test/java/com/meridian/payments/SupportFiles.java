package com.meridian.payments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SupportFiles {

    private SupportFiles() {
    }

    public static byte[] referenceSample(String... parts) throws IOException {
        Path base = Path.of("..", "reference", "providers");
        Path file = base;
        for (String part : parts) {
            file = file.resolve(part);
        }
        return Files.readAllBytes(file);
    }
}