package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TooltipPlacementTest {
	private static final int LEFT = 0;
	private static final int RIGHT = 1920;

	@Test
	public void smallContentKeepsItsWidthAndPosition() {
		final TooltipPlacement placement = TooltipPlacement.of(250, 800, LEFT, RIGHT);
		assertThat(placement.width).isEqualTo(250);
		assertThat(placement.shift).isZero();
	}

	@Test
	public void wideContentTakesAllTheRoomOnTheRightOfTheNode() {
		final TooltipPlacement placement = TooltipPlacement.of(5000, 800, LEFT, RIGHT);
		assertThat(placement.width).isEqualTo(1120);
		assertThat(placement.shift).isZero();
	}

	@Test
	public void contentFittingInTheRoomIsNotStretched() {
		final TooltipPlacement placement = TooltipPlacement.of(600, 800, LEFT, RIGHT);
		assertThat(placement.width).isEqualTo(600);
	}

	@Test
	public void nodeNearTheRightEdgeShiftsTheTooltipLeftToKeepTheMinimumWidth() {
		final TooltipPlacement placement = TooltipPlacement.of(5000, 1800, LEFT, RIGHT);
		assertThat(placement.width).isEqualTo(TooltipPlacement.MINIMUM_WIDTH);
		assertThat(placement.shift).isEqualTo(120 - 300);
		assertThat(1800 + placement.shift + placement.width).isEqualTo(RIGHT);
	}

	@Test
	public void smallContentNearTheRightEdgeOnlyShiftsByWhatItNeeds() {
		final TooltipPlacement placement = TooltipPlacement.of(150, 1850, LEFT, RIGHT);
		assertThat(placement.width).isEqualTo(150);
		assertThat(1850 + placement.shift + placement.width).isEqualTo(RIGHT);
	}

	@Test
	public void shiftNeverGoesBeyondTheLeftEdgeOfTheScreen() {
		final TooltipPlacement placement = TooltipPlacement.of(5000, 100, LEFT, 150);
		assertThat(100 + placement.shift).isGreaterThanOrEqualTo(LEFT);
		assertThat(placement.width).isEqualTo(150);
	}

	@Test
	public void screensNotStartingAtZeroAreSupported() {
		final TooltipPlacement placement = TooltipPlacement.of(5000, 3700, -1920, 1920 + 1920);
		assertThat(placement.width).isEqualTo(TooltipPlacement.MINIMUM_WIDTH);
		assertThat(3700 + placement.shift + placement.width).isEqualTo(3840);
	}
}
