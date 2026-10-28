param(
    [Parameter(Mandatory = $true)]
    [ValidateRange(1, 60)]
    [int]$TimeoutSeconds
)

$ErrorActionPreference = "Stop"
$watcher = $null

try {
    Add-Type -AssemblyName System.Device
    $watcher = New-Object System.Device.Location.GeoCoordinateWatcher
    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    $startupTimeout = [Math]::Min(5, $TimeoutSeconds)
    $started = $watcher.TryStart($false, [TimeSpan]::FromSeconds($startupTimeout))

    if (-not $started) {
        $status = $watcher.Status.ToString()
        if ($status -eq "Disabled") {
            Write-Output "RESULT|DENIED|Windows location services are disabled or access was denied."
            exit 0
        }
        if ($status -ne "NoData") {
            Write-Output "RESULT|UNAVAILABLE|Windows location service status: $status"
            exit 0
        }
        $watcher.Start()
    }

    do {
        if ($watcher.Status.ToString() -eq "Disabled") {
            Write-Output "RESULT|DENIED|Windows location access is disabled or denied."
            exit 0
        }
        $coordinate = $watcher.Position.Location
        if (-not $coordinate.IsUnknown) {
            break
        }
        Start-Sleep -Milliseconds 250
    } while ([DateTime]::UtcNow -lt $deadline)

    if ($coordinate.IsUnknown) {
        Write-Output "RESULT|UNAVAILABLE|Windows location service started but returned no position within the timeout. Check desktop-app location access and available location sources."
        exit 0
    }

    $latitude = [double]$coordinate.Latitude
    $longitude = [double]$coordinate.Longitude
    $accuracy = [double]$coordinate.HorizontalAccuracy
    if ([double]::IsNaN($accuracy) -or [double]::IsInfinity($accuracy) -or $accuracy -lt 0) {
        $accuracy = -1
    }
    $formatted = [string]::Format(
        [Globalization.CultureInfo]::InvariantCulture,
        "RESULT|SUCCESS|{0:R}|{1:R}|{2:R}",
        $latitude,
        $longitude,
        $accuracy
    )
    Write-Output $formatted
}
catch {
    $message = $_.Exception.Message -replace '[\r\n|]+', ' '
    Write-Output "RESULT|ERROR|$message"
}
finally {
    if ($null -ne $watcher) {
        $watcher.Stop()
        $watcher.Dispose()
    }
}
