package org.freeplane.features.url.mmx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ImageResizingTest {
	@Test
	public void heightKeepsAspectRatio() {
		assertThat(ImageResizing.heightFor(200, 100, 100)).isEqualTo(50);
		assertThat(ImageResizing.heightFor(120, 60, 360)).isEqualTo(180);
	}

	@Test
	public void heightIsNeverBelowOnePixel() {
		assertThat(ImageResizing.heightFor(1000, 10, 1)).isEqualTo(1);
		assertThat(ImageResizing.heightFor(0, 0, 50)).isEqualTo(1);
	}

	@Test
	public void widthForPercentScalesOriginal() {
		assertThat(ImageResizing.widthForPercent(200, 50)).isEqualTo(100);
		assertThat(ImageResizing.widthForPercent(3, 25)).isEqualTo(1);
	}
}
