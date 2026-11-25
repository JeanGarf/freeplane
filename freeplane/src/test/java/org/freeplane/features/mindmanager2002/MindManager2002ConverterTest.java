package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.Test;

public class MindManager2002ConverterTest {
	@Test
	public void encodesImageAsPngDataUri() throws Exception {
		BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
		image.setRGB(1, 1, 0xff0000);

		String uri = MindManager2002Converter.pngDataUri(image);

		assertThat(uri).startsWith("data:image/png;base64,");
		byte[] png = Base64.getDecoder().decode(uri.substring("data:image/png;base64,".length()));
		BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(png));
		assertThat(decoded.getWidth()).isEqualTo(3);
		assertThat(decoded.getHeight()).isEqualTo(2);
		assertThat(decoded.getRGB(1, 1) & 0xffffff).isEqualTo(0xff0000);
	}
}
