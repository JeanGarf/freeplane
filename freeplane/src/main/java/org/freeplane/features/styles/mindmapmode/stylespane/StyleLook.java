package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.freeplane.core.ui.components.ButtonLook;

import org.freeplane.features.nodestyle.NodeStyleShape;

public class StyleLook implements ButtonLook {
	public final Color background;
	public final Color foreground;
	public final Color border;
	public final String fontFamily;
	public final boolean bold;
	public final boolean italic;
	public final NodeStyleShape shape;

	public StyleLook(Color background, Color foreground, Color border, String fontFamily, boolean bold,
	                 boolean italic, NodeStyleShape shape) {
		this.background = background;
		this.foreground = foreground;
		this.border = border;
		this.fontFamily = fontFamily;
		this.bold = bold;
		this.italic = italic;
		this.shape = shape;
	}

	@Override
	public Color foreground() {
		return foreground;
	}

	@Override
	public Font deriveFont(Font base) {
		return StyleLookPainter.fontFor(this, base);
	}

	@Override
	public void paintBackground(Graphics2D g, int x, int y, int width, int height) {
		StyleLookPainter.paintBackground(g, this, x, y, width, height);
	}
}
