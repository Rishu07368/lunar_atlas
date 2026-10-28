package org.lunaratlas.app;

import org.lunaratlas.astronomy.LunarCalculator;
import org.lunaratlas.astronomy.PhaseEventFilter;
import org.lunaratlas.astronomy.PhaseEvent;
import org.lunaratlas.io.EventCsvExporter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class EventCalendarPanel extends JPanel {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private final LunarCalculator calculator;
    private final JTextField from = new JTextField(10);
    private final JTextField through = new JTextField(10);
    private final JComboBox<PhaseEventFilter> filter = new JComboBox<>(PhaseEventFilter.values());
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Event (UTC)", "Phase", "Estimated precision"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private List<PhaseEvent> visibleEvents = List.of();
    private final JLabel status = new JLabel("Select a date range and generate calculated phase events.");

    EventCalendarPanel(LunarCalculator calculator) {
        super(new BorderLayout(12, 12));
        this.calculator = calculator;
        setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        LocalDate today = LocalDate.now();
        from.setText(today.minusMonths(1).format(DATE));
        through.setText(today.plusMonths(2).format(DATE));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 6));
        controls.setBackground(UiTheme.BACKGROUND);
        controls.add(new JLabel("From (YYYY-MM-DD)"));
        controls.add(from);
        controls.add(new JLabel("Through"));
        controls.add(through);
        controls.add(filter);
        JButton generate = new JButton("Calculate events");
        generate.addActionListener(event -> calculate());
        controls.add(generate);
        JButton export = new JButton("Export visible CSV");
        export.addActionListener(event -> export());
        controls.add(export);
        add(controls, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setBackground(UiTheme.BACKGROUND);
        footer.add(status, BorderLayout.NORTH);
        JLabel unavailable = new JLabel("<html>Eclipses, perigee/apogee, and local visibility: "
                + "<b>Unavailable in this release</b> — no eclipse-path or validated event dataset is bundled.</html>");
        unavailable.setForeground(UiTheme.MUTED);
        footer.add(unavailable, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        calculate();
    }

    private void calculate() {
        try {
            LocalDate start = LocalDate.parse(from.getText().trim(), DATE);
            LocalDate end = LocalDate.parse(through.getText().trim(), DATE);
            List<PhaseEvent> all = calculator.events(start, end, ZoneOffset.UTC);
            PhaseEventFilter selected = (PhaseEventFilter) filter.getSelectedItem();
            visibleEvents = selected.apply(all);
            tableModel.setRowCount(0);
            for (PhaseEvent item : visibleEvents) {
                tableModel.addRow(new Object[]{item.instant().toString(), item.phase().label(),
                        "Approx. ± several hours; low-precision algorithm"});
            }
            status.setText(visibleEvents.size() + " calculated phase events · UTC · algorithmic estimates, not an official almanac.");
        } catch (RuntimeException ex) {
            status.setText("Could not calculate events: " + ex.getMessage());
        }
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("lunar-phase-events.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            new EventCsvExporter().export(visibleEvents, chooser.getSelectedFile().toPath());
            status.setText("Exported " + visibleEvents.size() + " events to "
                    + chooser.getSelectedFile().getAbsolutePath());
        } catch (java.io.IOException ex) {
            status.setText("CSV export failed: " + ex.getMessage());
        }
    }

}
