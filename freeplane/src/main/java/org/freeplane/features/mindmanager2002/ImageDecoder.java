package org.freeplane.features.mindmanager2002;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

class ImageDecoder {
	private static final int EMF_SIGNATURE_OFFSET = 40;
	private static final int EMF_SIGNATURE = 0x464D4520;

	static BufferedImage decode(byte[] data) throws IOException {
		if (isEmf(data))
			return EmfRenderer.render(data);
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
		if (image == null)
			throw new IOException("Unsupported image format");
		return image;
	}

	private static boolean isEmf(byte[] data) {
		if (data.length < EMF_SIGNATURE_OFFSET + 4)
			return false;
		ArchiveReader reader = new ArchiveReader(data, 0);
		try {
			if (reader.i32() != 1)
				return false;
			reader.seek(EMF_SIGNATURE_OFFSET);
			return reader.i32() == EMF_SIGNATURE;
		}
		catch (IOException e) {
			return false;
		}
	}
}
