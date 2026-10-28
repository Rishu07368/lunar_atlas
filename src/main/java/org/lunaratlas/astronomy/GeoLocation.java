package org.lunaratlas.astronomy;

import java.time.ZoneId;
import java.util.Objects;

public record GeoLocation(String id, String name, double latitudeDegrees,
                          double longitudeDegrees, ZoneId zoneId) {
    public GeoLocation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(zoneId, "zoneId");
        if (id.isBlank() || name.isBlank()
                || !Double.isFinite(latitudeDegrees) || latitudeDegrees < -90 || latitudeDegrees > 90
                || !Double.isFinite(longitudeDegrees) || longitudeDegrees < -180 || longitudeDegrees > 180) {
            throw new IllegalArgumentException("Invalid location name or coordinates.");
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
