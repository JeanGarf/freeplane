package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Point;

import org.freeplane.features.styles.mindmapmode.stylespane.StyleRowLayout.Target;
import org.junit.Test;

public class StyleRowLayoutTest {
	@Test
	public void placesMenuArrowAtRightEnd() {
		final StyleRowLayout layout = new StyleRowLayout(200, 24, 0);

		assertThat(layout.menu.x + layout.menu.width).isEqualTo(200 - StyleRowLayout.PADDING);
	}

	@Test
	public void identifiesMenuArrowAndStyleByPosition() {
		final StyleRowLayout layout = new StyleRowLayout(200, 24, 0);

		assertThat(layout.targetAt(new Point(layout.menu.x + 2, 12))).isEqualTo(Target.MENU);
		assertThat(layout.targetAt(new Point(20, 12))).isEqualTo(Target.STYLE);
	}

	@Test
	public void reservesRoomForAcceleratorBetweenStyleAndMenuArrow() {
		final StyleRowLayout layout = new StyleRowLayout(200, 24, 30);

		assertThat(layout.accelerator.width).isEqualTo(30);
		assertThat(layout.style.x + layout.style.width).isLessThan(layout.accelerator.x);
		assertThat(layout.accelerator.x + layout.accelerator.width + StyleRowLayout.GAP).isEqualTo(layout.menu.x);
	}

	@Test
	public void styleFillsAllRoomWhenThereIsNoAccelerator() {
		final StyleRowLayout layout = new StyleRowLayout(200, 24, 0);

		assertThat(layout.style.x + layout.style.width).isEqualTo(layout.menu.x - StyleRowLayout.GAP);
	}

	@Test
	public void clampsStyleWidthOnVeryNarrowRows() {
		final StyleRowLayout layout = new StyleRowLayout(20, 24, 40);

		assertThat(layout.style.width).isZero();
	}
}
