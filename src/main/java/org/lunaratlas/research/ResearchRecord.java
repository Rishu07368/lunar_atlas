package org.lunaratlas.research;

public record ResearchRecord(
        String kind,
        String region,
        String title,
        String description,
        String period,
        String category,
        String source,
        String confidence) {
    public ResearchRecord {
        if (kind.isBlank() || region.isBlank() || title.isBlank()
                || description.isBlank() || source.isBlank() || confidence.isBlank()) {
            throw new IllegalArgumentException("Research records must include their context and source.");
        }
    }
}
