# Data sources and provenance

Lunar Atlas is offline-first. It makes no required or optional network requests in this release. The following references were manually curated for the bundled records; source text, datasets, maps, and third-party assets are not downloaded or embedded.

## Astronomical calculation references

- U.S. Naval Observatory, Astronomical Applications Department, [Dates of Primary Phases of the Moon](https://aa.usno.navy.mil/data/MoonPhases). The published API was used for the April 2024 New Moon (2024-04-08 18:21 UTC), First Quarter (2024-04-15 19:13 UTC), Full Moon (2024-04-23 23:49 UTC), and subsequent test/reference comparisons.
- Meeus, Jean. *Astronomical Algorithms*, 2nd edition, Willmann-Bell, 1998. Mean phase and periodic-term method; the implementation is deliberately truncated and its limitations are stated in [SCIENTIFIC_METHODS.md](SCIENTIFIC_METHODS.md).
- Schlyter, Paul. [How to compute planetary positions](https://www.stjarnhimlen.se/comp/ppcomp.html). Simplified orbital-element approach informs the approximate position model; the Lunar Atlas implementation is not a full ephemeris.

## Curated cultural and historical records

The exact record-level citations and scope notes are included alongside each record in `src/main/resources/data/research-records.tsv`.

- Dershowitz, Nachum, and Edward M. Reingold. *Calendrical Calculations*, 4th edition, Cambridge University Press, 2018. General reference for named lunisolar calendar conventions.
- Encyclopaedia Britannica, [Islamic calendar](https://www.britannica.com/topic/Islamic-calendar) and [Metonic cycle](https://www.britannica.com/science/Metonic-cycle).
- Bricker, Victoria R., and Harvey M. Bricker. *Astronomy in the Maya Codices*. Memoirs of the American Philosophical Society, 2011.
- Te Ara — The Encyclopedia of New Zealand, [Māori lunar calendar — maramataka](https://teara.govt.nz/en/maramataka-the-lunar-calendar).
- Galileo Galilei, *Sidereus Nuncius* (Venice, 1610), primary historical publication.
- NASA, [Apollo 11 mission overview](https://www.nasa.gov/missions/apollo/apollo-11-mission-overview/).

## Science education references

- NOAA Ocean Service, [Tides and water levels educational tutorial](https://oceanservice.noaa.gov/education/tutorial_tides/).
- NASA, [Eclipse Web Site](https://eclipse.gsfc.nasa.gov/).
- U.S. Geological Survey, [Can the position of the Moon or the planets affect seismicity?](https://www.usgs.gov/faqs/can-position-moon-or-planets-affect-seismicity).

## Licensing and update policy

The bundled source records are short factual summaries and citations written for this project; they are not copied articles or datasets. No image, map tile, coastline, station measurement, or proprietary API data is bundled. Source availability and page URLs can change. New records require source review, careful community/context labeling, and updates to [CULTURAL_RESEARCH_METHOD.md](CULTURAL_RESEARCH_METHOD.md).
