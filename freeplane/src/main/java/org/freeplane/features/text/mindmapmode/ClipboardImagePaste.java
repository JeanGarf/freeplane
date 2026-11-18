package org.freeplane.features.text.mindmapmode;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.text.DefaultEditorKit;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.fpmx.ImageDataUri;

class ClipboardImagePaste {
	static boolean holdsOnlyAnImage(Transferable contents) {
		return contents != null && contents.isDataFlavorSupported(DataFlavor.imageFlavor)
		        && !contents.isDataFlavorSupported(DataFlavor.allHtmlFlavor)
		        && !contents.isDataFlavorSupported(DataFlavor.fragmentHtmlFlavor)
		        && !contents.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	static void install(JEditorPane editorPane) {
		final ActionMap actionMap = editorPane.getActionMap();
		final Action standardPaste = actionMap.get(DefaultEditorKit.pasteAction);
		actionMap.put(DefaultEditorKit.pasteAction, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!pasteImage(editorPane) && standardPaste != null)
					standardPaste.actionPerformed(e);
			}
		});
	}

	private static boolean pasteImage(JEditorPane editorPane) {
		if (!editorPane.isEditable())
			return false;
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final Transferable contents;
		try {
			contents = clipboard.getContents(null);
			if (ImageFiles.holdsFiles(contents)) {
				final java.util.List<String> images = ImageFiles.htmlOf(contents);
				if (images.isEmpty())
					return false;
				deleteSelection(editorPane);
				ImageFiles.insert(editorPane, images, editorPane.getSelectionStart());
				return true;
			}
			if (!holdsOnlyAnImage(contents))
				return false;
			final Image image = (Image) contents.getTransferData(DataFlavor.imageFlavor);
			insert(editorPane, ImageDataUri.imageHtml(image));
			return true;
		}
		catch (Exception | LinkageError e) {
			LogUtils.warn(e);
			return false;
		}
	}

	private static void deleteSelection(JEditorPane editorPane) throws Exception {
		final int start = editorPane.getSelectionStart();
		final int end = editorPane.getSelectionEnd();
		if (end > start)
			editorPane.getDocument().remove(start, end - start);
	}

	private static void insert(JEditorPane editorPane, String html) throws Exception {
		deleteSelection(editorPane);
		ImageFiles.insert(editorPane, java.util.Collections.singletonList(html), editorPane.getSelectionStart());
	}
}
