package org.freeplane.view.swing.map.edge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ControlDistanceTest {
	@Test
	public void keepsMinimumForShortSpan() {
		assertThat(ControlDistance.of(12f, 10f)).isEqualTo(12f);
	}

	@Test
	public void growsWithHalfOfLongSpan() {
		assertThat(ControlDistance.of(12f, 100f)).isEqualTo(50f);
	}

	@Test
	public void ignoresSpanDirection() {
		assertThat(ControlDistance.of(12f, -100f)).isEqualTo(50f);
	}
}
