package org.lunaratlas.app;

import org.lunaratlas.astronomy.GeoLocation;
import org.lunaratlas.astronomy.HorizonTimes;
import org.lunaratlas.astronomy.LunarCalculator;
import org.lunaratlas.astronomy.LunarState;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObservatoryPanel extends JPanel {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm z");
    private final LunarCalculator calculator;
    private final JLabel phase = new JLabel("Calculating", SwingConstants.CENTER);
    private final JLabel illumination = new JLabel("", SwingConstants.CENTER);
    private final JLabel age = new JLabel();
    private final JLabel distance = new JLabel();
    private final JLabel position = new JLabel();
    private final JLabel riseSet = new JLabel();
    private final JLabel timeStandards = new JLabel();
    private final MoonCanvas moonCanvas = new MoonCanvas();
    private Instant selectedInstant;

    ObservatoryPanel(LunarCalculator calculator) {
        super(new BorderLayout(18, 18));
        this.calculator = calculator;
        setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(UiTheme.PANEL);
        left.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        moonCanvas.setPreferredSize(new Dimension(245, 245));
        phase.setFont(phase.getFont().deriveFont(Font.BOLD, 23f));
        left.add(phase, BorderLayout.NORTH);
        left.add(moonCanvas, BorderLayout.CENTER);
        JPanel facts = new JPanel();
        facts.setLayout(new BoxLayout(facts, BoxLayout.Y_AXIS));
        facts.setBackground(UiTheme.BACKGROUND);
        facts.add(Box.createVerticalStrut(8));
        facts.add(sectionLabel("OBSERVING CONDITIONS"));
        facts.add(Box.createVerticalStrut(14));
        facts.add(illumination);
        facts.add(Box.createVerticalStrut(14));
        facts.add(age);
        facts.add(Box.createVerticalStrut(14));
        facts.add(distance);
        facts.add(Box.createVerticalStrut(14));
        facts.add(position);
        facts.add(Box.createVerticalStrut(14));
        facts.add(riseSet);
        facts.add(Box.createVerticalStrut(18));
        facts.add(timeStandards);
        for (Component component : facts.getComponents()) {
            if (component instanceof JComponent swingComponent) {
                swingComponent.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            }
        }
        add(left, BorderLayout.WEST);
        add(facts, BorderLayout.CENTER);
    }

    LunarState refresh(LocalDate date, GeoLocation location) {
        Instant instant = date.equals(LocalDate.now(location.zoneId()))
                ? Instant.now() : date.atTime(12, 0).atZone(location.zoneId()).toInstant();
        LunarState state = calculator.calculate(instant, location);
        HorizonTimes horizons = calculator.horizonTimes(date, location);
        phase.setText(state.phase().label());
        illumination.setText(String.format(Locale.ROOT, "%.1f%% illuminated",
                state.illuminatedFraction() * 100));
        age.setText(String.format(Locale.ROOT, "Phase age: %.2f days since new moon", state.phaseAgeDays()));
        distance.setText(String.format(Locale.ROOT, "Geocentric distance: %,.0f km", state.distanceKilometers()));
        position.setText(String.format(Locale.ROOT,
                "Approx. topocentric altitude %.1f° · azimuth %.1f° · RA %.1f° / Dec %.1f°",
                state.altitudeDegrees(), state.azimuthDegrees(),
                state.rightAscensionDegrees(), state.declinationDegrees()));
        riseSet.setText("Approx. moonrise: " + format(horizons.moonrise(), location)
                + "  ·  moonset: " + format(horizons.moonset(), location));
        selectedInstant = instant;
        updateClock(location);
        moonCanvas.setState(state);
        return state;
    }

    void updateClock(GeoLocation location) {
        if (selectedInstant == null) return;
        Instant now = Instant.now();
        timeStandards.setText("<html><body style='width:450px;color:#9CAFC2'>"
                + "Sky instant: " + selectedInstant + " UTC<br>Current UTC: " + now
                + " · local clock: " + now.atZone(location.zoneId()).format(CLOCK) + " ("
                + location.zoneId() + ")<br>Calculated with a low-precision orbital model; "
                + "only a first-order topocentric altitude correction is included. Refraction, "
                + "terrain horizon, observer elevation, and topocentric azimuth are not modeled. "
                + "Approximate rise/set uses a simple horizon threshold and "
                + "standard refraction/semidiameter allowance. "
                + "Not for navigation or observation planning requiring precision.</body></html>");
    }

    private static String format(java.util.Optional<Instant> instant, GeoLocation location) {
        return instant.map(value -> value.atZone(location.zoneId())
                .format(DateTimeFormatter.ofPattern("HH:mm z", Locale.ROOT))).orElse("no crossing");
    }

    private static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UiTheme.ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private static final class MoonCanvas extends JPanel {
        private LunarState state;

        private MoonCanvas() {
            setBackground(UiTheme.PANEL);
        }

        void setState(LunarState newState) {
            state = newState;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            if (state == null) return;
            int diameter = Math.min(getWidth(), getHeight()) - 25;
            graphics.drawImage(MoonGlyph.render(diameter,
                    state.phaseAgeDays()), (getWidth() - diameter) / 2,
                    (getHeight() - diameter) / 2, null);
        }
    }
}
