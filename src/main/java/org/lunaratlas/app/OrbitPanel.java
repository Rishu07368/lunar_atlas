package org.lunaratlas.app;

import org.lunaratlas.astronomy.LunarState;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

final class OrbitPanel extends JPanel {
    private LunarState state;

    OrbitPanel() {
        setBackground(UiTheme.BACKGROUND);
        setPreferredSize(new java.awt.Dimension(650, 400));
    }

    void setState(LunarState newState) {
        state = newState;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (state == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2 - 15;
        int orbitRadius = Math.min(width / 3, height / 2 - 40);
        g.setColor(new Color(80, 104, 132));
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                1, new float[]{5, 6}, 0));
        g.drawOval(centerX - orbitRadius, centerY - orbitRadius,
                orbitRadius * 2, orbitRadius * 2);

        int sunX = Math.max(48, centerX - orbitRadius - 110);
        g.setColor(new Color(239, 195, 115, 35));
        g.fillOval(sunX - 36, centerY - 36, 72, 72);
        g.setColor(UiTheme.GOLD);
        g.fillOval(sunX - 23, centerY - 23, 46, 46);
        g.setColor(UiTheme.TEXT);
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g.drawString("SUN", sunX - 14, centerY + 48);

        g.setColor(new Color(74, 129, 175));
        g.fillOval(centerX - 33, centerY - 33, 66, 66);
        g.setColor(new Color(120, 166, 189, 150));
        g.drawArc(centerX - 24, centerY - 20, 45, 37, 20, 125);
        g.setColor(UiTheme.TEXT);
        g.drawString("EARTH", centerX - 25, centerY + 53);

        double fraction = Math.max(0, Math.min(1, state.phaseAgeDays() / 29.530588853));
        double angle = Math.PI + 2 * Math.PI * fraction;
        int moonX = centerX + (int) (orbitRadius * Math.cos(angle));
        int moonY = centerY + (int) (orbitRadius * Math.sin(angle));
        g.drawImage(MoonGlyph.render(38, state.phaseAgeDays()),
                moonX - 19, moonY - 19, null);
        g.setColor(UiTheme.TEXT);
        g.drawString("MOON", moonX - 20, moonY + 34);

        g.setColor(UiTheme.MUTED);
        g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        String title = state.phase().label() + "  |  " + Math.round(state.illuminatedFraction() * 100) + "% illuminated";
        g.drawString(title, 24, 28);
        g.drawString("SCHEMATIC — not to scale; circular orbit; inclination and eccentricity omitted",
                24, height - 19);
        g.dispose();
    }
}
