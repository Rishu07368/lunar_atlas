package org.lunaratlas.app;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

final class UiTheme {
    static final Color BACKGROUND = new Color(12, 19, 34);
    static final Color PANEL = new Color(20, 30, 48);
    static final Color PANEL_ALT = new Color(27, 40, 61);
    static final Color TEXT = new Color(230, 238, 248);
    static final Color MUTED = new Color(156, 173, 194);
    static final Color ACCENT = new Color(113, 197, 230);
    static final Color GOLD = new Color(239, 195, 115);

    private UiTheme() {
    }

    static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("TabbedPane.background", BACKGROUND);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", PANEL);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", PANEL_ALT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("ComboBox.background", PANEL_ALT);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("TextField.background", PANEL_ALT);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.background", PANEL_ALT);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("TextArea.caretForeground", TEXT);
        UIManager.put("Table.background", PANEL);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(46, 77, 104));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", PANEL_ALT);
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
    }
}
