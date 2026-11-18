package org.freeplane.features.clipboard.mindmapmode;

import java.awt.Image;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.clipboard.NodeImagePaster;
import org.freeplane.features.map.mindmapmode.clipboard.PasteImageTarget;
import org.freeplane.features.mode.Controller;

@SelectableAction(checkOnPropertyChange = PasteImageTarget.PROPERTY_NAME)
class PasteImageAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final PasteImageTarget target;

	PasteImageAction(PasteImageTarget target) {
		super(target.actionKey);
		this.target = target;
		setSelected();
	}

	@Override
	public void setSelected() {
		setSelected(NodeImagePaster.rememberedTarget() == target);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		try {
			pasteImage();
		}
		finally {
			setSelected();
		}
	}

	private void pasteImage() {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final NodeModel node = selection == null ? null : selection.getSelected();
		if (node == null)
			return;
		final Image image = NodeImagePaster.imageInClipboard();
		if (image == null) {
			UITools.informationMessage(TextUtils.getText("paste_image.no_image"));
			return;
		}
		NodeImagePaster.remember(target);
		NodeImagePaster.paste(image, node, target);
	}
}
