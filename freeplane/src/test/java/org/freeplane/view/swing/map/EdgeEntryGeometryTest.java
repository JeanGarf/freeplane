package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.freeplane.features.edge.EdgeAttachment;
import org.junit.Test;

public class EdgeEntryGeometryTest {
	@Test
	public void bottomIsLastRowInsideBorder() {
		assertThat(EdgeEntryGeometry.entryY(EdgeAttachment.bottom, 30, 2, 0)).isEqualTo(28);
	}

	@Test
	public void topIsFirstRowInsideBorder() {
		assertThat(EdgeEntryGeometry.entryY(EdgeAttachment.top, 30, 2, 0)).isEqualTo(1);
	}

	@Test
	public void middleIsHalfOfHeight() {
		assertThat(EdgeEntryGeometry.entryY(EdgeAttachment.middle, 30, 2, 0)).isEqualTo(15);
	}

	@Test
	public void marginMovesTopAndBottomInwards() {
		assertThat(EdgeEntryGeometry.entryY(EdgeAttachment.top, 30, 0, 4)).isEqualTo(4);
		assertThat(EdgeEntryGeometry.entryY(EdgeAttachment.bottom, 30, 0, 4)).isEqualTo(25);
	}

	@Test
	public void withoutOutlineKeepsFallbackX() {
		assertThat(EdgeEntryGeometry.entryX(null, true, 10, 100, 0)).isZero();
		assertThat(EdgeEntryGeometry.entryX(null, false, 10, 100, 99)).isEqualTo(99);
	}

	@Test
	public void roundedCornerPullsEntryPointInsideAtTheBottom() {
		final RoundRectangle2D outline = new RoundRectangle2D.Double(0, 0, 100, 30, 20, 20);

		final int left = EdgeEntryGeometry.entryX(outline, true, 29, 100, 0);
		final int right = EdgeEntryGeometry.entryX(outline, false, 29, 100, 99);

		assertThat(left).isGreaterThan(0);
		assertThat(right).isLessThan(99);
	}

	@Test
	public void entryPointOnStraightSideStaysAtTheBorder() {
		final RoundRectangle2D outline = new RoundRectangle2D.Double(0, 0, 100, 30, 20, 20);

		assertThat(EdgeEntryGeometry.entryX(outline, true, 15, 100, 5)).isZero();
		assertThat(EdgeEntryGeometry.entryX(outline, false, 15, 100, 5)).isEqualTo(99);
	}

	@Test
	public void ellipseEntryPointMovesTowardsTheCenterNearTheBottom() {
		final Ellipse2D outline = new Ellipse2D.Double(0, 0, 100, 40);

		assertThat(EdgeEntryGeometry.entryX(outline, true, 35, 100, 0)).isGreaterThan(10);
	}

	@Test
	public void fallsBackWhenOutlineDoesNotContainTheRow() {
		final Ellipse2D outline = new Ellipse2D.Double(0, 0, 100, 40);

		assertThat(EdgeEntryGeometry.entryX(outline, true, 200, 100, 7)).isEqualTo(7);
	}
}
