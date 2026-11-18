package org.freeplane.features.map.mindmapmode.clipboard;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URI;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.url.mmx.ImageDataUri;
import org.freeplane.view.swing.features.filepreview.ViewerController;

public class NodeImagePaster {
	private static final int MAXIMUM_WIDTH_IN_TEXT = 800;

	public static PasteImageTarget rememberedTarget() {
		return PasteImageTarget.parse(ResourceController.getResourceController().getProperty(
		    PasteImageTarget.PROPERTY_NAME, PasteImageTarget.DEFAULT.name()));
	}

	public static void remember(PasteImageTarget target) {
		ResourceController.getResourceController().setProperty(PasteImageTarget.PROPERTY_NAME, target.name());
	}

	public static Image imageInClipboard() {
		try {
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			final Transferable contents = clipboard.getContents(null);
			if (contents != null && contents.isDataFlavorSupported(DataFlavor.imageFlavor))
				return (Image) contents.getTransferData(DataFlavor.imageFlavor);
		}
		catch (Exception | LinkageError e) {
			LogUtils.warn(e);
		}
		return null;
	}

	public static void paste(Image image, NodeModel node, PasteImageTarget target) {
		try {
			final ModeController modeController = Controller.getCurrentModeController();
			switch (target) {
				case NODE: {
					final String html = ImageDataUri.imageHtml(ImageDataUri.limitedToWidth(image, MAXIMUM_WIDTH_IN_TEXT));
					MTextController.getController().setNodeText(node, ImageHtml.appendTo(node.getText(), html));
					break;
				}
				case DETAILS: {
					final String html = ImageDataUri.imageHtml(ImageDataUri.limitedToWidth(image, MAXIMUM_WIDTH_IN_TEXT));
					MTextController.getController().setDetails(node,
					    ImageHtml.appendTo(DetailModel.getDetailText(node), html));
					break;
				}
				case NOTE: {
					final String html = ImageDataUri.imageHtml(image);
					((MNoteController) NoteController.getController()).setNoteText(node,
					    ImageHtml.appendTo(NoteModel.getNoteText(node), html));
					break;
				}
				case EXTENSION:
					modeController.getExtension(ViewerController.class).paste(URI.create(ImageDataUri.pngDataUri(image)), node);
					break;
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}
}
