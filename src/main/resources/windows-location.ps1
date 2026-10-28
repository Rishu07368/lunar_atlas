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
    $started = $watcher.TryStart($false, [TimeSpan]::FromSeconds($TimeoutSeconds))

    if (-not $started) {
        $status = $watcher.Status.ToString()
        if ($status -eq "Disabled") {
            Write-Output "RESULT|DENIED|Windows location services are disabled or access was denied."
        }
        else {
            Write-Output "RESULT|UNAVAILABLE|Windows location service status: $status"
        }
        exit 0
    }

    $coordinate = $watcher.Position.Location
    if ($coordinate.IsUnknown) {
        Write-Output "RESULT|UNAVAILABLE|Windows location service returned no coordinates."
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
