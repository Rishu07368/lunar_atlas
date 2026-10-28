param(
    [Parameter(Mandatory = $true)]
    [string]$Launcher
)

$ErrorActionPreference = "Stop"
$resolvedLauncher = (Resolve-Path -LiteralPath $Launcher).Path
$testRoot = Join-Path $env:TEMP "LunarAtlas-smoke-$([guid]::NewGuid().ToString('N'))"
$testAppData = Join-Path $testRoot "AppData"
New-Item -ItemType Directory -Path $testAppData -Force | Out-Null
$originalAppData = $env:APPDATA
$originalPath = $env:PATH
$process = $null
$guiProcess = $null
try {
    $env:APPDATA = $testAppData
    $env:PATH = "$env:SystemRoot\System32;$env:SystemRoot"
    $process = Start-Process -FilePath $resolvedLauncher -PassThru
    if (-not $process.WaitForInputIdle(20000)) {
        throw "The packaged app did not become idle within 20 seconds."
    }
    $deadline = (Get-Date).AddSeconds(20)
    while ($null -eq $guiProcess -and (Get-Date) -lt $deadline) {
        $process.Refresh()
        if ($process.HasExited) {
            break
        }
        $children = Get-CimInstance Win32_Process |
            Where-Object { $_.ParentProcessId -eq $process.Id }
        foreach ($child in $children) {
            $candidate = Get-Process -Id $child.ProcessId -ErrorAction SilentlyContinue
            if ($null -ne $candidate -and $candidate.MainWindowHandle -ne 0) {
                $guiProcess = $candidate
                break
            }
        }
        if ($null -eq $guiProcess) {
            Start-Sleep -Seconds 1
        }
    }
    if ($null -eq $guiProcess) {
        throw "The packaged application exited or did not create a main window."
    }
    if (-not (Test-Path -LiteralPath (Join-Path $testAppData "LunarAtlas"))) {
        throw "The packaged app did not initialize its per-user data folder."
    }
    $guiProcess.CloseMainWindow() | Out-Null
    if (-not $guiProcess.WaitForExit(10000)) {
        throw "The packaged application did not close in response to a normal window-close request."
    }
    $process.WaitForExit(5000) | Out-Null
    Write-Output "Packaged launcher opened a main window and shut down normally."
}
finally {
    if ($null -ne $guiProcess) {
        $guiProcess.Refresh()
        if (-not $guiProcess.HasExited) {
            Stop-Process -Id $guiProcess.Id
        }
    }
    if ($null -ne $process) {
        $process.Refresh()
        if (-not $process.HasExited) {
            Stop-Process -Id $process.Id
        }
    }
    $env:APPDATA = $originalAppData
    $env:PATH = $originalPath
    if (Test-Path -LiteralPath $testRoot) {
        Remove-Item -LiteralPath $testRoot -Recurse -Force
    }
}
