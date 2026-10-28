package org.lunaratlas.app;

import org.lunaratlas.astronomy.GeoLocation;
import org.lunaratlas.astronomy.LocationCatalog;
import org.lunaratlas.storage.PreferenceStore;
import org.lunaratlas.storage.WorkspaceStore;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;

final class WorkspacePanel extends JPanel {
    private final WorkspaceStore workspace;
    private final PreferenceStore preferences;
    private final Consumer<GeoLocation> selectLocation;
    private final JTextArea notes = new JTextArea();
    private final DefaultListModel<GeoLocation> bookmarkModel = new DefaultListModel<>();
    private final javax.swing.JLabel status = new javax.swing.JLabel("Notes are stored only on this computer.");

    WorkspacePanel(WorkspaceStore workspace, PreferenceStore preferences,
                   Consumer<GeoLocation> selectLocation) throws java.io.IOException {
        super(new BorderLayout(12, 12));
        this.workspace = workspace;
        this.preferences = preferences;
        this.selectLocation = selectLocation;
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(UiTheme.BACKGROUND);
        left.add(new javax.swing.JLabel("Saved locations"), BorderLayout.NORTH);
        JList<GeoLocation> bookmarks = new JList<>(bookmarkModel);
        bookmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookmarks.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && bookmarks.getSelectedValue() != null) {
                selectLocation.accept(bookmarks.getSelectedValue());
            }
        });
        left.add(new JScrollPane(bookmarks), BorderLayout.CENTER);
        JButton addBookmark = new JButton("Bookmark selected location");
        addBookmark.addActionListener(event -> {
            GeoLocation location = selectedLocation;
            if (location != null) {
                preferences.addBookmark(location.id());
                reloadBookmarks();
                status.setText("Saved " + location.name() + " in local preferences.");
            }
        });
        left.add(addBookmark, BorderLayout.SOUTH);
        left.setPreferredSize(new java.awt.Dimension(250, 400));
        add(left, BorderLayout.WEST);

        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setText(workspace.loadNotes());
        JPanel notePanel = new JPanel(new BorderLayout(8, 8));
        notePanel.setBackground(UiTheme.BACKGROUND);
        notePanel.add(new javax.swing.JLabel("Private investigation notes (saved locally; not synchronized)"),
                BorderLayout.NORTH);
        notePanel.add(new JScrollPane(notes), BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(UiTheme.BACKGROUND);
        JButton save = new JButton("Save notes");
        save.addActionListener(event -> saveNotes());
        footer.add(save);
        footer.add(status);
        notePanel.add(footer, BorderLayout.SOUTH);
        add(notePanel, BorderLayout.CENTER);
        reloadBookmarks();
    }

    private GeoLocation selectedLocation;

    void setSelectedLocation(GeoLocation location) {
        selectedLocation = location;
    }

    private void reloadBookmarks() {
        bookmarkModel.clear();
        for (String id : preferences.bookmarks()) {
            GeoLocation location = "custom".equals(id)
                    ? preferences.customLocation().orElse(null) : LocationCatalog.byId(id);
            if (location != null) {
                bookmarkModel.addElement(location);
            }
        }
    }

    private void saveNotes() {
        try {
            workspace.saveNotes(notes.getText());
            status.setText("Saved to " + workspace.directory().resolve("investigation-notes.txt"));
        } catch (java.io.IOException ex) {
            status.setText("Could not save notes: " + ex.getMessage());
        }
    }
}
