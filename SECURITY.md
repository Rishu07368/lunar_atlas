# Security and privacy

## Data and network behavior

- The core application is offline and makes no app-directed network requests by default. It requires no account, analytics, credentials, or API keys.
- Only after the user selects **Use My Current Location**, a bundled PowerShell script invokes the documented [.NET Framework `GeoCoordinateWatcher`](https://learn.microsoft.com/en-us/dotnet/api/system.device.location.geocoordinatewatcher?view=netframework-4.8.1) location service once. Windows may use enabled sensors, nearby Wi-Fi, or its location service/network according to the device's settings. Lunar Atlas does not send coordinates to an IP geolocation service or any third party.
- Windows Location services and privacy controls apply. If access is denied or no location source is available, the app offers Windows Location settings and manual coordinates. No continuous location tracking runs.
- Device coordinates and the confirmed time zone are saved locally as the selected custom location. Windows does not return a time zone with `GeoCoordinateWatcher`; the user must confirm or change the suggested system time zone. The app does not infer a time zone from coordinates.
- Selected location/date, the last custom coordinates/time zone, and bookmarks are stored with JDK Preferences.
- Investigation notes are saved locally as UTF-8 under `%APPDATA%\LunarAtlas` on Windows. The app does not upload or synchronize them.
- Bundled reference data are read-only resources. User data is separate from application installation files.

## Input and file handling

- Geographic coordinates are range-checked by the `GeoLocation` constructor; the shipped location list is fixed.
- Event date ranges are parsed as ISO dates and limited to one year.
- Note files are limited to 1 MB. Writes use a temporary file within the known user-data directory followed by atomic replacement when supported.
- CSV output is written only to a file the user explicitly selects. Imported files are not supported or executed.

## Dependencies and signing

The application runtime uses only JDK modules. Maven's JUnit 5 dependency is test-scoped. Dependency versions are pinned in `pom.xml`. The release is not signed unless a release maintainer separately signs and verifies it; this repository contains no certificates or signing secrets.

## Reporting issues

Do not include personal notes or private local data in bug reports. Report reproducible failures with the app version, operating system, and steps to reproduce.
