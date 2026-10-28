package org.lunaratlas.astronomy;

import java.time.Instant;

public record LunarState(
        Instant instant,
        LunarPhase phase,
        double phaseAgeDays,
        double illuminatedFraction,
        double distanceKilometers,
        double rightAscensionDegrees,
        double declinationDegrees,
        double altitudeDegrees,
        double azimuthDegrees) {
}
