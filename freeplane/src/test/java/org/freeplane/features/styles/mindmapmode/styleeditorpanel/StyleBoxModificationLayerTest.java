package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Rectangle;

import org.junit.Test;

public class StyleBoxModificationLayerTest {
	@Test
	public void markerStaysInsideBoxLeftOfArrow() {
		final Rectangle marker = StyleBoxModificationLayer.markerBounds(200, 24);

		assertThat(new Rectangle(0, 0, 200, 24).contains(marker)).isTrue();
		assertThat(marker.x + marker.width).isLessThan(200 - 10);
	}

	@Test
	public void markerIsCenteredVertically() {
		final Rectangle marker = StyleBoxModificationLayer.markerBounds(200, 24);

		assertThat(marker.y * 2 + marker.height).isEqualTo(24);
	}
}
