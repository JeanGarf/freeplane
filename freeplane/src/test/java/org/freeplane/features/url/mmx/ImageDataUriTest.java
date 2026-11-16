package org.freeplane.features.url.mmx;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ImageDataUriTest {
	@Test
	public void pastedImageSurvivesExtractionAndRestoration() throws Exception {
		final BufferedImage image = new BufferedImage(7, 5, BufferedImage.TYPE_INT_ARGB);
		final String html = ImageDataUri.imageHtml(image);
		assertThat(html).contains("width=\"7\"").contains("height=\"5\"");
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(html);
		assertThat(extraction.images).hasSize(1);
		final byte[] png = extraction.images.values().iterator().next();
		final BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(png));
		assertThat(decoded.getWidth()).isEqualTo(7);
		assertThat(EmbeddedImages.restore(extraction.text, extraction.images)).isEqualTo(html);
	}

	@Test
	public void imageFileIsEmbeddedWithItsOwnBytesAndSize() throws Exception {
		final java.io.File file = java.io.File.createTempFile("embedded", ".png");
		try {
			ImageIO.write(new BufferedImage(9, 4, BufferedImage.TYPE_INT_RGB), "png", file);
			final String html = ImageDataUri.htmlForFile(file);
			assertThat(html).contains("data:image/png;base64,").contains("width=\"9\"").contains("height=\"4\"");
			final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(html);
			assertThat(extraction.images.values().iterator().next())
			    .isEqualTo(java.nio.file.Files.readAllBytes(file.toPath()));
		}
		finally {
			file.delete();
		}
	}

	@Test
	public void nonImageFileGivesNoHtml() throws Exception {
		final java.io.File file = java.io.File.createTempFile("embedded", ".txt");
		try {
			assertThat(ImageDataUri.htmlForFile(file)).isNull();
		}
		finally {
			file.delete();
		}
	}
}
