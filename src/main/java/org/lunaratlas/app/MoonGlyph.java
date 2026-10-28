package org.lunaratlas.app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

final class MoonGlyph {
    private MoonGlyph() {
    }

    static BufferedImage render(int diameter, double ageDays) {
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        double phase = Math.max(0, Math.min(1, ageDays / 29.530588853)) * Math.PI * 2;
        double sunX = Math.sin(phase);
        double sunZ = -Math.cos(phase);
        for (int y = 0; y < diameter; y++) {
            double ny = (2.0 * y / (diameter - 1)) - 1;
            for (int x = 0; x < diameter; x++) {
                double nx = (2.0 * x / (diameter - 1)) - 1;
                double radiusSquared = nx * nx + ny * ny;
                if (radiusSquared <= 1) {
                    double nz = Math.sqrt(1 - radiusSquared);
                    boolean lit = nx * sunX + nz * sunZ > 0;
                    double shade = 0.78 + 0.22 * nz;
                    Color color = lit
                            ? new Color((int) (242 * shade), (int) (233 * shade), (int) (207 * shade))
                            : new Color(29, 42, 59);
                    image.setRGB(x, y, color.getRGB());
                }
            }
        }
        return image;
    }
}
