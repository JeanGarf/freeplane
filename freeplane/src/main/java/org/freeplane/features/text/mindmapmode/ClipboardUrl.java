package org.freeplane.features.text.mindmapmode;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.regex.Pattern;

import org.freeplane.core.util.LogUtils;

class ClipboardUrl {
	private static final Pattern WEB_ADDRESS = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

	static String webAddressIn(String clipboardText) {
		if (clipboardText == null)
			return null;
		final String trimmed = clipboardText.trim();
		return WEB_ADDRESS.matcher(trimmed).matches() ? trimmed : null;
	}

	static String webAddressInClipboard() {
		try {
			final Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor))
				return webAddressIn((String) contents.getTransferData(DataFlavor.stringFlavor));
		}
		catch (Exception | LinkageError e) {
			LogUtils.warn(e);
		}
		return null;
	}
}
