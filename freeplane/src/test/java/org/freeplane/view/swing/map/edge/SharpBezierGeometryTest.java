package org.freeplane.view.swing.map.edge;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.junit.Test;

public class SharpBezierGeometryTest {
	private static SharpBezierGeometry geometry(float startX, float startY, float endX, float endY, float startHalf,
	                                            float endHalf) {
		final Point2D.Float start = new Point2D.Float(startX, startY);
		return new SharpBezierGeometry(start, start, new Point2D.Float(endX, endY), -1, 0, startHalf, endHalf, 12f);
	}

	private static int thicknessAt(GeneralPath shape, float x, float fromY, float toY) {
		int count = 0;
		for (float y = fromY; y <= toY; y += 0.25f)
			if (shape.contains(x, y))
				count++;
		return Math.round(count * 0.25f);
	}

	@Test
	public void taperNeverRegrowsOnStraightRun() {
		final GeneralPath shape = geometry(0, 0, 200, 0, 20f, 0.5f).shape();

		int previous = Integer.MAX_VALUE;
		for (int x = 2; x < 198; x += 4) {
			final int thickness = thicknessAt(shape, x, -30, 30);
			assertThat(thickness).isLessThanOrEqualTo(previous);
			previous = thickness;
		}
	}

	@Test
	public void startsWithFullWidthAndEndsWithBorderWidth() {
		final GeneralPath shape = geometry(0, 0, 200, 0, 20f, 0.5f).shape();

		assertThat(thicknessAt(shape, 1, -30, 30)).isBetween(38, 40);
		assertThat(thicknessAt(shape, 199, -30, 30)).isLessThanOrEqualTo(2);
	}

	@Test
	public void bordersStayNearStraightChordBeforeBendingAtTheEnd() {
		final GeneralPath shape = geometry(0, 0, 200, 200, 20f, 0.5f).shape();

		assertThat(shape.contains(60, 60)).isTrue();
		assertThat(shape.contains(60, 100)).isFalse();
		assertThat(shape.contains(120, 120)).isTrue();
	}

	@Test
	public void arrivesHorizontallyAtChild() {
		final GeneralPath shape = geometry(0, 0, 200, 200, 20f, 0.5f).shape();

		assertThat(thicknessAt(shape, 190, 150, 250)).isLessThanOrEqualTo(3);
		assertThat(shape.contains(190, 185)).isFalse();
	}

	@Test
	public void centerLineEndsAtTarget() {
		final Point2D end = geometry(0, 0, 200, 50, 20f, 0.5f).centerLine().getCurrentPoint();

		assertThat(end.getX()).isEqualTo(200);
		assertThat(end.getY()).isEqualTo(50);
	}
}
