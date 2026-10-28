package org.lunaratlas.storage;

import org.junit.jupiter.api.Test;
import org.lunaratlas.astronomy.GeoLocation;
import org.lunaratlas.astronomy.LocationCatalog;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferenceStoreTest {
    @Test
    void locationDateAndBookmarksPersistAcrossStoreInstances() throws BackingStoreException {
        Preferences node = Preferences.userRoot().node(
                "org/lunaratlas/tests/" + UUID.randomUUID());
        try {
            PreferenceStore first = new PreferenceStore(node);
            first.setLocationId("delhi");
            first.setSelectedDate(LocalDate.parse("2024-04-08"));
            first.addBookmark("delhi");
            first.addBookmark("delhi");
            node.flush();

            PreferenceStore reopened = new PreferenceStore(node);
            assertEquals("delhi", reopened.locationId());
            assertEquals(LocalDate.parse("2024-04-08"), reopened.selectedDate());
            assertEquals(List.of("delhi"), reopened.bookmarks());
            GeoLocation location = LocationCatalog.byId(reopened.locationId());
            assertEquals("New Delhi, India", location.name());
        } finally {
            node.removeNode();
            Preferences.userRoot().flush();
        }
    }

    @Test
    void customLocationCoordinatesAndTimeZonePersistAcrossStoreInstances() throws BackingStoreException {
        Preferences node = Preferences.userRoot().node(
                "org/lunaratlas/tests/" + UUID.randomUUID());
        try {
            PreferenceStore first = new PreferenceStore(node);
            GeoLocation custom = new GeoLocation("custom", "Observed location",
                    -33.8688, 151.2093, ZoneId.of("Australia/Sydney"));
            first.setCustomLocation(custom);
            first.setLocationId(custom.id());
            node.flush();

            PreferenceStore reopened = new PreferenceStore(node);
            assertEquals("custom", reopened.locationId());
            assertEquals(custom, reopened.customLocation().orElseThrow());
        } finally {
            node.removeNode();
            Preferences.userRoot().flush();
        }
    }
}
