package org.freeplane.features.clipboard.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Point;
import java.awt.Rectangle;

import org.junit.Test;

public class SnipSelectionTest {
	private static final Rectangle SCREEN = new Rectangle(0, 0, 1000, 800);

	@Test
	public void dragInAnyDirectionGivesTheSameRectangle() {
		final Rectangle expected = new Rectangle(100, 200, 300, 50);
		assertThat(SnipSelection.between(new Point(100, 200), new Point(400, 250), SCREEN)).isEqualTo(expected);
		assertThat(SnipSelection.between(new Point(400, 250), new Point(100, 200), SCREEN)).isEqualTo(expected);
		assertThat(SnipSelection.between(new Point(400, 200), new Point(100, 250), SCREEN)).isEqualTo(expected);
	}

	@Test
	public void selectionIsClampedToTheCapturedArea() {
		assertThat(SnipSelection.between(new Point(-50, -20), new Point(1200, 900), SCREEN)).isEqualTo(SCREEN);
	}

	@Test
	public void selectionWorksWhenTheVirtualScreenStartsAtANegativeOrigin() {
		final Rectangle twoScreens = new Rectangle(-1920, 0, 3840, 1080);
		assertThat(SnipSelection.between(new Point(-100, 10), new Point(100, 60), twoScreens))
		    .isEqualTo(new Rectangle(-100, 10, 200, 50));
	}

	@Test
	public void tinySelectionsAreNotUsable() {
		assertThat(SnipSelection.isUsable(new Rectangle(0, 0, 2, 50))).isFalse();
		assertThat(SnipSelection.isUsable(new Rectangle(0, 0, 3, 3))).isTrue();
		assertThat(SnipSelection.isUsable(null)).isFalse();
	}

	@Test
	public void screensAreMergedIntoOneVirtualArea() {
		assertThat(SnipSelection.unionOf(new Rectangle(0, 0, 1920, 1080), new Rectangle(-1280, 100, 1280, 1024)))
		    .isEqualTo(new Rectangle(-1280, 0, 3200, 1124));
		assertThat(SnipSelection.unionOf()).isEqualTo(new Rectangle());
	}
}
