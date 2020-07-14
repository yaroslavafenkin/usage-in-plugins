package org.jenkinsci.deprecatedusage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@FunctionalInterface
public interface Checksum {

    boolean matches(byte[] data);

    default boolean matches(Path file) throws IOException {
        if (Files.notExists(file) || !Files.isRegularFile(file)) {
            return false;
        }
        return matches(Files.readAllBytes(file));
    }
}
