package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Color;

import org.freeplane.features.nodestyle.NodeStyleShape;

public class StyleLook {
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
}
