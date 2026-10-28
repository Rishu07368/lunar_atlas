# Deployment

## Developer build

Requirements: JDK 17 or newer, Maven 3.8+, Windows PowerShell for Windows packaging. The end-user Windows location action uses the built-in Windows PowerShell 5.1 and .NET Framework `System.Device.Location.GeoCoordinateWatcher`; it adds no Maven/runtime dependency.

```powershell
mvn -B clean verify
java -jar target\lunar-atlas-0.1.0.jar
```

The project compiles with Java 17 language/API compatibility. The package uses no runtime third-party dependencies.

## Portable Windows distribution

On Windows with a full JDK containing `jpackage`:

```powershell
.\scripts\package-windows.ps1
```

The script runs `mvn -B clean verify`, creates a `jpackage --type app-image` with a private runtime, verifies the launcher/runtime files, zips the complete app image, writes a SHA-256 sidecar, and emits a release manifest. Output is created under a unique `dist\LunarAtlas-Windows-0.1.0-<timestamp>` directory. The portable ZIP is the supported end-user delivery path.

To perform a manual interactive launch check:

```powershell
.\scripts\smoke-test-windows.ps1 -Launcher ".\dist\<release-folder>\LunarAtlas\LunarAtlas.exe"
```

This smoke test requires an interactive desktop session. It creates a unique temporary `%APPDATA%` folder for the child process and attempts normal window-close shutdown.

## End-user instructions

1. Download the ZIP from a published project release.
2. Extract the entire ZIP to a writable folder.
3. Launch `LunarAtlas\LunarAtlas.exe`.
4. Keep the `LunarAtlas` folder intact; it contains the application and bundled Java runtime.
5. Back up `%APPDATA%\LunarAtlas` to retain local preferences and notes.

The application stores only explicit user preferences, custom coordinates/time zone, bookmarks, and saved notes. It does not need Java, Maven, Python, Node.js, SQLite Server, or credentials. Core functions work offline. On an explicit location request, the Windows location service may rely on available device sensors, Wi-Fi positioning, or network connectivity according to Windows settings.

## Installers, signatures, and verification boundary

This project does **not** currently produce an MSI or installer EXE. The verified packaging target is a portable `jpackage` app-image ZIP. A native MSI may require WiX/installer tooling on the Windows build worker and a tested installation/uninstallation path; it is not silently claimed here. The app is unsigned unless a release owner adds a valid code-signing certificate and verifies signatures.

The GitHub workflow at `.github/workflows/windows.yml` builds/tests on a Windows runner and uploads portable ZIP, checksum, and manifest files. Workflow existence is not evidence that a workflow run has passed. Public releases are created only from version tags after the job succeeds.

## Troubleshooting

- **Windows blocks an unknown publisher:** the distribution is not signed. Verify its origin and checksum before choosing to run it.
- **The EXE does not start:** extract the full ZIP; do not separate the EXE from its `app` and `runtime` folders.
- **Unable to save notes:** ensure `%APPDATA%\LunarAtlas` is writable; there are no external database/service prerequisites.
- **No moonrise/moonset shown:** the approximate model found no threshold crossing on that local date, or the simplified model did not resolve one. This is not a precision high-latitude almanac.
- **Current location unavailable:** enable the main Location services setting and the separate **Let desktop apps access your location** switch under Windows Settings > Privacy & security > Location. Allowing Store apps alone may not grant a traditional desktop application access. Windows must also have a usable sensor/provider and, for Wi-Fi positioning, a suitable network/source. The app waits up to 20 seconds for a location fix; it does not assume that starting the provider means coordinates are ready. If unavailable, select a built-in city or use **Enter coordinates…**. No IP-based fallback is used. Confirm the time zone in the app; Windows location coordinates do not include a time zone.
