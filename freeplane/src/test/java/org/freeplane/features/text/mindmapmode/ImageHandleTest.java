package org.freeplane.features.text.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.junit.Test;

public class ImageHandleTest {
	private static final Dimension START = new Dimension(200, 100);

	@Test
	public void eastHandleGrowsWidthAndKeepsRatio() {
		assertThat(ImageHandle.EAST.resized(START, 50, 999)).isEqualTo(new Dimension(250, 125));
	}

	@Test
	public void westHandleGrowsWhenDraggedLeft() {
		assertThat(ImageHandle.WEST.resized(START, -40, 0)).isEqualTo(new Dimension(240, 120));
	}

	@Test
	public void southHandleDrivesHeightAndKeepsRatio() {
		assertThat(ImageHandle.SOUTH.resized(START, 999, 50)).isEqualTo(new Dimension(300, 150));
	}

	@Test
	public void cornerHandleUsesHorizontalMovement() {
		assertThat(ImageHandle.SOUTH_EAST.resized(START, -100, 77)).isEqualTo(new Dimension(100, 50));
	}

	@Test
	public void sizeNeverDropsBelowMinimum() {
		assertThat(ImageHandle.EAST.resized(START, -1000, 0).width).isEqualTo(ImageHandle.MINIMUM_SIZE);
	}

	@Test
	public void northWestPreviewKeepsOppositeCornerFixed() {
		final Rectangle start = new Rectangle(10, 20, 200, 100);
		final Rectangle preview = ImageHandle.NORTH_WEST.anchored(start, new Dimension(300, 150));
		assertThat(preview).isEqualTo(new Rectangle(-90, -30, 300, 150));
	}

	@Test
	public void handleCentersAreOnTheImageEdges() {
		final Rectangle image = new Rectangle(0, 0, 100, 40);
		assertThat(ImageHandle.NORTH.centerOn(image)).isEqualTo(new Point(50, 0));
		assertThat(ImageHandle.SOUTH_EAST.centerOn(image)).isEqualTo(new Point(100, 40));
		assertThat(ImageHandle.WEST.centerOn(image)).isEqualTo(new Point(0, 20));
	}
}
