package org.lunaratlas.research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ResearchCatalog {
    private static final String RESOURCE = "/data/research-records.tsv";

    private ResearchCatalog() {
    }

    public static List<ResearchRecord> load() throws IOException {
        InputStream input = ResearchCatalog.class.getResourceAsStream(RESOURCE);
        if (input == null) {
            throw new IOException("Bundled research data is missing: " + RESOURCE);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (!"kind\tregion\ttitle\tdescription\tperiod\tcategory\tsource\tconfidence".equals(header)) {
                throw new IOException("Bundled research data has an unexpected header.");
            }
            List<ResearchRecord> records = reader.lines().map(ResearchCatalog::parseLine).toList();
            if (records.isEmpty()) {
                throw new IOException("Bundled research data contains no records.");
            }
            return records;
        } catch (IllegalArgumentException ex) {
            throw new IOException("Bundled research data is invalid: " + ex.getMessage(), ex);
        }
    }

    private static ResearchRecord parseLine(String line) {
        String[] fields = line.split("\t", -1);
        if (fields.length != 8) {
            throw new IllegalArgumentException("Each record must have eight tab-separated fields.");
        }
        return new ResearchRecord(fields[0], fields[1], fields[2], fields[3],
                fields[4], fields[5], fields[6], fields[7]);
    }
}
