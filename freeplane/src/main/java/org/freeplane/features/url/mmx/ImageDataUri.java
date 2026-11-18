package org.freeplane.features.url.mmx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

	public static BufferedImage limitedToWidth(Image image, int maximumWidth) {
		final BufferedImage buffered = toBufferedImage(image);
		if (buffered.getWidth() <= maximumWidth)
			return buffered;
		final int height = Math.max(1, Math.round((float) buffered.getHeight() * maximumWidth / buffered.getWidth()));
		final BufferedImage scaled = new BufferedImage(maximumWidth, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = scaled.createGraphics();
		graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
		    java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.drawImage(buffered, 0, 0, maximumWidth, height, null);
		graphics.dispose();
		return scaled;
	}

	public static String pngDataUri(Image image) throws IOException {
		final BufferedImage buffered = toBufferedImage(image);
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		if (!ImageIO.write(buffered, "png", bytes))
			throw new IOException("No PNG writer available");
		return EmbeddedImages.dataUri(bytes.toByteArray(), "png");
	}

	public static String htmlForFile(File file) {
		try {
			final BufferedImage image = ImageIO.read(file);
			if (image == null)
				return null;
			final byte[] content = Files.readAllBytes(file.toPath());
			final String extension = EmbeddedImages.extensionOfFileName(file.getName());
			final String source = extension != null ? EmbeddedImages.dataUri(content, extension) : pngDataUri(image);
			return "<img src=\"" + source + "\" width=\"" + image.getWidth() + "\" height=\"" + image.getHeight()
			        + "\">";
		}
		catch (IOException e) {
			return null;
		}
	}

	public static String imageHtml(Image image) throws IOException {
		final BufferedImage buffered = toBufferedImage(image);
		return "<img src=\"" + pngDataUri(buffered) + "\" width=\"" + buffered.getWidth() + "\" height=\""
		        + buffered.getHeight() + "\">";
	}
}
