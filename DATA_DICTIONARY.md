# Data dictionary

## Calculated values

| Value | Type / unit | Meaning | Classification |
|---|---|---|---|
| `instant` | UTC `Instant` | Time used by the observatory calculation | Calculated input |
| `phase` | `LunarPhase` enum | One of eight descriptive phase sectors | Calculated approximation |
| `phaseAgeDays` | decimal days | Days since calculated previous New Moon | Calculated approximation |
| `illuminatedFraction` | 0–1 | Approximate illuminated fraction from phase-cycle geometry | Calculated approximation |
| `distanceKilometers` | km | Approximate geocentric distance from simplified orbit | Calculated approximation |
| `rightAscensionDegrees` | degrees, 0–360 | Approximate equatorial right ascension | Calculated approximation |
| `declinationDegrees` | degrees, -90–90 | Approximate equatorial declination | Calculated approximation |
| `altitudeDegrees` | degrees, -90–90 | Approximate topocentric altitude with first-order parallax | Calculated approximation |
| `azimuthDegrees` | degrees, 0–360 | Approximate azimuth, north through east | Calculated approximation |
| `PhaseEvent.instant` | UTC `Instant` | Estimated primary phase event time | Calculated approximation |

No angular quantity is presented as an observed measurement. The observatory displays its calculation scope and omissions.

## Geographic values

`GeoLocation` contains an ID, display name, latitude/longitude in decimal degrees (WGS84-style geographic convention), and an IANA `ZoneId`. Constructor validation restricts latitude to [-90, 90] and longitude to [-180, 180]. The seven bundled catalog coordinates are representative city centers. A user may instead select one locally saved custom location: coordinates may be entered manually or obtained once from Windows `GeoCoordinateWatcher`. Device-provided accuracy is shown when available; Windows does not return the time zone, which the user must confirm or choose. No IP geolocation is used.

## Curated TSV records

`src/main/resources/data/research-records.tsv` has one record per line and an eight-column header:

| Field | Meaning |
|---|---|
| `kind` | `cultural`, `historical`, or `science` classification |
| `region` | Specific geographic/historical scope used by the summary |
| `title` | Short record label |
| `description` | Summary with scope and uncertainty |
| `period` | Historical or temporal context, if known |
| `category` | Calendar, mechanism, observation, etc. |
| `source` | Bibliographic citation and/or URL |
| `confidence` | Source confidence, uncertainty, and generalization limit |

Data are curated, not live-published. Resource loading checks the header and field count; records with missing context or source are rejected.

## User data

- Preferences: selected location ID/date, optional custom location name/coordinates/time zone, and bookmarked location IDs through JDK Preferences. Custom coordinates and zone remain local.
- Notes: UTF-8 text file `investigation-notes.txt`, maximum 1,000,000 bytes, in the per-user LunarAtlas directory.
- Event CSV: UTF-8 `event,instant_utc`; exported only to a path chosen by the user.
