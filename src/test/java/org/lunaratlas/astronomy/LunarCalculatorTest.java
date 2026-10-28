package org.lunaratlas.astronomy;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LunarCalculatorTest {
    private static final LunarCalculator CALCULATOR = new LunarCalculator();
    private static final GeoLocation LONDON = LocationCatalog.byId("london");

    @Test
    void identifiesKnownNewAndFullMoonReferenceDates() {
        LunarState newMoon = CALCULATOR.calculate(Instant.parse("2024-04-08T18:21:00Z"), LONDON);
        LunarState fullMoon = CALCULATOR.calculate(Instant.parse("2024-04-23T23:49:00Z"), LONDON);

        assertEquals(LunarPhase.NEW_MOON, newMoon.phase());
        assertTrue(newMoon.illuminatedFraction() < 0.01);
        assertEquals(LunarPhase.FULL_MOON, fullMoon.phase());
        assertTrue(fullMoon.illuminatedFraction() > 0.99);
    }

    @Test
    void estimatesApril2024PrimaryPhasesWithinThirtyMinutesOfUsnoReferences() {
        List<PhaseEvent> events = CALCULATOR.events(
                LocalDate.parse("2024-04-08"), LocalDate.parse("2024-04-23"), ZoneOffset.UTC);

        PhaseEvent newMoon = events.stream()
                .filter(event -> event.phase() == LunarPhase.NEW_MOON)
                .findFirst().orElseThrow();
        PhaseEvent fullMoon = events.stream()
                .filter(event -> event.phase() == LunarPhase.FULL_MOON)
                .findFirst().orElseThrow();
        PhaseEvent firstQuarter = events.stream()
                .filter(event -> event.phase() == LunarPhase.FIRST_QUARTER)
                .findFirst().orElseThrow();
        PhaseEvent lastQuarter = CALCULATOR.events(
                LocalDate.parse("2024-05-01"), LocalDate.parse("2024-05-01"), ZoneOffset.UTC)
                .stream().filter(event -> event.phase() == LunarPhase.LAST_QUARTER)
                .findFirst().orElseThrow();
        assertReferenceWithinThirtyMinutes(newMoon, "2024-04-08T18:21:00Z");
        assertReferenceWithinThirtyMinutes(firstQuarter, "2024-04-15T19:13:00Z");
        assertReferenceWithinThirtyMinutes(fullMoon, "2024-04-23T23:49:00Z");
        assertReferenceWithinThirtyMinutes(lastQuarter, "2024-05-01T11:27:00Z");
    }

    private static void assertReferenceWithinThirtyMinutes(PhaseEvent event, String reference) {
        long differenceSeconds = Math.abs(event.instant().getEpochSecond()
                - Instant.parse(reference).getEpochSecond());
        assertTrue(differenceSeconds < 30 * 60,
                event.phase() + " estimate differs by " + differenceSeconds + " seconds");
    }

    @Test
    void phaseEventsAreSortedAndRespectInclusiveLocalDateBoundaries() {
        LocalDate date = LocalDate.parse("2024-04-08");
        List<PhaseEvent> events = CALCULATOR.events(date, date, ZoneOffset.UTC);

        assertFalse(events.isEmpty());
        assertTrue(events.stream().allMatch(event -> !event.instant().isBefore(date.atStartOfDay(ZoneOffset.UTC).toInstant())
                && event.instant().isBefore(date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())));
        assertTrue(events.stream().map(PhaseEvent::instant).toList().equals(
                events.stream().map(PhaseEvent::instant).sorted().toList()));
    }

    @Test
    void rejectsInvalidDateRanges() {
        assertThrows(IllegalArgumentException.class, () -> CALCULATOR.events(
                LocalDate.parse("2024-04-09"), LocalDate.parse("2024-04-08"), ZoneOffset.UTC));
        assertThrows(IllegalArgumentException.class, () -> CALCULATOR.events(
                LocalDate.parse("2024-01-01"), LocalDate.parse("2025-01-02"), ZoneOffset.UTC));
    }

    @Test
    void eventDateBoundariesFollowTheSuppliedTimeZone() {
        List<PhaseEvent> tokyoAprilEighth = CALCULATOR.events(
                LocalDate.parse("2024-04-08"), LocalDate.parse("2024-04-08"),
                java.time.ZoneId.of("Asia/Tokyo"));
        List<PhaseEvent> tokyoAprilNinth = CALCULATOR.events(
                LocalDate.parse("2024-04-09"), LocalDate.parse("2024-04-09"),
                java.time.ZoneId.of("Asia/Tokyo"));

        assertTrue(tokyoAprilEighth.stream().noneMatch(event -> event.phase() == LunarPhase.NEW_MOON));
        assertTrue(tokyoAprilNinth.stream().anyMatch(event -> event.phase() == LunarPhase.NEW_MOON));
    }

    @Test
    void coordinatesAndPositionOutputsAreValidatedAndFinite() {
        assertThrows(IllegalArgumentException.class, () -> new GeoLocation(
                "invalid", "Invalid", 91, 0, ZoneOffset.UTC));
        LunarState state = CALCULATOR.calculate(Instant.parse("2024-04-08T18:21:00Z"), LONDON);
        assertTrue(Double.isFinite(state.altitudeDegrees()));
        assertTrue(state.altitudeDegrees() >= -90 && state.altitudeDegrees() <= 90);
        assertTrue(state.azimuthDegrees() >= 0 && state.azimuthDegrees() < 360);
        assertTrue(state.distanceKilometers() > 300_000 && state.distanceKilometers() < 450_000);
    }

    @Test
    void selectedCoordinatesChangeLocalPositionCalculations() {
        Instant instant = Instant.parse("2024-04-08T18:21:00Z");
        LunarState london = CALCULATOR.calculate(instant, LocationCatalog.byId("london"));
        GeoLocation custom = new GeoLocation("custom", "Manual location",
                -33.8688, 151.2093, java.time.ZoneId.of("Australia/Sydney"));
        LunarState sydney = CALCULATOR.calculate(instant, custom);

        assertTrue(Math.abs(london.altitudeDegrees() - sydney.altitudeDegrees()) > 1);
        assertTrue(Math.abs(london.azimuthDegrees() - sydney.azimuthDegrees()) > 1);
    }

    @Test
    void riseAndSetReturnOnlyValidOptionalPassages() {
        HorizonTimes times = CALCULATOR.horizonTimes(
                LocalDate.parse("2024-04-08"), LocationCatalog.byId("new-york"));

        times.moonrise().ifPresent(time -> assertEquals(
                LocalDate.parse("2024-04-08"), time.atZone(LocationCatalog.byId("new-york").zoneId()).toLocalDate()));
        times.moonset().ifPresent(time -> assertEquals(
                LocalDate.parse("2024-04-08"), time.atZone(LocationCatalog.byId("new-york").zoneId()).toLocalDate()));
    }
}
