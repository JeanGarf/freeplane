package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

class MapFilesFolder {
	static final String SUFFIX = "_files";

	static File of(File mapFile) {
		return new File(mapFile.getAbsoluteFile().getParentFile(), mapFile.getName() + SUFFIX);
	}

	static boolean isReferenced(File folder, Collection<File> referencedFiles) {
		try {
			final String folderPath = folder.getCanonicalPath() + File.separator;
			for (File file : referencedFiles)
				if (file.getCanonicalPath().startsWith(folderPath))
					return true;
			return false;
		}
		catch (IOException e) {
			return true;
		}
	}
}
