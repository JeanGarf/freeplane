package org.freeplane.features.mindmanager2002;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.function.Function;

import javax.imageio.ImageIO;

class MindManager2002Converter {
	private static final String PNG_DATA_URI_PREFIX = "data:image/png;base64,";

	static Mm2002Map read(File mmpFile) throws IOException {
		return Mm2002FileReader.read(Files.readAllBytes(mmpFile.toPath()));
	}

	static String toFreeplaneXml(Mm2002Map map, ConversionTable table,
			Function<String, StyleAppearance> styleAppearance) throws IOException {
		return new FreeplaneMapWriter((name, image) -> pngDataUri(image), table, styleAppearance).write(map);
	}

	static String pngDataUri(BufferedImage image) throws IOException {
		ByteArrayOutputStream png = new ByteArrayOutputStream();
		if (!ImageIO.write(image, "png", png))
			throw new IOException("Cannot encode image as PNG");
		return PNG_DATA_URI_PREFIX + Base64.getEncoder().encodeToString(png.toByteArray());
	}
}
