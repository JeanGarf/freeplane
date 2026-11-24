package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Rectangle;

import org.junit.Test;

public class StyleModificationMarkerTest {
	@Test
	public void showsExactCountUpToNine() {
		assertThat(StyleModificationMarker.label(1)).isEqualTo("1");
		assertThat(StyleModificationMarker.label(9)).isEqualTo("9");
	}

	@Test
	public void capsLargeCounts() {
		assertThat(StyleModificationMarker.label(10)).isEqualTo("9+");
		assertThat(StyleModificationMarker.label(120)).isEqualTo("9+");
	}

	@Test
	public void placesMarkerOnTopRightCornerOfStyle() {
		final Rectangle style = new Rectangle(30, 6, 100, 24);

		final Rectangle marker = StyleModificationMarker.boundsIn(style);

		assertThat(marker.width).isEqualTo(StyleModificationMarker.SIZE);
		assertThat(marker.y).isLessThan(style.y + style.height / 2);
		assertThat(marker.x + marker.width / 2).isBetween(style.x + style.width - 10, style.x + style.width + 10);
	}
}
