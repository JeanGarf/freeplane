package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Color;

import org.freeplane.api.Dash;
import org.freeplane.features.edge.EdgeStyle;

public class StyleEdge {
	public final Color color;
	public final int width;
	public final Dash dash;
	public final EdgeStyle style;

	public StyleEdge(Color color, int width, Dash dash, EdgeStyle style) {
		this.color = color;
		this.width = width;
		this.dash = dash;
		this.style = style;
	}
}
