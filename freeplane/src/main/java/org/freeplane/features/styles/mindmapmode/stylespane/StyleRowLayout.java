package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.Point;
import java.awt.Rectangle;

class StyleRowLayout {
	enum Target {
		STYLE, MENU
	}

	static final int PADDING = 4;
	static final int MENU_SIZE = 18;
	static final int GAP = 6;
	static final int ROW_INSET = 8;
	static final int EDGE_WIDTH = 20;
	static final int ROW_VERTICAL_PADDING = 12;

	final Rectangle edge;
	final Rectangle style;
	final Rectangle accelerator;
	final Rectangle menu;

	StyleRowLayout(int width, int height, int acceleratorWidth) {
		menu = new Rectangle(width - ROW_INSET - MENU_SIZE, (height - MENU_SIZE) / 2, MENU_SIZE, MENU_SIZE);
		final int acceleratorGap = acceleratorWidth > 0 ? GAP : 0;
		accelerator = new Rectangle(menu.x - GAP - acceleratorWidth, 0, acceleratorWidth, height);
		final int styleRight = accelerator.x - acceleratorGap;
		edge = new Rectangle(ROW_INSET, 0, EDGE_WIDTH, height);
		final int styleLeft = edge.x + edge.width;
		style = new Rectangle(styleLeft, ROW_VERTICAL_PADDING / 2, Math.max(0, styleRight - styleLeft), height - ROW_VERTICAL_PADDING);
	}

	Target targetAt(Point point) {
		return menu.contains(point) ? Target.MENU : Target.STYLE;
	}
}
