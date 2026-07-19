package com.flowday.flowday.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Carga variables desde .env en la raíz del proyecto antes de arrancar Spring.
 * No sobrescribe variables ya definidas en el entorno del sistema.
 */
public final class DotenvLoader {

    private DotenvLoader() {}

    public static void load() {
        Path envPath = Paths.get(".env");
        if (!Files.isRegularFile(envPath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envPath);
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            /* .env opcional */
        }
    }
}
