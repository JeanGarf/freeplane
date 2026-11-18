package org.freeplane.features.map.mindmapmode.clipboard;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.mindmapmode.InsertionRelation;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.features.filepreview.ImageAdder;

class LinkedImageFilePaster {
	static void paste(Image image, NodeModel target, Side side) {
		final ModeController modeController = Controller.getCurrentModeController();
		final MMapController mapController = (MMapController) modeController.getMapController();
		final File mindmapFile = target.getMap().getFile();
		if (mindmapFile == null) {
			UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
			return;
		}
		try {
			File imageFile = new TargetFileCreator().createTargetFile(mindmapFile, ImageAdder.IMAGE_FORMAT);
			imageFile.getParentFile().mkdirs();
			final File proposedFile = new File(imageFile.getAbsolutePath());
			final JFileChooser fileChooser = UITools.newFileChooser(proposedFile);
			final CaseSensitiveFileNameExtensionFilter filter = new CaseSensitiveFileNameExtensionFilter();
			filter.addExtension(ImageAdder.IMAGE_FORMAT);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileFilter(filter);
			fileChooser.setSelectedFile(proposedFile);
			if (fileChooser.showSaveDialog(UITools.getCurrentRootComponent()) != JFileChooser.APPROVE_OPTION) {
				proposedFile.delete();
				return;
			}
			imageFile = fileChooser.getSelectedFile();
			if (proposedFile.exists() && !imageFile.getAbsoluteFile().equals(proposedFile))
				proposedFile.delete();
			if (imageFile.isDirectory())
				return;
			if (!FileUtils.getExtension(imageFile.getName()).equals(ImageAdder.IMAGE_FORMAT))
				imageFile = new File(imageFile.getPath() + '.' + ImageAdder.IMAGE_FORMAT);
			final NodeModel node = mapController.newNode(imageFile.getName(), target.getMap());
			node.setSide(side.isSibling() ? target.getSide() : side);
			mapController.insertNode(node, target, InsertionRelation.bySide(side));
			new ImageAdder(image, mapController, mindmapFile, imageFile).attachImageToNode(node);
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}
	}
}
