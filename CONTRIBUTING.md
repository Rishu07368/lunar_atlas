# Contributing

## Build and verify

Use JDK 17+ and Maven 3.8+:

```powershell
mvn -B clean verify
```

On Windows, validate the portable build with:

```powershell
.\scripts\package-windows.ps1
```

## Scientific changes

- Keep computations outside Swing components and expose typed values.
- Add authoritative reference cases and boundary tests for algorithm changes; state tolerances and time standards.
- Do not display unavailable data as zero, a plausible-looking estimate, or fictional live output.
- Explain coordinate/time conventions, algorithm assumptions, and limitations in the relevant documentation.

## Curated research additions

Add specific, traceable references and scope/uncertainty statements in `src/main/resources/data/research-records.tsv`. Preserve the TSV header and eight fields per record. Do not copy restricted material or add sacred/private Indigenous knowledge. Review [CULTURAL_RESEARCH_METHOD.md](CULTURAL_RESEARCH_METHOD.md) and [DATA_SOURCES.md](DATA_SOURCES.md) when adding entries.

## UI and release behavior

Use existing Swing patterns and keep controls connected to documented outcomes. Add tests for persistence/export behavior. Do not label an app-image as an installer, and do not describe an artifact as tested unless the actual packaged launcher has been exercised.
