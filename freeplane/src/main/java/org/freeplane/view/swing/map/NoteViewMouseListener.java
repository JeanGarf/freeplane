package org.freeplane.view.swing.map;

import java.awt.event.MouseEvent;

import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.ui.LinkNavigatorMouseListener;
import org.freeplane.view.swing.ui.NodeSelector;

public class NoteViewMouseListener extends LinkNavigatorMouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			TextController controller = TextController.getController();
			if (controller.canEdit()) {
				NodeModel node = NodeSelector.mapNodeSelector.getRelatedNodeView(e).getNode();
				((MNoteController) NoteController.getController()).editNoteInDialog(node);
			}
		}
		else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && isOnIcon(e)
		        && NoteController.getController() instanceof MNoteController) {
			final NodeModel node = NodeSelector.mapNodeSelector.getRelatedNodeView(e).getNode();
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			if (selection != null && selection.getSelected() != node)
				selection.selectAsTheOnlyOneSelected(node);
			((MNoteController) NoteController.getController()).showNoteAndFocusIt();
		}
		else
			super.mouseClicked(e);
	}

	private boolean isOnIcon(MouseEvent e) {
		return e.getComponent() instanceof ZoomableLabel && e.getX() < ((ZoomableLabel) e.getComponent()).getZoomedIconWidth();
	}
}
