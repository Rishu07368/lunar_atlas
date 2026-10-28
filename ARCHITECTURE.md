# Architecture

## Runtime shape

Lunar Atlas is a single-process Java 17-compatible Swing desktop program. The application starts on the Swing event-dispatch thread, loads bundled TSV reference content, resolves per-user storage, and wires a small set of UI panels to typed services. There are no required network services, accounts, database servers, or runtime libraries beyond the Java runtime.

```text
Swing application / panels
  ├─ observatory ──> LunarCalculator ──> typed LunarState
  ├─ explicit location action ──> Windows GeoCoordinateWatcher ──> validated GeoLocation
  ├─ phase calendar ──> LunarCalculator.events ──> CSV exporter
  ├─ map / orbit ──> schematic painters + GeoLocation catalog
  ├─ research view ──> validated bundled TSV records
  └─ workspace ──> Preferences + atomic UTF-8 notes file
```

## Package boundaries

- `astronomy`: validated geographic values, phase/event values, low-precision orbit and horizon calculations.
- `research`: immutable curated record type and bundled-data parser.
- `storage`: persistent selected location/date, bookmarks, and locally saved notes.
- `io`: explicit UTF-8 phase-event CSV export.
- `app`: Swing lifecycle, panels, illustrative renderers, and the opt-in Windows location-provider adapter. The provider runs off the Swing event thread and is invoked only by user action.

The map uses only a longitude/latitude graticule and real coordinates for the built-in cities; it does not draw coastlines or event paths. The Earth–Moon diagram is intentionally not to scale. Both are labeled in the UI.

## State and persistence

Bundled research records are classpath resources and are not rewritten. User preferences, including the last selected custom coordinates and chosen time zone, use the JDK `Preferences` API. Notes are limited to 1 MB, saved under `%APPDATA%\LunarAtlas\investigation-notes.txt` on Windows, written to a sibling temporary file, and atomically replaced when the file system supports it. The product does not adopt SQLite because the current first release has no query/history workload that justifies an embedded database; database schema migration is therefore not applicable.

## Lifecycle and failure boundaries

Swing components are only accessed on the EDT. Device location is explicitly requested once through the Windows .NET Framework `GeoCoordinateWatcher` from a background worker; Windows location services and privacy settings govern access and may use sensor, Wi-Fi, or network sources. The provider returns coordinates and accuracy only, so the user confirms the time zone. Location denial, provider absence, and timeouts preserve manual location selection. Core calculations require no network.

The ordinary close action releases the process; user notes persist only after the explicit **Save notes** action. Unsaved note edits are not presented as saved.

## Decisions

- Swing over JavaFX: it is part of the JDK, keeps packaging simple, and is sufficient for the two-dimensional visualizations in this release.
- Java 17 source compatibility: this is the requested LTS baseline; local builds use JDK 25 with `--release 17`.
- No required runtime dependencies: lowers offline and packaging risk.
- No local database: saved preferences and one notes file are the only user state implemented.
- `jpackage` app-image ZIP: packages a private runtime for Windows. It is not an MSI or installer.

For the architectural reference review, see [REFERENCE_ANALYSIS.md](REFERENCE_ANALYSIS.md).
