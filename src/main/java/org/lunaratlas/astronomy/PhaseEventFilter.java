package org.lunaratlas.astronomy;

import java.util.List;

public enum PhaseEventFilter {
    ALL("All phases", null),
    NEW_MOON("New Moon", LunarPhase.NEW_MOON),
    FIRST_QUARTER("First Quarter", LunarPhase.FIRST_QUARTER),
    FULL_MOON("Full Moon", LunarPhase.FULL_MOON),
    LAST_QUARTER("Last Quarter", LunarPhase.LAST_QUARTER);

    private final String label;
    private final LunarPhase phase;

    PhaseEventFilter(String label, LunarPhase phase) {
        this.label = label;
        this.phase = phase;
    }

    public List<PhaseEvent> apply(List<PhaseEvent> events) {
        return events.stream().filter(event -> phase == null || event.phase() == phase).toList();
    }

    @Override
    public String toString() {
        return label;
    }
}
