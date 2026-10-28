# Testing

## Automated suite

Run on JDK 17+ and Maven 3.8+:

```powershell
mvn -B clean verify
```

JUnit 5 tests cover:

- New Moon and Full Moon phase labels/illumination for April 2024 reference instants.
- New Moon, First Quarter, Full Moon, and Last Quarter event timestamps against USNO's 2024 primary-phase table (30-minute tolerance).
- Inclusive UTC date filtering, event sorting, and invalid/overlong date ranges.
- Geographic coordinate validation, finite/bounded position values, and lunar distance sanity range.
- Different selected coordinates change local altitude/azimuth; Windows provider success, denial, unavailable, and malformed-response handling.
- Custom latitude/longitude and selected time zone persistence across application preference reload.
- Rise/set output date boundaries where crossings are returned.
- Bundled data loading and presence of source/context fields.
- Local notes save/reopen and oversized-data rejection.
- UTF-8 CSV schema and event/timestamp output.

Current test suite count: **17 tests**. The suite also verifies persisted preferences/bookmarks and the phase-filter service. No tests are intentionally skipped.

## Manual desktop checks

The UI is Swing and does not have a dedicated automated UI-driving library. Manually verify:

1. Launch the JAR or packaged launcher and confirm the main frame opens.
2. Change date and location; confirm phase, local time, position, rise/set, orbit graphic, and map selection update.
3. Use **Use My Current Location** with Windows Location services enabled; confirm the returned coordinates and choose the correct time zone. Verify that altitude/azimuth, rise/set, local clock, map marker, and saved location update. Then deny/disable Windows location or test on a device with no location source; confirm an explanatory message and working **Enter coordinates…** fallback.
4. Click city markers and verify the corresponding location becomes selected.
5. Calculate a valid phase range, filter phase types, and export/open the CSV.
6. Search research records and inspect source/scope text.
7. Save notes, close normally, relaunch, and verify notes/bookmarks/preferences remain.
8. Test a malformed or longer-than-one-year event range and verify an inline error instead of a crash.
9. Disconnect networking and launch; core data, manual locations, and calculations should remain available.

`scripts/smoke-test-windows.ps1` performs a packaged main-window and graceful-close smoke test on an interactive Windows desktop. A successful Maven build alone does not establish that the launcher was tested.

## Location feature verification record

- Automated verification: Windows provider response parsing (success, denied, unavailable, malformed), local calculation changes for different coordinates, and custom coordinate/time-zone persistence pass in the JUnit suite.
- Windows provider integration: local diagnostics showed `GeoCoordinateWatcher.TryStart` returns true while status initially remains `NoData` and `Position.Location.IsUnknown`. The provider previously checked once and prematurely reported no location. After adding a bounded wait, the provider returned a valid live fix on this Windows host within a 10-second request. The provider was then invoked from the extracted packaged JAR and returned `SUCCESS` (coordinate values deliberately withheld). The portable launcher also opened and closed normally with Java removed from `PATH`; the complete Swing confirmation/apply interaction was not automated.
- The packaged provider failure state appeared without replacing the existing built-in selection. Full manual form entry through submission was not confirmed in the packaged UI; the manual input path is covered by compilation, coordinate validation, and preference persistence tests.

## Accuracy limits

The automated reference tests only bound selected phase calculations. There are no precise reference tests for lunar rise/set, topocentric azimuth, eclipse paths, tide models, or cultural-calendar conversion; those are not validated features in this release.
