package org.freeplane.features.mindmanager2002;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import javax.imageio.ImageIO;

class MindManager2002Converter {
	private static final String IMAGE_DIRECTORY_SUFFIX = "_files";

	static Mm2002Map read(File mmpFile) throws IOException {
		return Mm2002FileReader.read(Files.readAllBytes(mmpFile.toPath()));
	}

	static String toFreeplaneXml(Mm2002Map map, File outputMapFile, ConversionTable table,
			Function<String, StyleAppearance> styleAppearance) throws IOException {
		String directoryName = imageDirectoryName(outputMapFile);
		File imageDirectory = new File(outputMapFile.getParentFile(), directoryName);
		return new FreeplaneMapWriter((name, image) -> {
			if (!imageDirectory.isDirectory() && !imageDirectory.mkdirs())
				throw new IOException("Cannot create " + imageDirectory);
			ImageIO.write(image, "png", new File(imageDirectory, name));
			return FreeplaneMapWriter.uri(null, directoryName + "/" + name);
		}, table, styleAppearance).write(map);
	}

	static String imageDirectoryName(File outputMapFile) {
		String name = outputMapFile.getName();
		int extension = name.lastIndexOf('.');
		return (extension > 0 ? name.substring(0, extension) : name) + IMAGE_DIRECTORY_SUFFIX;
	}
}
