package org.freeplane.main.application;

import java.io.File;

import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.fpmx.FpmxArchive;

class ArchiveConversion {
	static boolean isConversion(File before, File after) {
		if (before == null || after == null)
			return false;
		final String beforeName = before.getName();
		final String afterName = after.getName();
		if (!beforeName.endsWith(UrlManager.FREEPLANE_FILE_EXTENSION) || !afterName.endsWith(FpmxArchive.FILE_EXTENSION))
			return false;
		return baseName(beforeName, UrlManager.FREEPLANE_FILE_EXTENSION).equals(baseName(afterName, FpmxArchive.FILE_EXTENSION))
		        && sameFolder(before, after);
	}

	private static String baseName(String fileName, String extension) {
		return fileName.substring(0, fileName.length() - extension.length());
	}

	private static boolean sameFolder(File before, File after) {
		final File beforeFolder = before.getAbsoluteFile().getParentFile();
		final File afterFolder = after.getAbsoluteFile().getParentFile();
		return beforeFolder == null ? afterFolder == null : beforeFolder.equals(afterFolder);
	}
}
