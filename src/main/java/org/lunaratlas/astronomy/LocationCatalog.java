package org.lunaratlas.astronomy;

import java.time.ZoneId;
import java.util.List;

public final class LocationCatalog {
    private static final List<GeoLocation> LOCATIONS = List.of(
            new GeoLocation("london", "London, United Kingdom", 51.5072, -0.1276, ZoneId.of("Europe/London")),
            new GeoLocation("new-york", "New York, United States", 40.7128, -74.0060, ZoneId.of("America/New_York")),
            new GeoLocation("nairobi", "Nairobi, Kenya", -1.2921, 36.8219, ZoneId.of("Africa/Nairobi")),
            new GeoLocation("delhi", "New Delhi, India", 28.6139, 77.2090, ZoneId.of("Asia/Kolkata")),
            new GeoLocation("beijing", "Beijing, China", 39.9042, 116.4074, ZoneId.of("Asia/Shanghai")),
            new GeoLocation("sydney", "Sydney, Australia", -33.8688, 151.2093, ZoneId.of("Australia/Sydney")),
            new GeoLocation("anchorage", "Anchorage, United States", 61.2181, -149.9003, ZoneId.of("America/Anchorage"))
    );

    private LocationCatalog() {
    }

    public static List<GeoLocation> locations() {
        return LOCATIONS;
    }

    public static GeoLocation byId(String id) {
        return LOCATIONS.stream().filter(location -> location.id().equals(id))
                .findFirst().orElse(LOCATIONS.get(0));
    }
}
