package org.freeplane.view.swing.map;

class TooltipPlacement {
	static final int MINIMUM_WIDTH = 300;

	final int width;
	final int shift;

	private TooltipPlacement(int width, int shift) {
		this.width = width;
		this.shift = shift;
	}

	static TooltipPlacement of(int preferredWidth, int anchorX, int desktopLeft, int desktopRight) {
		final int minimumWidth = Math.min(preferredWidth, MINIMUM_WIDTH);
		final int roomOnTheRight = desktopRight - anchorX;
		if (roomOnTheRight >= minimumWidth)
			return new TooltipPlacement(Math.min(preferredWidth, roomOnTheRight), 0);
		final int shift = Math.max(roomOnTheRight - minimumWidth, desktopLeft - anchorX);
		return new TooltipPlacement(Math.min(minimumWidth, desktopRight - (anchorX + shift)), shift);
	}
}
