package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.SHTMLPanelImpl;

class LinkShortcut {
	private static final String ACTION_NAME = "freeplane.editLink";

	static void install(JEditorPane editorPane, SHTMLPanel panel) {
		editorPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control K"), ACTION_NAME);
		editorPane.getActionMap().put(ACTION_NAME, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final Action editLink = ((SHTMLPanelImpl) panel).getAction("editLink");
				if (editLink != null)
					editLink.actionPerformed(e);
			}
		});
	}
}
