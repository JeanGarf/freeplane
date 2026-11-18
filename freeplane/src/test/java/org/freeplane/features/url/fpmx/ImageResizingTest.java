package org.freeplane.features.url.fpmx;

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

	@Test
	public void widthForHeightKeepsAspectRatio() {
		assertThat(ImageResizing.widthForHeight(200, 100, 50)).isEqualTo(100);
		assertThat(ImageResizing.widthForHeight(0, 0, 50)).isEqualTo(1);
	}

	@Test
	public void percentIsRelativeToTheReferenceWidth() {
		assertThat(ImageResizing.percentOf(200, 100)).isEqualTo(50);
		assertThat(ImageResizing.percentOf(200, 300)).isEqualTo(150);
		assertThat(ImageResizing.percentOf(0, 300)).isEqualTo(100);
	}
}
