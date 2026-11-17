package org.freeplane.features.text.mindmapmode;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

enum ImageHandle {
	NORTH_WEST(-1, -1, Cursor.NW_RESIZE_CURSOR),
	NORTH(0, -1, Cursor.N_RESIZE_CURSOR),
	NORTH_EAST(1, -1, Cursor.NE_RESIZE_CURSOR),
	EAST(1, 0, Cursor.E_RESIZE_CURSOR),
	SOUTH_EAST(1, 1, Cursor.SE_RESIZE_CURSOR),
	SOUTH(0, 1, Cursor.S_RESIZE_CURSOR),
	SOUTH_WEST(-1, 1, Cursor.SW_RESIZE_CURSOR),
	WEST(-1, 0, Cursor.W_RESIZE_CURSOR);

	static final int MINIMUM_SIZE = 8;

	final int xFactor;
	final int yFactor;
	final int cursorType;

	private ImageHandle(int xFactor, int yFactor, int cursorType) {
		this.xFactor = xFactor;
		this.yFactor = yFactor;
		this.cursorType = cursorType;
	}

	Point centerOn(Rectangle image) {
		final int x = xFactor < 0 ? image.x : xFactor > 0 ? image.x + image.width : image.x + image.width / 2;
		final int y = yFactor < 0 ? image.y : yFactor > 0 ? image.y + image.height : image.y + image.height / 2;
		return new Point(x, y);
	}

	Dimension resized(Dimension start, int dx, int dy) {
		final float ratio = start.height / (float) Math.max(1, start.width);
		if (xFactor != 0) {
			final int width = Math.max(MINIMUM_SIZE, start.width + xFactor * dx);
			return new Dimension(width, Math.max(1, Math.round(width * ratio)));
		}
		final int height = Math.max(MINIMUM_SIZE, start.height + yFactor * dy);
		return new Dimension(Math.max(1, Math.round(height / ratio)), height);
	}

	Rectangle anchored(Rectangle start, Dimension size) {
		final int x = xFactor < 0 ? start.x + start.width - size.width : start.x;
		final int y = yFactor < 0 ? start.y + start.height - size.height : start.y;
		return new Rectangle(x, y, size.width, size.height);
	}
}
