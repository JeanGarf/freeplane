package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.freeplane.core.util.LogUtils;

class OldMapFolder {
	static final String NAME = "OLD_MM_TO_DELETE";

	static File moveInto(File map) {
		final File folder = new File(map.getAbsoluteFile().getParentFile(), NAME);
		try {
			if (!folder.isDirectory() && !folder.mkdirs())
				throw new IOException("Cannot create " + folder);
			final File target = freeNameIn(folder, map.getName());
			Files.move(map.toPath(), target.toPath());
			return target;
		}
		catch (IOException e) {
			LogUtils.warn("Cannot move " + map + " to " + folder, e);
			return null;
		}
	}

	private static File freeNameIn(File folder, String name) {
		File candidate = new File(folder, name);
		for (int number = 1; candidate.exists(); number++)
			candidate = new File(folder, number + "_" + name);
		return candidate;
	}
}
