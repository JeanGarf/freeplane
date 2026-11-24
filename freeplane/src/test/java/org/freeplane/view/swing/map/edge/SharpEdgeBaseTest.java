package org.freeplane.view.swing.map.edge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SharpEdgeBaseTest {
	@Test
	public void endsAsPointWhenBorderIsOnePixel() {
		assertThat(SharpEdgeBase.endHalfWidth(1f)).isZero();
	}

	@Test
	public void compensatesOutlineStrokeForThickerBorders() {
		assertThat(SharpEdgeBase.endHalfWidth(2f)).isEqualTo(0.5f);
		assertThat(SharpEdgeBase.endHalfWidth(5f)).isEqualTo(2f);
	}

	@Test
	public void neverGoesNegative() {
		assertThat(SharpEdgeBase.endHalfWidth(0f)).isZero();
	}
}
