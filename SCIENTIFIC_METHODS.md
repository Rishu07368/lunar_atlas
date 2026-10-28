# Scientific methods and result classifications

Every displayed item is either **Calculated**, **Curated**, **Illustrative**, or **Unavailable**. The first release does not retrieve live observations.

## Lunar phase, age, and illumination

The phase calendar uses a truncated true-phase periodic series with a synodic-month mean and principal periodic corrections. It returns UTC instants for New Moon, First Quarter, Full Moon, and Last Quarter; event intervals are restricted to one year. Regression cases are compared with the U.S. Naval Observatory (USNO) primary-phase service, including April 2024 New Moon and Full Moon times, with a 30-minute test tolerance.

For a selected instant, phase age is elapsed time from the previous estimated New Moon divided by the estimated interval to the next New Moon. The phase label and illuminated fraction are derived from that cycle fraction. This does not model apparent limb orientation, libration, atmospheric visibility, or local crescent-sighting criteria. Results are approximations, not a replacement for USNO/JPL ephemerides.

## Position, altitude, azimuth, and rise/set

The UI calculates a simplified two-body lunar orbit using time-varying orbital elements, converts the geocentric direction to equatorial coordinates, calculates local sidereal time from the Julian date, and applies a first-order horizontal-parallax correction to altitude. It solves horizon crossings by sampling at five-minute intervals and linearly interpolating the crossing with an approximate semidiameter/refraction allowance.

The positional model omits major lunar perturbation terms, nutation, precise UT1/TT conversion, observer elevation, local terrain, weather/refraction variation, and a precise azimuth topocentric correction. A crossing may be absent on a particular date; this is shown as “no crossing.” Altitude/azimuth and rise/set can be materially wrong and are not suitable for navigation or precision observing.

Time input to the calculation is an `Instant`; phase-event results are UTC. A selected date is interpreted in the selected location's timezone for the observatory. The map is a simple equirectangular plot of the catalogued coordinates.

## Earth–Moon diagram and geographic display

Both visualizations are **Illustrative**. The orbit diagram uses the calculated phase-cycle fraction on a circular orbit and omits relative scale, orbital inclination/eccentricity, libration, and precise solar geometry. The location display shows a graticule and built-in city markers only. Neither implies an eclipse footprint, coast, tide gauge observation, or measured condition.

## Eclipses and tides

Local eclipse circumstances and paths are **Unavailable** because a verified eclipse ephemeris/path dataset is not bundled. Tide heights and station predictions are **Unavailable** because there is no validated local hydrodynamic model or authoritative station time series.

The science notes explain established mechanisms: lunar/solar tidal forcing, broad spring/neap geometry, and orbital alignment requirements for eclipses. Local tidal range depends on basin shape, bathymetry, coastline, resonance, and weather; phase alone is not a local prediction.

## Accuracy and reference cases

The automated tests use USNO's published 2024 primary phases as references; this is a narrow algorithm regression check, not a global accuracy certification. The displayed uncertainty wording remains conservative. No exact precision claim is made for lunar position or rise/set.
