package org.lunaratlas.app;

import org.lunaratlas.astronomy.GeoLocation;
import org.lunaratlas.astronomy.LunarCalculator;
import org.lunaratlas.astronomy.LunarState;
import org.lunaratlas.astronomy.LocationCatalog;
import org.lunaratlas.research.ResearchRecord;
import org.lunaratlas.storage.PreferenceStore;
import org.lunaratlas.storage.WorkspaceStore;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

final class MainFrame extends JFrame {
    private final PreferenceStore preferences;
    private final LunarCalculator calculator = new LunarCalculator();
    private final JComboBox<GeoLocation> locations = new JComboBox<>(
            LocationCatalog.locations().toArray(GeoLocation[]::new));
    private final JSpinner datePicker;
    private final ObservatoryPanel observatory = new ObservatoryPanel(calculator);
    private final OrbitPanel orbit = new OrbitPanel();
    private final WorldPanel world;
    private final EventCalendarPanel calendar = new EventCalendarPanel(calculator);
    private final WorkspacePanel workspacePanel;
    private final JLabel status = new JLabel("Offline calculations · device location is requested only when selected");
    private final JButton detectLocation = new JButton("Use My Current Location");
    private GeoLocation customLocation;

    MainFrame(WorkspaceStore workspace, PreferenceStore preferences,
              List<ResearchRecord> research) throws java.io.IOException {
        super("Lunar Atlas");
        java.net.URL icon = MainFrame.class.getResource("/lunar-atlas.png");
        if (icon == null) {
            throw new IllegalStateException("Bundled application icon is missing.");
        }
        setIconImage(new ImageIcon(icon).getImage());
        this.preferences = preferences;
        if ("custom".equals(preferences.locationId())) {
            customLocation = preferences.customLocation().orElse(null);
            if (customLocation != null) {
                locations.addItem(customLocation);
            }
        }
        GeoLocation savedLocation = customLocation == null
                ? LocationCatalog.byId(preferences.locationId()) : customLocation;
        locations.setSelectedItem(savedLocation);
        LocalDate selectedDate = preferences.selectedDate();
        datePicker = new JSpinner(new SpinnerDateModel(
                Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(datePicker, "yyyy-MM-dd");
        dateEditor.getFormat().setTimeZone(java.util.TimeZone.getTimeZone(ZoneId.systemDefault()));
        datePicker.setEditor(dateEditor);
        world = new WorldPanel(locations::setSelectedItem);
        workspacePanel = new WorkspacePanel(workspace, preferences, this::selectLocation);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(980, 680));
        setSize(1220, 820);
        setLocationByPlatform(true);
        getContentPane().setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Observatory", observatory);
        JPanel orbitPage = new JPanel(new BorderLayout(8, 8));
        orbitPage.setBackground(UiTheme.BACKGROUND);
        orbitPage.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        orbitPage.add(orbit, BorderLayout.CENTER);
        orbitPage.add(new JLabel("Date selection changes the phase shown. The orbit drawing is explanatory, "
                + "not proportional or an ephemeris.", JLabel.CENTER), BorderLayout.SOUTH);
        tabs.addTab("Earth–Moon", orbitPage);
        tabs.addTab("Geographic explorer", world);
        tabs.addTab("Phase calendar", calendar);
        tabs.addTab("Research atlas", new ResearchPanel(research));
        tabs.addTab("My workspace", workspacePanel);
        add(tabs, BorderLayout.CENTER);
        status.setForeground(UiTheme.MUTED);
        status.setBorder(BorderFactory.createEmptyBorder(7, 14, 8, 14));
        add(status, BorderLayout.SOUTH);

        locations.addActionListener(event -> refresh());
        datePicker.addChangeListener(event -> refresh());
        detectLocation.addActionListener(event -> detectCurrentLocation());
        Timer clockTimer = new Timer(30_000, event -> observatory.updateClock(currentLocation()));
        clockTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                clockTimer.stop();
            }
        });
        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBackground(UiTheme.PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 13, 18));
        JPanel title = new JPanel(new BorderLayout());
        title.setBackground(UiTheme.PANEL);
        JLabel name = new JLabel("LUNAR ATLAS");
        name.setForeground(UiTheme.ACCENT);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 20f));
        title.add(name, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Global lunar science, geography & cultural exploration · offline edition");
        subtitle.setForeground(UiTheme.MUTED);
        title.add(subtitle, BorderLayout.SOUTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        controls.setBackground(UiTheme.PANEL);
        locations.setPreferredSize(new java.awt.Dimension(230, locations.getPreferredSize().height));
        controls.add(new JLabel("Location"));
        controls.add(locations);
        controls.add(new JLabel("Date"));
        controls.add(datePicker);
        javax.swing.JButton today = new javax.swing.JButton("Today");
        today.addActionListener(event -> setDate(LocalDate.now(currentLocation().zoneId())));
        controls.add(today);
        detectLocation.setToolTipText("One-time Windows location lookup; Windows may use its enabled location services.");
        controls.add(detectLocation);
        JButton manualLocation = new JButton("Enter coordinates…");
        manualLocation.addActionListener(event -> enterManualLocation());
        controls.add(manualLocation);
        header.add(title, BorderLayout.NORTH);
        header.add(controls, BorderLayout.SOUTH);
        return header;
    }

    private void refresh() {
        try {
            GeoLocation location = currentLocation();
            LocalDate date = selectedDate();
            preferences.setLocationId(location.id());
            if ("custom".equals(location.id())) {
                preferences.setCustomLocation(location);
            }
            preferences.setSelectedDate(date);
            LunarState state = observatory.refresh(date, location);
            orbit.setState(state);
            world.setSelected(location);
            workspacePanel.setSelectedLocation(location);
            status.setText("Calculated · low-precision orbital model · local time zone: " + location.zoneId());
        } catch (RuntimeException | java.text.ParseException ex) {
            status.setText("Could not update the display: " + ex.getMessage());
        }
    }

    private GeoLocation currentLocation() {
        return (GeoLocation) locations.getSelectedItem();
    }

    private LocalDate selectedDate() throws java.text.ParseException {
        datePicker.commitEdit();
        Date value = (Date) datePicker.getValue();
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setDate(LocalDate date) {
        datePicker.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void detectCurrentLocation() {
        detectLocation.setEnabled(false);
        status.setText("Requesting one-time location from Windows; no continuous tracking is used…");
        new SwingWorker<WindowsLocationProvider.Result, Void>() {
            @Override
            protected WindowsLocationProvider.Result doInBackground() {
                return new WindowsLocationProvider().detect();
            }

            @Override
            protected void done() {
                detectLocation.setEnabled(true);
                try {
                    WindowsLocationProvider.Result result = get();
                    if (result.status() != WindowsLocationProvider.Status.SUCCESS) {
                        showLocationFailure(result);
                        return;
                    }
                    chooseDetectedLocation(result);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    status.setText("The device location request was interrupted.");
                } catch (java.util.concurrent.ExecutionException ex) {
                    status.setText("Device location failed: " + ex.getCause().getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this, status.getText(),
                            "Location unavailable", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void chooseDetectedLocation(WindowsLocationProvider.Result result) {
        JComboBox<String> zones = timeZoneSelector(ZoneId.systemDefault());
        String accuracy = result.accuracyMeters() < 0
                ? "Accuracy was not reported."
                : String.format(Locale.ROOT, "Reported horizontal accuracy: about %.0f m.", result.accuracyMeters());
        JPanel confirmation = new JPanel(new GridLayout(0, 1, 4, 4));
        confirmation.add(new JLabel(String.format(Locale.ROOT,
                "Coordinates: %.6f°, %.6f°", result.latitude(), result.longitude())));
        confirmation.add(new JLabel(accuracy));
        confirmation.add(new JLabel("Windows provides no timezone with these coordinates. Confirm or change it:"));
        confirmation.add(zones);
        int choice = JOptionPane.showConfirmDialog(this, confirmation,
                "Confirm device location", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            status.setText("Device location was not applied; the previous location remains selected.");
            return;
        }
        try {
            ZoneId zone = ZoneId.of(Objects.toString(zones.getSelectedItem()));
            String name = result.accuracyMeters() < 0
                    ? "Windows device location"
                    : String.format(Locale.ROOT, "Windows device location (±%.0f m)", result.accuracyMeters());
            applyCustomLocation(new GeoLocation("custom", name,
                    result.latitude(), result.longitude(), zone), "Windows device location");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Choose a valid time zone identifier (for example, "
                    + "America/New_York).\n" + ex.getMessage(), "Invalid time zone", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enterManualLocation() {
        JTextField name = new JTextField("Manual coordinates", 22);
        JTextField latitude = new JTextField(22);
        JTextField longitude = new JTextField(22);
        JComboBox<String> zones = timeZoneSelector(currentLocation().zoneId());
        JPanel fields = new JPanel(new GridLayout(0, 2, 6, 6));
        fields.add(new JLabel("Location name"));
        fields.add(name);
        fields.add(new JLabel("Latitude (−90 to 90)"));
        fields.add(latitude);
        fields.add(new JLabel("Longitude (−180 to 180)"));
        fields.add(longitude);
        fields.add(new JLabel("Time zone"));
        fields.add(zones);
        int choice = JOptionPane.showConfirmDialog(this, fields,
                "Enter a location manually", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            String label = name.getText().trim();
            if (label.length() > 80) {
                throw new IllegalArgumentException("Location name must be 80 characters or fewer.");
            }
            GeoLocation location = new GeoLocation("custom", label,
                    Double.parseDouble(latitude.getText().trim()),
                    Double.parseDouble(longitude.getText().trim()),
                    ZoneId.of(Objects.toString(zones.getSelectedItem())));
            applyCustomLocation(location, "Manual location");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid name, latitude, longitude, and time zone.\n"
                    + ex.getMessage(), "Invalid location", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComboBox<String> timeZoneSelector(ZoneId selectedZone) {
        List<String> zoneIds = ZoneId.getAvailableZoneIds().stream().sorted(Comparator.naturalOrder()).toList();
        JComboBox<String> zones = new JComboBox<>(zoneIds.toArray(String[]::new));
        zones.setEditable(true);
        zones.setSelectedItem(selectedZone.getId());
        return zones;
    }

    private void applyCustomLocation(GeoLocation location, String source) {
        if (customLocation != null) {
            locations.removeItem(customLocation);
        }
        customLocation = location;
        locations.addItem(location);
        locations.setSelectedItem(location);
        status.setText(source + " applied · coordinates remain on this device · time zone " + location.zoneId());
    }

    private void selectLocation(GeoLocation location) {
        if ("custom".equals(location.id())) {
            if (customLocation != null && !customLocation.equals(location)) {
                locations.removeItem(customLocation);
            }
            customLocation = location;
            boolean present = false;
            for (int index = 0; index < locations.getItemCount(); index++) {
                if (locations.getItemAt(index).equals(location)) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                locations.addItem(location);
            }
        }
        locations.setSelectedItem(location);
    }

    private void showLocationFailure(WindowsLocationProvider.Result result) {
        String details = switch (result.status()) {
            case DENIED -> "Windows location access is disabled or denied. Enable Location services in "
                    + "Windows Settings > Privacy & security > Location, then try again.";
            case UNAVAILABLE -> "Windows could not provide coordinates before the request timed out. "
                    + "A location sensor, Wi-Fi positioning, network, or Windows location service may be unavailable.";
            case TIMEOUT -> result.detail();
            case FAILED -> result.detail();
            case SUCCESS -> throw new IllegalStateException("Successful location has no failure message.");
        };
        Object[] options = {"Enter coordinates…", "Open Location settings", "Close"};
        int choice = JOptionPane.showOptionDialog(this, details, "Current location unavailable",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            enterManualLocation();
        } else if (choice == 1) {
            openLocationSettings();
        }
        status.setText("Device location unavailable · manual location selection remains available.");
    }

    private void openLocationSettings() {
        try {
            new ProcessBuilder("cmd.exe", "/c", "start", "", "ms-settings:privacy-location").start();
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open Windows Location settings.\n"
                    + "Open Settings > Privacy & security > Location manually.\n" + ex.getMessage(),
                    "Could not open settings", JOptionPane.ERROR_MESSAGE);
        }
    }
}
