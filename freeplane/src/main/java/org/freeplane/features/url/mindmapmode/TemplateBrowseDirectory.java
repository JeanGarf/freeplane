package org.freeplane.features.url.mindmapmode;

import java.io.File;

public class TemplateBrowseDirectory {
	public static final TemplateBrowseDirectory SESSION = new TemplateBrowseDirectory();

	private File lastDirectory;

	public synchronized File proposed(File userTemplateDirectory) {
		if (lastDirectory != null && lastDirectory.isDirectory())
			return lastDirectory;
		return userTemplateDirectory != null && userTemplateDirectory.isDirectory() ? userTemplateDirectory : null;
	}

	public synchronized void remember(File directory) {
		if (directory != null && directory.isDirectory())
			lastDirectory = directory;
	}
}
