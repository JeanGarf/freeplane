package org.freeplane.features.mindmanager2002;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

public class MindManager2002Converter {
	private static final String IMAGE_DIRECTORY_SUFFIX = "_files";

	public static String convert(File mmpFile, File outputMapFile) throws IOException {
		Mm2002Map map = Mm2002FileReader.read(Files.readAllBytes(mmpFile.toPath()));
		String directoryName = imageDirectoryName(outputMapFile);
		File imageDirectory = new File(outputMapFile.getParentFile(), directoryName);
		return new FreeplaneMapWriter((name, image) -> {
			if (!imageDirectory.isDirectory() && !imageDirectory.mkdirs())
				throw new IOException("Cannot create " + imageDirectory);
			ImageIO.write(image, "png", new File(imageDirectory, name));
			return FreeplaneMapWriter.uri(null, directoryName + "/" + name);
		}).write(map);
	}

	static String imageDirectoryName(File outputMapFile) {
		String name = outputMapFile.getName();
		int extension = name.lastIndexOf('.');
		return (extension > 0 ? name.substring(0, extension) : name) + IMAGE_DIRECTORY_SUFFIX;
	}
}
