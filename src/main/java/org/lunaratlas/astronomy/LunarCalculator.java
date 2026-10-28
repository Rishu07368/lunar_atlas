package org.lunaratlas.astronomy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LunarCalculator {
    private static final double J2000 = 2_451_545.0;
    private static final double SYNODIC_MONTH = 29.530588853;
    private static final double EARTH_EQUATORIAL_RADIUS_KM = 6_378.14;
    private static final double DEG = Math.PI / 180.0;

    public LunarState calculate(Instant instant, GeoLocation location) {
        double jd = julianDay(instant);
        double lunation = Math.floor((jd - 2_451_550.09765) / SYNODIC_MONTH);
        double lastNewMoon = truePhase(lunation);
        if (jd < lastNewMoon) {
            lunation -= 1;
            lastNewMoon = truePhase(lunation);
        }
        double nextNewMoon = truePhase(lunation + 1);
        double age = jd - lastNewMoon;
        double cycleFraction = age / (nextNewMoon - lastNewMoon);
        LunarPhase phase = phaseFor(cycleFraction);
        double illumination = (1 - Math.cos(2 * Math.PI * cycleFraction)) / 2;

        OrbitalPosition position = position(jd);
        double localSidereal = normalizeDegrees(greenwichSiderealDegrees(jd)
                + location.longitudeDegrees());
        double hourAngle = normalizeSignedDegrees(localSidereal - position.rightAscension);
        double latitude = location.latitudeDegrees() * DEG;
        double declination = position.declination * DEG;
        double hourAngleRadians = hourAngle * DEG;
        double altitude = Math.asin(Math.sin(latitude) * Math.sin(declination)
                + Math.cos(latitude) * Math.cos(declination) * Math.cos(hourAngleRadians));
        double azimuth = Math.atan2(-Math.sin(hourAngleRadians) * Math.cos(declination),
                Math.sin(declination) * Math.cos(latitude)
                        - Math.cos(declination) * Math.sin(latitude) * Math.cos(hourAngleRadians));
        double horizontalParallax = Math.asin(1.0 / position.distanceEarthRadii);
        double topocentricAltitude = altitude
                - Math.asin(Math.sin(horizontalParallax) * Math.cos(altitude));

        return new LunarState(instant, phase, age, illumination,
                position.distanceEarthRadii * EARTH_EQUATORIAL_RADIUS_KM,
                position.rightAscension, position.declination,
                topocentricAltitude / DEG, normalizeDegrees(azimuth / DEG));
    }

    public List<PhaseEvent> events(LocalDate from, LocalDate through, ZoneId zone) {
        if (through.isBefore(from)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
        if (through.toEpochDay() - from.toEpochDay() > 366) {
            throw new IllegalArgumentException("Choose a date range of one year or less.");
        }
        double startJd = julianDay(from.atStartOfDay(zone).toInstant());
        double endJd = julianDay(through.plusDays(1).atStartOfDay(zone).toInstant());
        long firstLunation = (long) Math.floor((startJd - 2_451_550.09765) / SYNODIC_MONTH) - 1;
        List<PhaseEvent> results = new ArrayList<>();
        LunarPhase[] phases = {
                LunarPhase.NEW_MOON, LunarPhase.FIRST_QUARTER,
                LunarPhase.FULL_MOON, LunarPhase.LAST_QUARTER
        };
        for (long lunation = firstLunation; ; lunation++) {
            boolean beyondRange = true;
            for (int i = 0; i < phases.length; i++) {
                double eventJd = truePhase(lunation + i * 0.25);
                if (eventJd <= endJd) {
                    beyondRange = false;
                }
                if (eventJd >= startJd && eventJd < endJd) {
                    results.add(new PhaseEvent(fromJulianDay(eventJd), phases[i]));
                }
            }
            if (beyondRange && lunation > firstLunation) {
                break;
            }
        }
        results.sort(Comparator.comparing(PhaseEvent::instant));
        return List.copyOf(results);
    }

    public HorizonTimes horizonTimes(LocalDate date, GeoLocation location) {
        Instant start = date.atStartOfDay(location.zoneId()).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(location.zoneId()).toInstant();
        Instant previousInstant = start;
        double previousAltitude = calculate(previousInstant, location).altitudeDegrees() + 0.82;
        Instant rise = null;
        Instant set = null;
        Duration step = Duration.ofMinutes(5);
        for (Instant current = start.plus(step); !current.isAfter(end); current = current.plus(step)) {
            double altitude = calculate(current, location).altitudeDegrees() + 0.82;
            if (previousAltitude <= 0 && altitude > 0 && rise == null) {
                rise = interpolateCrossing(previousInstant, current, previousAltitude, altitude);
            } else if (previousAltitude > 0 && altitude <= 0 && set == null) {
                set = interpolateCrossing(previousInstant, current, previousAltitude, altitude);
            }
            previousInstant = current;
            previousAltitude = altitude;
        }
        return new HorizonTimes(java.util.Optional.ofNullable(rise), java.util.Optional.ofNullable(set));
    }

    private static Instant interpolateCrossing(Instant start, Instant end,
                                               double startAltitude, double endAltitude) {
        double fraction = startAltitude / (startAltitude - endAltitude);
        return start.plusNanos(Math.round(Duration.between(start, end).toNanos() * fraction));
    }

    public static double julianDay(Instant instant) {
        return 2_440_587.5 + instant.getEpochSecond() / 86_400.0
                + instant.getNano() / 86_400_000_000_000.0;
    }

    private static Instant fromJulianDay(double jd) {
        double seconds = (jd - 2_440_587.5) * 86_400.0;
        long wholeSeconds = (long) Math.floor(seconds);
        long nanos = Math.round((seconds - wholeSeconds) * 1_000_000_000.0);
        if (nanos == 1_000_000_000L) {
            wholeSeconds++;
            nanos = 0;
        }
        return Instant.ofEpochSecond(wholeSeconds, nanos);
    }

    private static double truePhase(double k) {
        double t = k / 1_236.85;
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double jde = 2_451_550.09766 + 29.530588861 * k + 0.00015437 * t2
                - 0.000000150 * t3 + 0.00000000073 * t4;
        double e = 1 - 0.002516 * t - 0.0000074 * t2;
        double m = radians(2.5534 + 29.10535670 * k - 0.0000014 * t2 - 0.00000011 * t3);
        double mp = radians(201.5643 + 385.81693528 * k + 0.0107582 * t2
                + 0.00001238 * t3 - 0.000000058 * t4);
        double f = radians(160.7108 + 390.67050284 * k - 0.0016118 * t2
                - 0.00000227 * t3 + 0.000000011 * t4);
        double omega = radians(124.7746 - 1.56375588 * k + 0.0020672 * t2 + 0.00000215 * t3);
        double correction;
        if (isQuarter(k)) {
            correction = -0.62801 * Math.sin(mp) + 0.17172 * e * Math.sin(m)
                    - 0.01183 * e * Math.sin(mp + m) + 0.00862 * Math.sin(2 * mp)
                    + 0.00804 * Math.sin(2 * f) + 0.00454 * e * Math.sin(mp - m)
                    + 0.00204 * e * e * Math.sin(2 * m) - 0.00180 * Math.sin(mp - 2 * f)
                    - 0.00070 * Math.sin(mp + 2 * f) - 0.00040 * Math.sin(3 * mp)
                    - 0.00034 * e * Math.sin(2 * mp - m) + 0.00032 * e * Math.sin(m + 2 * f)
                    + 0.00032 * e * Math.sin(m - 2 * f) - 0.00028 * e * e * Math.sin(mp + 2 * m)
                    + 0.00027 * e * Math.sin(2 * mp + m) - 0.00017 * Math.sin(omega)
                    - 0.00005 * Math.sin(mp - m - 2 * f) + 0.00004 * Math.sin(2 * mp + 2 * f)
                    - 0.00004 * Math.sin(mp + m + 2 * f) + 0.00004 * Math.sin(mp - 2 * m)
                    + 0.00003 * Math.sin(mp + m - 2 * f) + 0.00003 * Math.sin(2 * mp - 2 * f)
                    - 0.00003 * Math.sin(mp - m + 2 * f) + 0.00003 * Math.sin(3 * m)
                    + 0.00003 * Math.sin(2 * mp + m - 2 * f) - 0.00002 * Math.sin(2 * mp - m + 2 * f)
                    - 0.00002 * Math.sin(3 * mp + m);
            double w = 0.00306 - 0.00038 * e * Math.cos(m) + 0.00026 * Math.cos(mp)
                    - 0.00002 * Math.cos(mp - m) + 0.00002 * Math.cos(mp + m)
                    + 0.00002 * Math.cos(2 * f);
            correction += isFirstQuarter(k) ? w : -w;
        } else {
            correction = (isNewMoon(k) ? -0.40720 : -0.40614) * Math.sin(mp)
                    + 0.17241 * e * Math.sin(m) + 0.01608 * Math.sin(2 * mp)
                    + 0.01039 * Math.sin(2 * f) + 0.00739 * e * Math.sin(mp - m)
                    - 0.00514 * e * Math.sin(mp + m) + 0.00208 * e * e * Math.sin(2 * m)
                    - 0.00111 * Math.sin(mp - 2 * f) - 0.00057 * Math.sin(mp + 2 * f)
                    + 0.00056 * e * Math.sin(2 * mp + m) - 0.00042 * Math.sin(3 * mp)
                    + 0.00042 * e * Math.sin(m + 2 * f) + 0.00038 * e * Math.sin(m - 2 * f)
                    - 0.00024 * e * Math.sin(2 * mp - m) - 0.00017 * Math.sin(omega)
                    - 0.00007 * Math.sin(mp + 2 * m) + 0.00004 * Math.sin(2 * mp - 2 * f)
                    + 0.00004 * Math.sin(3 * m) + 0.00003 * Math.sin(mp + m - 2 * f)
                    + 0.00003 * Math.sin(2 * mp + 2 * f) - 0.00003 * Math.sin(mp + m + 2 * f)
                    + 0.00003 * Math.sin(mp - m + 2 * f) - 0.00002 * Math.sin(mp - m - 2 * f)
                    - 0.00002 * Math.sin(3 * mp + m) + 0.00002 * Math.sin(4 * mp);
        }
        return jde + correction;
    }

    private static boolean isNewMoon(double k) {
        return Math.abs(k - Math.rint(k)) < 1e-8;
    }

    private static boolean isQuarter(double k) {
        double fraction = k - Math.floor(k);
        return Math.abs(fraction - 0.25) < 1e-8 || Math.abs(fraction - 0.75) < 1e-8;
    }

    private static boolean isFirstQuarter(double k) {
        return Math.abs((k - Math.floor(k)) - 0.25) < 1e-8;
    }

    private static LunarPhase phaseFor(double fraction) {
        if (fraction < 0.03 || fraction >= 0.97) return LunarPhase.NEW_MOON;
        if (fraction < 0.22) return LunarPhase.WAXING_CRESCENT;
        if (fraction < 0.28) return LunarPhase.FIRST_QUARTER;
        if (fraction < 0.47) return LunarPhase.WAXING_GIBBOUS;
        if (fraction < 0.53) return LunarPhase.FULL_MOON;
        if (fraction < 0.72) return LunarPhase.WANING_GIBBOUS;
        if (fraction < 0.78) return LunarPhase.LAST_QUARTER;
        return LunarPhase.WANING_CRESCENT;
    }

    private static OrbitalPosition position(double jd) {
        double d = jd - J2000;
        double node = radians(125.1228 - 0.0529538083 * d);
        double inclination = radians(5.1454);
        double periapsis = radians(318.0634 + 0.1643573223 * d);
        double eccentricity = 0.054900;
        double meanAnomaly = radians(115.3654 + 13.0649929509 * d);
        double eccentricAnomaly = meanAnomaly;
        for (int i = 0; i < 8; i++) {
            eccentricAnomaly -= (eccentricAnomaly - eccentricity * Math.sin(eccentricAnomaly)
                    - meanAnomaly) / (1 - eccentricity * Math.cos(eccentricAnomaly));
        }
        double xv = 60.2666 * (Math.cos(eccentricAnomaly) - eccentricity);
        double yv = 60.2666 * Math.sqrt(1 - eccentricity * eccentricity)
                * Math.sin(eccentricAnomaly);
        double trueAnomaly = Math.atan2(yv, xv);
        double radius = Math.hypot(xv, yv);
        double x = radius * (Math.cos(node) * Math.cos(trueAnomaly + periapsis)
                - Math.sin(node) * Math.sin(trueAnomaly + periapsis) * Math.cos(inclination));
        double y = radius * (Math.sin(node) * Math.cos(trueAnomaly + periapsis)
                + Math.cos(node) * Math.sin(trueAnomaly + periapsis) * Math.cos(inclination));
        double z = radius * Math.sin(trueAnomaly + periapsis) * Math.sin(inclination);
        double eclipticLongitude = Math.atan2(y, x);
        double eclipticLatitude = Math.atan2(z, Math.hypot(x, y));
        double obliquity = radians(23.4393 - 3.563e-7 * d);
        double equatorialX = Math.cos(eclipticLongitude) * Math.cos(eclipticLatitude);
        double equatorialY = Math.sin(eclipticLongitude) * Math.cos(eclipticLatitude) * Math.cos(obliquity)
                - Math.sin(eclipticLatitude) * Math.sin(obliquity);
        double equatorialZ = Math.sin(eclipticLongitude) * Math.cos(eclipticLatitude) * Math.sin(obliquity)
                + Math.sin(eclipticLatitude) * Math.cos(obliquity);
        return new OrbitalPosition(normalizeDegrees(Math.atan2(equatorialY, equatorialX) / DEG),
                Math.atan2(equatorialZ, Math.hypot(equatorialX, equatorialY)) / DEG, radius);
    }

    private static double greenwichSiderealDegrees(double jd) {
        double t = (jd - J2000) / 36_525.0;
        return normalizeDegrees(280.46061837 + 360.98564736629 * (jd - J2000)
                + 0.000387933 * t * t - t * t * t / 38_710_000.0);
    }

    private static double normalizeDegrees(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    private static double normalizeSignedDegrees(double degrees) {
        double normalized = normalizeDegrees(degrees);
        return normalized > 180 ? normalized - 360 : normalized;
    }

    private static double radians(double degrees) {
        return degrees * DEG;
    }

    private record OrbitalPosition(double rightAscension, double declination,
                                   double distanceEarthRadii) {
    }
}
