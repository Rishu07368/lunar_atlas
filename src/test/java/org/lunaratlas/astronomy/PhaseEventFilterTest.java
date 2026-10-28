package org.lunaratlas.astronomy;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhaseEventFilterTest {
    @Test
    void filtersPrimaryPhasesAndRetainsAllInChronologicalOrder() {
        List<PhaseEvent> events = List.of(
                new PhaseEvent(Instant.parse("2024-04-08T18:21:00Z"), LunarPhase.NEW_MOON),
                new PhaseEvent(Instant.parse("2024-04-15T19:13:00Z"), LunarPhase.FIRST_QUARTER),
                new PhaseEvent(Instant.parse("2024-04-23T23:49:00Z"), LunarPhase.FULL_MOON));

        assertEquals(List.of(events.get(1)), PhaseEventFilter.FIRST_QUARTER.apply(events));
        assertEquals(events, PhaseEventFilter.ALL.apply(events));
    }
}
