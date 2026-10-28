package org.lunaratlas.storage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.time.ZoneId;
import java.util.prefs.Preferences;
import org.lunaratlas.astronomy.GeoLocation;

public final class PreferenceStore {
    private final Preferences preferences;

    public PreferenceStore(Class<?> owner) {
        this(Preferences.userNodeForPackage(owner));
    }

    public PreferenceStore(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }

    public String locationId() {
        return preferences.get("locationId", "london");
    }

    public void setLocationId(String id) {
        preferences.put("locationId", id);
    }

    public Optional<GeoLocation> customLocation() {
        String name = preferences.get("customLocationName", "");
        String latitude = preferences.get("customLocationLatitude", "");
        String longitude = preferences.get("customLocationLongitude", "");
        String zone = preferences.get("customLocationZone", "");
        if (name.isBlank() || latitude.isBlank() || longitude.isBlank() || zone.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new GeoLocation("custom", name, Double.parseDouble(latitude),
                    Double.parseDouble(longitude), ZoneId.of(zone)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public void setCustomLocation(GeoLocation location) {
        if (!"custom".equals(location.id())) {
            throw new IllegalArgumentException("Only a custom location can be saved here.");
        }
        preferences.put("customLocationName", location.name());
        preferences.put("customLocationLatitude", Double.toString(location.latitudeDegrees()));
        preferences.put("customLocationLongitude", Double.toString(location.longitudeDegrees()));
        preferences.put("customLocationZone", location.zoneId().getId());
    }

    public LocalDate selectedDate() {
        return LocalDate.parse(preferences.get("selectedDate", LocalDate.now().toString()));
    }

    public void setSelectedDate(LocalDate date) {
        preferences.put("selectedDate", date.toString());
    }

    public List<String> bookmarks() {
        String value = preferences.get("bookmarks", "");
        if (value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).filter(id -> !id.isBlank()).distinct().toList();
    }

    public void addBookmark(String id) {
        List<String> updated = new java.util.ArrayList<>(bookmarks());
        if (!updated.contains(id)) {
            updated.add(id);
            preferences.put("bookmarks", String.join(",", updated));
        }
    }
}
