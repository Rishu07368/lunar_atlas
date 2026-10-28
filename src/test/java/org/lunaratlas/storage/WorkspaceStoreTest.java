package org.lunaratlas.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkspaceStoreTest {
    @TempDir
    Path directory;

    @Test
    void notesAreCreatedAndReopenedFromLocalStorage() throws IOException {
        WorkspaceStore store = new WorkspaceStore(directory);
        assertEquals("", store.loadNotes());

        store.saveNotes("Observation notes: waxing crescent.");

        assertEquals("Observation notes: waxing crescent.",
                new WorkspaceStore(directory).loadNotes());
        assertTrueFileIsInWorkspace(store);
    }

    @Test
    void oversizedExistingNotesAreRejected() throws IOException {
        Files.writeString(directory.resolve("investigation-notes.txt"), "x".repeat(1_000_001));
        assertThrows(IOException.class, () -> new WorkspaceStore(directory).loadNotes());
    }

    private static void assertTrueFileIsInWorkspace(WorkspaceStore store) {
        org.junit.jupiter.api.Assertions.assertTrue(
                Files.exists(store.directory().resolve("investigation-notes.txt")));
    }
}
