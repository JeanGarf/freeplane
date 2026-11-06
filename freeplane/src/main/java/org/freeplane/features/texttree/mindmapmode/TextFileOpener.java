package org.freeplane.features.texttree.mindmapmode;

import java.io.File;
import java.io.IOException;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

class TextFileOpener {
	static void open(final File file) {
		if (Compat.isWindowsOS() && openedWithWindowsShell(file))
			return;
		try {
			Controller.getCurrentController().getViewController().openDocument(file.toURI().toURL());
		}
		catch (final Exception e) {
			LogUtils.warn(e);
		}
	}

	private static boolean openedWithWindowsShell(final File file) {
		try {
			new ProcessBuilder("rundll32.exe", "shell32.dll,ShellExec_RunDLL", file.getAbsolutePath()).start();
			return true;
		}
		catch (final IOException e) {
			LogUtils.warn(e);
			return false;
		}
	}

	private TextFileOpener() {
	}
}
