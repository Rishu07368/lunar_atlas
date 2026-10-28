# Limitations and unsupported features

Lunar Atlas 0.1.0 is a functional educational first release with intentionally limited scientific and cultural scope.

## Astronomy

- Phase calculations use a low-precision series, validated against a small sample of USNO 2024 primary phases (30-minute regression tolerance). This is not a general accuracy certification or a substitute for JPL/USNO ephemerides.
- Position values are approximate. The simplified orbital-element model omits important perturbations, nutation, precise time-scale conversion, and full topocentric corrections.
- Rise/set estimates use five-minute sampling, linear interpolation, simplified parallax, and an approximate horizon threshold. Terrain, elevation, atmosphere, refraction variation, and local obstructions are omitted.
- Timezone data are from the JDK. Date selection is a local calendar date for the selected location; phase calendar event instants are UTC.
- No eclipse prediction, local eclipse circumstance, eclipse-path geometry, event visibility mask, perigee/apogee calendar, lunar libration, or verified ephemeris feed exists.
- There is no solar position service or 3D representation.

## Geography and Earth systems

- The “map” is a schematic equirectangular graticule containing seven built-in city coordinates and the currently selected manual/device location; there are no coastlines, country borders, stations, measured observations, or event paths.
- There is no tide-height computation, station overlay, bathymetry model, coastal forecast, or live/archived gauge data.
- The science module is a small educational note set. Ecology examples and other uncertain lunar-effect claims are not included as causal claims. There are no predictions for earthquakes, weather, disasters, or human behavior.

## Culture, calendars, and history

- Cultural research contains six short documented samples, not broad global coverage. Several target regions and Indigenous/community-specific traditions are not represented; see [CULTURAL_RESEARCH_METHOD.md](CULTURAL_RESEARCH_METHOD.md).
- No religious calendar dates, observation-based crescent visibility, or regional calendar conversion is implemented.
- Historical entries are limited to two documented examples; no causal relationships with terrestrial events are asserted.
- Summaries are not substitutes for primary sources, local experts, or community consultation.

## Product and release

- The application has a portable Windows ZIP packaging script and CI workflow; an MSI/installer is not generated.
- PDF/report export, arbitrary place-name search/geocoding, IP-based location, and database migrations are not implemented. One custom coordinate location (device-detected or manually entered) is saved locally.
- Device location uses Windows .NET Framework `GeoCoordinateWatcher` through Windows PowerShell 5.1 and waits up to 20 seconds for a fix. It depends on Windows Location services, the desktop-app privacy switch, and a usable sensor/provider; laptops may not have GPS hardware. Wi-Fi/network positioning may be used by Windows. The app does not perform IP geolocation. Device location results do not include a time zone, so users must confirm or choose the correct zone; the suggested system time zone may not match the device's physical location. A live fix has not been verified on the development machine.
- The visualization has no play/pause animation or orbital time-step controls; changing the selected date updates the schematic.
- Device location availability and accuracy depend on the Windows host, location permissions, and installed sensors/providers; no successful live fix is guaranteed on every machine.
- JDK 25 is available in the development environment. The GitHub workflow targets JDK 17, but CI has not yet been run in this session.
