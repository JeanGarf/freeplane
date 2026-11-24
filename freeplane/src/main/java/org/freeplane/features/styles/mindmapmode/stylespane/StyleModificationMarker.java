package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class StyleModificationMarker {
	public static final int SIZE = 14;
	private static final int MAX_DISPLAYED = 9;
	private static final Color COLOR = new Color(0xD9, 0x7A, 0x00);

	public static String label(int count) {
		return count > MAX_DISPLAYED ? MAX_DISPLAYED + "+" : String.valueOf(count);
	}

	public static Rectangle boundsIn(Rectangle style) {
		return new Rectangle(style.x + style.width - SIZE + SIZE / 3, style.y - SIZE / 3, SIZE, SIZE);
	}

	public static void paint(Graphics2D graphics, Rectangle marker, int count, Font baseFont) {
		final Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(COLOR);
			g.fillOval(marker.x, marker.y, marker.width, marker.height);
			g.setColor(Color.WHITE);
			g.setFont(baseFont.deriveFont(Font.BOLD, marker.height - 5f));
			final FontMetrics metrics = g.getFontMetrics();
			final String label = label(count);
			g.drawString(label, marker.x + (marker.width - metrics.stringWidth(label)) / 2,
			    marker.y + (marker.height - metrics.getHeight()) / 2 + metrics.getAscent());
		}
		finally {
			g.dispose();
		}
	}
}
