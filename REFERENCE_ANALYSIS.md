# BasinWatch reference analysis

**Repository reviewed:** [Rishu07368/basinwatch](https://github.com/Rishu07368/basinwatch), `main`  
**Review date:** 2026-10-28

This review uses the repository README, architecture and deployment guides, Maven configuration, test and packaging scripts, the application entry point, and the published package/source inventory. The GitHub contents API returned rate limits for some nested directory listings, so this is not a claim that every BasinWatch source file was exhaustively audited.

## Existing architecture and package structure

BasinWatch is a single-process, offline Java Swing desktop application. Its documented source structure separates:

- `com.basinwatch.app`: Swing startup, main frame, map panel, and history UI.
- `com.basinwatch.domain`: the `BasinModel` aggregate, zone/station/mission entities, event types, commands, snapshots, and immutable values.
- `com.basinwatch.engine`: lifecycle orchestration, sensor feed/intake, bounded event buffer, and response workers.
- `com.basinwatch.io`: paths, logs, typed archive, snapshots, and reports.
- `com.basinwatch.db`: SQLite connection management, initialization, repositories, and asynchronous database service.
- `com.basinwatch.learning`: optional in-app architecture explanations.

`BasinWatchApp` initializes Swing on the event-dispatch thread, resolves writable user data paths, attempts to restore a versioned previous session, and then wires the model, engine, and frame. The architecture guide assigns UI ownership to Swing's EDT and requires services to publish immutable snapshots back to it. The domain aggregate owns invariants, while the engine owns worker and persistence lifecycles.

## UI, domain, calculation, persistence, and reporting boundaries

The UI presents a schematic map, situation overview, response desk, event feed, diagnostics, architecture notes, and SQLite history. It issues explicit engine commands instead of directly owning simulation state.

Domain objects and the aggregate enforce transition, validation, and resource invariants. The engine is the orchestration layer; the source guide describes a piped sensor producer, validating intake, bounded producer-consumer buffer, and a small set of workers. Resource allocation is atomic, short-lived, and isolated from I/O and UI callbacks. These mechanics are tailored to BasinWatch's concurrent operations simulation, not lunar computation.

Persistence is deliberately redundant by purpose: UTF-8 operational logs and reports, a typed binary sensor archive, validated/versioned session snapshots replaced atomically when possible, and optional SQLite history. SQLite uses JDBC repositories and a bounded single-thread database service; database failure is reported and does not silently replace the file-based records. Report generation is separate from the UI.

## Build and test practices

The Maven project pins plugin and SQLite JDBC versions, targets Java 17 using `maven.compiler.release`, builds an executable/shaded JAR, and has no GUI framework dependency. `scripts/test.ps1` compiles tests, resolves their runtime classpath through Maven, and runs a Java assertion-based `InvariantSuite` with assertions enabled. The documented suite includes concurrency/resource conservation, buffer lifecycle, domain operations, snapshot corruption/version checks, JDBC transactions, unavailable-database behavior, and orderly engine shutdown. This is a custom test runner rather than a conventional JUnit suite.

The package script runs tests and Maven packaging before invoking `jpackage`; it checks expected outputs and creates a portable ZIP. This establishes a useful fail-fast release sequence.

## Windows packaging and limitations

`scripts/package-windows.ps1` creates a Windows `jpackage --type app-image`, whose application folder contains a launcher, application resources, and a bundled Java runtime; it then zips the complete image. End users do not need Java or Maven. The deployment guide explicitly requires testing the launcher and ZIP after extraction and explains the per-user data location.

The checked-in packaging path produces a portable app-image ZIP, **not an MSI or installer**. It does not show a GitHub Actions workflow, release upload, checksum generation, signing, or evidence of a separate clean-machine test. An MSI would need an appropriate Windows installer toolchain (the guide names WiX). The documented `java -jar` run command is for development, not end-user distribution.

## Reusable engineering principles for Lunar Atlas

1. Keep Swing components on the EDT and keep domain calculations independent of presentation.
2. Use typed, validated domain values and immutable results at service/UI boundaries.
3. Give the application a clear owner for startup, background work, errors, persistence, and shutdown.
4. Persist user-created state separately from bundled reference data; initialize writable per-user storage automatically.
5. Use atomic replacement/versioning for mutable local files and report persistence errors explicitly.
6. Make offline operation a first-class path and label optional or unavailable data honestly.
7. Test invariants and known reference cases with deterministic inputs before shipping a portable runtime bundle.
8. Document schematic or illustrative visualizations as such, and test the actual packaged launcher rather than equating a successful JAR build with a release.

## Approaches not copied

- BasinWatch's flood response model, event pipeline, severity ordering, resource allocator, and worker topology have no scientific analogue in a lunar atlas and will not be duplicated.
- Its intentionally schematic basin map cannot satisfy geographic or astronomical claims. Lunar Atlas must label its own schematic visualizations and must not imply measured visibility or tidal values from illustrative graphics.
- Its custom assertion runner is not a reason to avoid JUnit; Lunar Atlas uses standard automated tests for calculation and persistence behavior.
- Object serialization is not an appropriate interchange or durable format for research data. The atlas will keep curated reference records in explicit, reviewable resource files and use a separate user-data store.
- Optional SQLite history is useful in BasinWatch because of its event history. Lunar Atlas will add storage only where it supports actual features, not as a decorative architectural dependency.
- The reference's simulation-oriented thread count, piped stream, monitor-based queue, and optional thread-priority experiments are unnecessary for deterministic astronomical calculations and are not part of this design.
- A portable ZIP is valuable, but Lunar Atlas will not describe it as an installer; MSI/EXE packaging and signing will only be claimed if built and tested.

## Lunar Atlas decision

The initial implementation uses Java 17-compatible Swing for a lightweight, offline desktop experience and `jpackage` for Windows app-image packaging. The installed JDK 25 and Maven 3.9.9 can build/test/package Java 17-targeted source locally, but final compatibility and runtime claims depend on the tests and packaging validation actually completed. The first release is intentionally bounded to reproducible lunar-phase calculations, a selectable set of geographic locations, an honest schematic Earth–Moon view, sourced educational content, and locally saved preferences/notes. Eclipse visibility, high-precision ephemerides, tidal predictions, and culturally specific calendar conversions are not represented as implemented unless backed by data and tests.
