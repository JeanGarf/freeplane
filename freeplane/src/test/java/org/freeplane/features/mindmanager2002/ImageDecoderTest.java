package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

public class ImageDecoderTest {
	@Test
	public void decodesPngWithTrailingData() throws Exception {
		BufferedImage source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
		source.setRGB(1, 1, 0x00ff00);
		ByteArrayOutputStream png = new ByteArrayOutputStream();
		ImageIO.write(source, "png", png);
		png.write("TRASP_INFO_M45".getBytes("US-ASCII"));

		BufferedImage image = ImageDecoder.decode(png.toByteArray());

		assertThat(image.getWidth()).isEqualTo(3);
		assertThat(image.getRGB(1, 1) & 0xffffff).isEqualTo(0x00ff00);
	}

	@Test
	public void decodesJpegAndBmp() throws Exception {
		BufferedImage source = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		for (String format : new String[] { "jpg", "bmp" }) {
			ByteArrayOutputStream encoded = new ByteArrayOutputStream();
			ImageIO.write(source, format, encoded);

			assertThat(ImageDecoder.decode(encoded.toByteArray()).getWidth()).as(format).isEqualTo(8);
		}
	}

	@Test
	public void rejectsUnknownData() {
		assertThatThrownBy(() -> ImageDecoder.decode(new byte[] { 1, 2, 3, 4 })).isInstanceOf(IOException.class);
	}
}
