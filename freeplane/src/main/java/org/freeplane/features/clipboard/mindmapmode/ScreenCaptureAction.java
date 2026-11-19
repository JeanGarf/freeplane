package org.freeplane.features.clipboard.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.clipboard.NodeImagePaster;
import org.freeplane.features.mode.Controller;

class ScreenCaptureAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	ScreenCaptureAction() {
		super("ScreenCaptureAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final NodeModel node = selection == null ? null : selection.getSelected();
		if (node == null)
			return;
		ScreenSnipper.start(image -> NodeImagePaster.paste(image, node, NodeImagePaster.rememberedTarget()));
	}
}
