package org.freeplane.features.url.mmx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageDataUri {
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;
		final BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null),
		    BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = buffered.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		return buffered;
	}

	public static String pngDataUri(Image image) throws IOException {
		final BufferedImage buffered = toBufferedImage(image);
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		if (!ImageIO.write(buffered, "png", bytes))
			throw new IOException("No PNG writer available");
		return EmbeddedImages.dataUri(bytes.toByteArray(), "png");
	}

	public static String imageHtml(Image image) throws IOException {
		final BufferedImage buffered = toBufferedImage(image);
		return "<img src=\"" + pngDataUri(buffered) + "\" width=\"" + buffered.getWidth() + "\" height=\""
		        + buffered.getHeight() + "\">";
	}
}
