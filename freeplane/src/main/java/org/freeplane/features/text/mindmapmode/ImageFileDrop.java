package org.freeplane.features.text.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.mmx.ImageDataUri;

class ImageFileDrop implements DropTargetListener {
	private final JEditorPane editorPane;
	private final DropTarget standard;

	static void install(JEditorPane editorPane) {
		final DropTarget standard = editorPane.getDropTarget();
		if (standard == null)
			return;
		editorPane.setDropTarget(new DropTarget(editorPane, DnDConstants.ACTION_COPY_OR_MOVE,
		    new ImageFileDrop(editorPane, standard), true));
	}

	private ImageFileDrop(JEditorPane editorPane, DropTarget standard) {
		this.editorPane = editorPane;
		this.standard = standard;
	}

	@Override
	public void dragEnter(DropTargetDragEvent event) {
		if (!acceptsFiles(event.getCurrentDataFlavors()))
			standard.dragEnter(event);
		else
			event.acceptDrag(DnDConstants.ACTION_COPY);
	}

	@Override
	public void dragOver(DropTargetDragEvent event) {
		if (!acceptsFiles(event.getCurrentDataFlavors()))
			standard.dragOver(event);
		else
			event.acceptDrag(DnDConstants.ACTION_COPY);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent event) {
		if (!acceptsFiles(event.getCurrentDataFlavors()))
			standard.dropActionChanged(event);
	}

	@Override
	public void dragExit(DropTargetEvent event) {
		standard.dragExit(event);
	}

	@Override
	public void drop(DropTargetDropEvent event) {
		final List<String> images = imageHtmlOf(event);
		if (images.isEmpty()) {
			standard.drop(event);
			return;
		}
		event.acceptDrop(DnDConstants.ACTION_COPY);
		try {
			insert(images, editorPane.viewToModel(event.getLocation()));
			event.dropComplete(true);
		}
		catch (Exception e) {
			LogUtils.warn(e);
			event.dropComplete(false);
		}
	}

	private boolean acceptsFiles(DataFlavor[] flavors) {
		for (final DataFlavor flavor : flavors)
			if (DataFlavor.javaFileListFlavor.equals(flavor))
				return editorPane.isEditable();
		return false;
	}

	@SuppressWarnings("unchecked")
	private List<String> imageHtmlOf(DropTargetDropEvent event) {
		final List<String> images = new ArrayList<>();
		final Transferable transferable = event.getTransferable();
		if (!editorPane.isEditable() || !transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			return images;
		try {
			final List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
			for (final File file : files) {
				final String html = ImageDataUri.htmlForFile(file);
				if (html == null)
					return new ArrayList<>();
				images.add(html);
			}
		}
		catch (Exception e) {
			return new ArrayList<>();
		}
		return images;
	}

	private void insert(List<String> images, int position) throws Exception {
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
