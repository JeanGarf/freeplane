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
}
