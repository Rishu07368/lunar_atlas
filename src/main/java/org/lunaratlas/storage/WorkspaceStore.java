package org.lunaratlas.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class WorkspaceStore {
    private static final long MAX_NOTE_BYTES = 1_000_000;
    private final Path directory;

    public WorkspaceStore() throws IOException {
        String appData = System.getenv("APPDATA");
        Path base = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), ".lunar-atlas")
                : Path.of(appData, "LunarAtlas");
        directory = prepareDirectory(base);
    }

    public WorkspaceStore(Path directory) throws IOException {
        this.directory = prepareDirectory(directory);
    }

    private static Path prepareDirectory(Path directory) throws IOException {
        Path normalized = directory.toAbsolutePath().normalize();
        Files.createDirectories(normalized);
        return normalized;
    }

    public Path directory() {
        return directory;
    }

    public String loadNotes() throws IOException {
        Path notes = directory.resolve("investigation-notes.txt");
        if (!Files.exists(notes)) {
            return "";
        }
        long size = Files.size(notes);
        if (size > MAX_NOTE_BYTES) {
            throw new IOException("Saved notes exceed the 1 MB safety limit.");
        }
        return Files.readString(notes, StandardCharsets.UTF_8);
    }

    public void saveNotes(String text) throws IOException {
        byte[] content = text.getBytes(StandardCharsets.UTF_8);
        if (content.length > MAX_NOTE_BYTES) {
            throw new IOException("Notes exceed the 1 MB safety limit.");
        }
        Path destination = directory.resolve("investigation-notes.txt");
        Path temporary = Files.createTempFile(directory, "notes-", ".tmp");
        try {
            Files.write(temporary, content, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
