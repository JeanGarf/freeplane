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
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.mmx.ImageDataUri;

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

	private static void insert(JEditorPane editorPane, String html) throws Exception {
		final HTMLDocument document = (HTMLDocument) editorPane.getDocument();
		final HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKit();
		final int start = editorPane.getSelectionStart();
		final int end = editorPane.getSelectionEnd();
		if (end > start)
			document.remove(start, end - start);
		kit.insertHTML(document, start, html, 0, 0, HTML.Tag.IMG);
	}
}
