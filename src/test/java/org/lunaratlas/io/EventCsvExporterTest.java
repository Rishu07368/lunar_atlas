package org.lunaratlas.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lunaratlas.astronomy.LunarPhase;
import org.lunaratlas.astronomy.PhaseEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCsvExporterTest {
    @TempDir
    Path directory;

    @Test
    void exportsUtf8CsvWithQuotedEventAndUtcTimestamp() throws IOException {
        Path destination = directory.resolve("events.csv");

        new EventCsvExporter().export(List.of(new PhaseEvent(
                Instant.parse("2024-04-08T18:21:00Z"), LunarPhase.NEW_MOON)), destination);

        String csv = Files.readString(destination);
        assertTrue(csv.startsWith("event,instant_utc\n"));
        assertTrue(csv.contains("\"New Moon\",\"2024-04-08T18:21:00Z\""));
    }
}
