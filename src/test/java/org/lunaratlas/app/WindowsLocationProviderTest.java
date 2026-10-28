package org.lunaratlas.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowsLocationProviderTest {
    @Test
    void parsesAndValidatesSuccessfulWindowsLocationResponse() {
        WindowsLocationProvider.Result result = WindowsLocationProvider.parseOutput(
                "provider diagnostic\r\nRESULT|SUCCESS|28.6139|77.209|120.5\r\n");

        assertEquals(WindowsLocationProvider.Status.SUCCESS, result.status());
        assertEquals(28.6139, result.latitude(), 1e-8);
        assertEquals(77.209, result.longitude(), 1e-8);
        assertEquals(120.5, result.accuracyMeters(), 1e-8);
    }

    @Test
    void mapsPermissionAndUnavailableResponsesWithoutThrowing() {
        assertEquals(WindowsLocationProvider.Status.DENIED,
                WindowsLocationProvider.parseOutput("RESULT|DENIED|Location is disabled").status());
        assertEquals(WindowsLocationProvider.Status.UNAVAILABLE,
                WindowsLocationProvider.parseOutput("RESULT|UNAVAILABLE|No fix").status());
        assertEquals(WindowsLocationProvider.Status.UNAVAILABLE,
                WindowsLocationProvider.parseOutput(
                        "RESULT|UNAVAILABLE|Windows location service started but returned no position.").status());
        assertEquals(WindowsLocationProvider.Status.FAILED,
                WindowsLocationProvider.parseOutput("RESULT|SUCCESS|91|0|10").status());
        assertEquals(WindowsLocationProvider.Status.FAILED,
                WindowsLocationProvider.parseOutput("provider failed").status());
    }
}
