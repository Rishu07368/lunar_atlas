package org.lunaratlas.research;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchCatalogTest {
    @Test
    void bundledRecordsHaveContextAndTraceableSources() throws IOException {
        List<ResearchRecord> records = ResearchCatalog.load();

        assertTrue(records.size() >= 10);
        assertTrue(records.stream().allMatch(record -> !record.region().isBlank()
                && !record.source().isBlank() && !record.confidence().isBlank()));
        assertTrue(records.stream().anyMatch(record -> record.kind().equals("cultural")
                && record.region().equals("Mesoamerica")));
        assertFalse(records.stream().anyMatch(record -> record.source().contains("example.com")));
    }
}
