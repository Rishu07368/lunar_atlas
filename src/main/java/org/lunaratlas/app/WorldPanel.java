package org.lunaratlas.app;

import org.lunaratlas.astronomy.GeoLocation;
import org.lunaratlas.astronomy.LocationCatalog;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

final class WorldPanel extends JPanel {
    private static final int LEFT = 54;
    private static final int RIGHT = 24;
    private static final int TOP = 32;
    private static final int BOTTOM = 45;
    private final Consumer<GeoLocation> selectLocation;
    private GeoLocation selected;

    WorldPanel(Consumer<GeoLocation> selectLocation) {
        this.selectLocation = selectLocation;
        setBackground(UiTheme.BACKGROUND);
        setToolTipText("Schematic graticule. Click a labeled city to select it.");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                GeoLocation nearest = null;
                double nearestDistance = 18 * 18;
                for (GeoLocation location : LocationCatalog.locations()) {
                    double dx = event.getX() - xFor(location);
                    double dy = event.getY() - yFor(location);
                    double distance = dx * dx + dy * dy;
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = location;
                    }
                }
                if (nearest != null) {
                    selectLocation.accept(nearest);
                }
            }
        });
    }

    void setSelected(GeoLocation location) {
        selected = location;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int mapWidth = Math.max(1, getWidth() - LEFT - RIGHT);
        int mapHeight = Math.max(1, getHeight() - TOP - BOTTOM);
        g.setColor(new Color(29, 48, 69));
        g.fillRoundRect(LEFT, TOP, mapWidth, mapHeight, 12, 12);
        g.setColor(new Color(71, 96, 123));
        g.setStroke(new BasicStroke(1f));
        for (int longitude = -180; longitude <= 180; longitude += 30) {
            int x = LEFT + (longitude + 180) * mapWidth / 360;
            g.drawLine(x, TOP, x, TOP + mapHeight);
            g.drawString(Integer.toString(longitude), x - 12, TOP + mapHeight + 20);
        }
        for (int latitude = -60; latitude <= 60; latitude += 30) {
            int y = TOP + (90 - latitude) * mapHeight / 180;
            g.drawLine(LEFT, y, LEFT + mapWidth, y);
            g.drawString(Integer.toString(latitude), 15, y + 4);
        }
        boolean selectedIsCatalogued = false;
        for (GeoLocation location : LocationCatalog.locations()) {
            int x = xFor(location);
            int y = yFor(location);
            boolean active = selected != null && selected.id().equals(location.id());
            selectedIsCatalogued |= active;
            g.setColor(active ? UiTheme.GOLD : UiTheme.ACCENT);
            g.fillOval(x - (active ? 6 : 4), y - (active ? 6 : 4),
                    active ? 12 : 8, active ? 12 : 8);
            g.setColor(UiTheme.TEXT);
            g.drawString(location.name().split(",")[0], x + 7, y - 6);
        }
        if (selected != null && !selectedIsCatalogued) {
            int x = xFor(selected);
            int y = yFor(selected);
            g.setColor(UiTheme.GOLD);
            g.fillOval(x - 6, y - 6, 12, 12);
            g.setColor(UiTheme.TEXT);
            g.drawString(selected.name(), x + 8, y - 6);
        }
        g.setColor(UiTheme.MUTED);
        g.drawString("ILLUSTRATIVE EQUIRECTANGULAR GRATICULE — no coastlines or event paths",
                LEFT, getHeight() - 12);
        g.dispose();
    }

    private int xFor(GeoLocation location) {
        return LEFT + (int) ((location.longitudeDegrees() + 180) / 360
                * (getWidth() - LEFT - RIGHT));
    }

    private int yFor(GeoLocation location) {
        return TOP + (int) ((90 - location.latitudeDegrees()) / 180
                * (getHeight() - TOP - BOTTOM));
    }
}
