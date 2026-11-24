package org.freeplane.view.swing.map.edge;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.junit.Test;

public class SharpLinearGeometryTest {
	private static Path2D shape(float endX, float endY, float halfWidth, float nodeHalfWidth, float nodeHalfHeight) {
		return new SharpLinearGeometry(new Point2D.Float(0, 0), new Point2D.Float(endX, endY), halfWidth,
		    nodeHalfWidth, nodeHalfHeight).shape();
	}

	@Test
	public void baseIsCenteredOnStartAcrossTheChord() {
		final Path2D shape = shape(200, 0, 20f, 1000f, 1000f);

		assertThat(shape.contains(1, 19)).isTrue();
		assertThat(shape.contains(1, -19)).isTrue();
		assertThat(shape.contains(1, 25)).isFalse();
	}

	@Test
	public void narrowsToPointAtEnd() {
		final Path2D shape = shape(200, 0, 20f, 1000f, 1000f);

		assertThat(shape.contains(199, 3)).isFalse();
		assertThat(shape.contains(199, 0)).isTrue();
	}

	@Test
	public void baseCornersStayInsideSourceNode() {
		final Path2D shape = shape(300, 300, 40f, 30f, 10f);

		assertThat(shape.getBounds2D().getMinX()).isGreaterThanOrEqualTo(-30.5);
		assertThat(shape.getBounds2D().getMaxY()).isGreaterThanOrEqualTo(299);
		assertThat(shape.getBounds2D().getMaxX()).isGreaterThanOrEqualTo(299);
		assertThat(shape.getBounds2D().getMinY()).isGreaterThanOrEqualTo(-10.5);
	}
}
