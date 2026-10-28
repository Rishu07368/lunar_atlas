$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$jpackage = Get-Command jpackage -ErrorAction SilentlyContinue
if ($null -eq $jpackage) {
    if (-not $env:JAVA_HOME) {
        throw "Install a full JDK with jpackage or add jpackage to PATH."
    }
    $jpackagePath = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
    if (-not (Test-Path -LiteralPath $jpackagePath -PathType Leaf)) {
        throw "jpackage.exe was not found under JAVA_HOME: $env:JAVA_HOME"
    }
}
else {
    $jpackagePath = $jpackage.Source
}

Push-Location $projectRoot
try {
    & mvn -B clean verify
    if ($LASTEXITCODE -ne 0) {
        throw "Maven verification failed with exit code $LASTEXITCODE."
    }

    $version = "0.1.0"
    $jarName = "lunar-atlas-$version.jar"
    $jar = Join-Path $projectRoot "target\$jarName"
    $icon = Join-Path $projectRoot "assets\lunar-atlas.ico"
    if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) {
        throw "Expected application JAR is missing: $jar"
    }
    if (-not (Test-Path -LiteralPath $icon -PathType Leaf)) {
        throw "The Lunar Atlas application icon is missing: $icon"
    }

    $packageInput = Join-Path $projectRoot "target\jpackage-input-$([guid]::NewGuid().ToString('N'))"
    New-Item -ItemType Directory -Path $packageInput | Out-Null
    Copy-Item -LiteralPath $jar -Destination (Join-Path $packageInput $jarName)

    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $releaseDirectory = Join-Path $projectRoot "dist\LunarAtlas-Windows-$version-$stamp"
    New-Item -ItemType Directory -Path $releaseDirectory -Force | Out-Null
    & $jpackagePath `
        --type app-image `
        --name LunarAtlas `
        --app-version $version `
        --vendor "Lunar Atlas Project" `
        --description "Offline lunar science and cultural exploration" `
        --input $packageInput `
        --main-jar $jarName `
        --main-class "org.lunaratlas.app.LunarAtlasApp" `
        --icon $icon `
        --dest $releaseDirectory
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed with exit code $LASTEXITCODE."
    }

    $image = Join-Path $releaseDirectory "LunarAtlas"
    $launcher = Join-Path $image "LunarAtlas.exe"
    if (-not (Test-Path -LiteralPath $launcher -PathType Leaf)) {
        throw "jpackage did not create its expected Windows launcher: $launcher"
    }
    if (-not (Test-Path -LiteralPath (Join-Path $image "runtime") -PathType Container)) {
        throw "The jpackage app-image does not contain its private runtime."
    }

    $zip = Join-Path $releaseDirectory "LunarAtlas-Windows.zip"
    Compress-Archive -Path $image -DestinationPath $zip -CompressionLevel Optimal
    if (-not (Test-Path -LiteralPath $zip -PathType Leaf)) {
        throw "Portable ZIP was not created: $zip"
    }

    $hash = (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLowerInvariant()
    Set-Content -LiteralPath "$zip.sha256" -Encoding ascii -Value "$hash  LunarAtlas-Windows.zip"
    $runtimeVersion = (& $jpackagePath --version).ToString()
    $manifest = @(
        "Project: Lunar Atlas"
        "Version: $version"
        "Build UTC: $([DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ'))"
        "Build runtime: $runtimeVersion"
        "Target: Windows 10/11 x64"
        "Distribution: jpackage app-image ZIP with bundled runtime"
        "Data version: curated-records-v0.1.0"
        "Data: bundled research-records.tsv; application data stored under %APPDATA%\LunarAtlas"
        "Location: optional one-time Windows GeoCoordinateWatcher; no IP lookup; Windows may use configured system location sources; timezone requires user confirmation."
        "Known limitations: see LIMITATIONS.md in the source distribution; no MSI/EXE installer is produced by this script."
        "SHA-256: $hash"
    )
    Set-Content -LiteralPath (Join-Path $releaseDirectory "release-manifest.txt") `
        -Encoding utf8 -Value $manifest

    Write-Output "Portable app-image: $image"
    Write-Output "Distributable ZIP: $zip"
    Write-Output "SHA-256 file: $zip.sha256"
    Write-Output "Release manifest: $(Join-Path $releaseDirectory 'release-manifest.txt')"
}
finally {
    Pop-Location
}
