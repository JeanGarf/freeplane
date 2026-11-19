package org.freeplane.features.clipboard.mindmapmode;

import java.awt.Point;
import java.awt.Rectangle;

class SnipSelection {
	static final int MINIMUM_SIZE = 3;

	static Rectangle between(Point start, Point end, Rectangle bounds) {
		final int left = clamp(Math.min(start.x, end.x), bounds.x, bounds.x + bounds.width);
		final int right = clamp(Math.max(start.x, end.x), bounds.x, bounds.x + bounds.width);
		final int top = clamp(Math.min(start.y, end.y), bounds.y, bounds.y + bounds.height);
		final int bottom = clamp(Math.max(start.y, end.y), bounds.y, bounds.y + bounds.height);
		return new Rectangle(left, top, right - left, bottom - top);
	}

	static boolean isUsable(Rectangle selection) {
		return selection != null && selection.width >= MINIMUM_SIZE && selection.height >= MINIMUM_SIZE;
	}

	static Rectangle unionOf(Rectangle... areas) {
		Rectangle union = null;
		for (final Rectangle area : areas)
			union = union == null ? new Rectangle(area) : union.union(area);
		return union == null ? new Rectangle() : union;
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}
}
