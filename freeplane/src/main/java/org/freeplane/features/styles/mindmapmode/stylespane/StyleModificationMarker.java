package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Rectangle;

class StyleModificationMarker {
	static final int SIZE = 14;
	private static final int MAX_DISPLAYED = 9;

	static String label(int count) {
		return count > MAX_DISPLAYED ? MAX_DISPLAYED + "+" : String.valueOf(count);
	}

	static Rectangle boundsIn(Rectangle style) {
		return new Rectangle(style.x + style.width - SIZE + SIZE / 3, style.y - SIZE / 3, SIZE, SIZE);
	}
}
