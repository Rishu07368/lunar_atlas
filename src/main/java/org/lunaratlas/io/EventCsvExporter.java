package org.lunaratlas.io;

import org.lunaratlas.astronomy.PhaseEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EventCsvExporter {
    public void export(List<PhaseEvent> events, Path destination) throws IOException {
        StringBuilder csv = new StringBuilder("event,instant_utc\n");
        for (PhaseEvent event : events) {
            csv.append(escape(event.phase().label())).append(',')
                    .append(escape(event.instant().toString())).append('\n');
        }
        Files.writeString(destination, csv, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
