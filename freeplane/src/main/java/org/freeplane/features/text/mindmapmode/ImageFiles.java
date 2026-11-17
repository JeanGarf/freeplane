package org.freeplane.features.text.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.mmx.ImageDataUri;

class ImageFiles {
	static boolean holdsFiles(Transferable contents) {
		return contents != null && contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	@SuppressWarnings("unchecked")
	static List<String> htmlOf(Transferable contents) {
		final List<String> images = new ArrayList<>();
		try {
			final List<File> files = (List<File>) contents.getTransferData(DataFlavor.javaFileListFlavor);
			for (final File file : files) {
				final String html = ImageDataUri.htmlForFile(file);
				if (html == null) {
					LogUtils.info("Not a readable image: " + file);
					return new ArrayList<>();
				}
				images.add(html);
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
			return new ArrayList<>();
		}
		return images;
	}

	static void insert(JEditorPane editorPane, List<String> images, int position) throws Exception {
		final HTMLDocument document = (HTMLDocument) editorPane.getDocument();
		final HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKit();
		int offset = position;
		for (final String html : images) {
			final int before = document.getLength();
			kit.insertHTML(document, offset, html, 0, 0, HTML.Tag.IMG);
			offset += document.getLength() - before;
		}
	}
}
