package org.lunaratlas.app;

import org.lunaratlas.research.ResearchCatalog;
import org.lunaratlas.storage.PreferenceStore;
import org.lunaratlas.storage.WorkspaceStore;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public final class LunarAtlasApp {
    private LunarAtlasApp() {
    }

    public static void main(String[] args) {
        UiTheme.install();
        SwingUtilities.invokeLater(() -> {
            try {
                WorkspaceStore workspace = new WorkspaceStore();
                PreferenceStore preferences = new PreferenceStore(LunarAtlasApp.class);
                MainFrame frame = new MainFrame(workspace, preferences, ResearchCatalog.load());
                frame.setVisible(true);
            } catch (IOException | RuntimeException ex) {
                JOptionPane.showMessageDialog(null,
                        "Lunar Atlas could not start.\n\n" + ex.getMessage(),
                        "Startup failed", JOptionPane.ERROR_MESSAGE);
                System.err.println("Lunar Atlas startup failed: " + ex);
            }
        });
    }
}
