package org.freeplane.features.text.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.List;

import javax.swing.JEditorPane;

import org.freeplane.core.util.LogUtils;

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
			acceptDrag(event);
	}

	@Override
	public void dragOver(DropTargetDragEvent event) {
		if (!acceptsFiles(event.getCurrentDataFlavors()))
			standard.dragOver(event);
		else
			acceptDrag(event);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent event) {
		if (!acceptsFiles(event.getCurrentDataFlavors()))
			standard.dropActionChanged(event);
		else
			acceptDrag(event);
	}

	private void acceptDrag(DropTargetDragEvent event) {
		final int action = nonDestructiveAction(event.getSourceActions());
		if (action == DnDConstants.ACTION_NONE)
			event.rejectDrag();
		else
			event.acceptDrag(action);
	}

	static int nonDestructiveAction(int sourceActions) {
		if ((sourceActions & DnDConstants.ACTION_COPY) != 0)
			return DnDConstants.ACTION_COPY;
		if ((sourceActions & DnDConstants.ACTION_LINK) != 0)
			return DnDConstants.ACTION_LINK;
		return DnDConstants.ACTION_NONE;
	}

	@Override
	public void dragExit(DropTargetEvent event) {
		standard.dragExit(event);
	}

	@Override
	public void drop(DropTargetDropEvent event) {
		if (!acceptsFiles(event.getCurrentDataFlavors())) {
			standard.drop(event);
			return;
		}
		final int action = nonDestructiveAction(event.getSourceActions());
		if (action == DnDConstants.ACTION_NONE) {
			event.rejectDrop();
			return;
		}
		event.acceptDrop(action);
		try {
			final List<String> images = ImageFiles.htmlOf(event.getTransferable());
			if (images.isEmpty()) {
				event.dropComplete(false);
				return;
			}
			ImageFiles.insert(editorPane, images, editorPane.viewToModel(event.getLocation()));
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
}
