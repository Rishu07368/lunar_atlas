package org.lunaratlas.astronomy;

import java.time.Instant;

public record PhaseEvent(Instant instant, LunarPhase phase) {
}
