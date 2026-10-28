package org.lunaratlas.app;

import org.lunaratlas.astronomy.GeoLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class WindowsLocationProvider {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(25);
    private static final Duration PROCESS_GRACE = Duration.ofSeconds(10);

    enum Status {
        SUCCESS, DENIED, UNAVAILABLE, TIMEOUT, FAILED
    }

    record Result(Status status, double latitude, double longitude,
                  double accuracyMeters, String detail) {
        static Result failure(Status status, String detail) {
            return new Result(status, Double.NaN, Double.NaN, Double.NaN, detail);
        }
    }

    Result detect() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows")) {
            return Result.failure(Status.UNAVAILABLE, "Device location is supported only on Windows.");
        }

        Path script = null;
        Process process = null;
        try {
            script = copyProviderScript();
            String powershell = Path.of(System.getenv().getOrDefault("WINDIR", "C:\\Windows"),
                    "System32", "WindowsPowerShell", "v1.0", "powershell.exe").toString();
            process = new ProcessBuilder(powershell, "-NoProfile",
                    "-File", script.toString(), "-TimeoutSeconds", "20")
                    .redirectErrorStream(true)
                    .start();
            boolean exited = process.waitFor(REQUEST_TIMEOUT.plus(PROCESS_GRACE).toSeconds(),
                    TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
                process.waitFor();
                return Result.failure(Status.TIMEOUT,
                        "Windows location request exceeded its 35-second safety timeout.");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return parseOutput(output);
        } catch (IOException ex) {
            return Result.failure(Status.FAILED, "Could not start the Windows location provider: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            return Result.failure(Status.FAILED, "The device location request was interrupted.");
        } catch (SecurityException ex) {
            return Result.failure(Status.FAILED, "Windows blocked the location provider: " + ex.getMessage());
        } finally {
            if (script != null) {
                try {
                    Files.deleteIfExists(script);
                } catch (IOException ignored) {
                    script.toFile().deleteOnExit();
                }
            }
        }
    }

    static Result parseOutput(String output) {
        for (String line : output.split("\\R")) {
            if (!line.startsWith("RESULT|")) {
                continue;
            }
            String[] fields = line.split("\\|", 0);
            if (fields.length < 3) {
                return Result.failure(Status.FAILED, "Windows returned an incomplete location response.");
            }
            switch (fields[1]) {
                case "SUCCESS" -> {
                    if (fields.length != 5) {
                        return Result.failure(Status.FAILED, "Windows returned an incomplete location response.");
                    }
                    try {
                        double latitude = Double.parseDouble(fields[2]);
                        double longitude = Double.parseDouble(fields[3]);
                        double accuracy = Double.parseDouble(fields[4]);
                        new GeoLocation("detected", "Windows device location", latitude, longitude,
                                ZoneId.of("UTC"));
                        if (!Double.isFinite(accuracy) || accuracy < -1) {
                            return Result.failure(Status.FAILED, "Windows returned invalid accuracy information.");
                        }
                        return new Result(Status.SUCCESS, latitude, longitude, accuracy, "");
                    } catch (IllegalArgumentException ex) {
                        return Result.failure(Status.FAILED, "Windows returned invalid location coordinates.");
                    }
                }
                case "DENIED" -> {
                    return Result.failure(Status.DENIED, fields[2]);
                }
                case "UNAVAILABLE" -> {
                    return Result.failure(Status.UNAVAILABLE, fields[2]);
                }
                case "ERROR" -> {
                    return Result.failure(Status.FAILED, fields[2]);
                }
                default -> {
                    return Result.failure(Status.FAILED, "Windows returned an unrecognized location status.");
                }
            }
        }
        return Result.failure(Status.FAILED, "Windows location provider returned no result.");
    }

    private static Path copyProviderScript() throws IOException {
        Path script = Files.createTempFile("lunar-atlas-location-", ".ps1");
        try (InputStream resource = WindowsLocationProvider.class.getResourceAsStream("/windows-location.ps1")) {
            if (resource == null) {
                throw new IOException("Bundled Windows location provider script is missing.");
            }
            Files.copy(resource, script, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Files.deleteIfExists(script);
            throw ex;
        }
        return script;
    }
}
