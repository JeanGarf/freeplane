package org.freeplane.features.url.mindmapmode;

import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.fpmx.FpmxArchive;

class ArchiveNaming {
	static String archiveNameFor(String mapFileName) {
		final String baseName = mapFileName.endsWith(UrlManager.FREEPLANE_FILE_EXTENSION)
		        ? mapFileName.substring(0, mapFileName.length() - UrlManager.FREEPLANE_FILE_EXTENSION.length())
		        : mapFileName;
		return baseName + FpmxArchive.FILE_EXTENSION;
	}

	static boolean isArchive(String fileName) {
		return fileName.endsWith(FpmxArchive.FILE_EXTENSION);
	}
}
