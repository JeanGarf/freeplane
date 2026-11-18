package org.freeplane.features.text.mindmapmode;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.Element;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.SHTMLPanelImpl;

class ImageContextMenu {
	private final JEditorPane editorPane;
	private final EmbeddedImageResizer resizer;
	private final JMenuItem sizeItem = new JMenuItem(TextUtils.getText("embedded_image_resize_menu"));
	private final JMenuItem linkItem = new JMenuItem(TextUtils.getText("SetLinkByTextFieldAction.text"));
	private final JPopupMenu.Separator separator = new JPopupMenu.Separator();
	private Point popupPoint;
	private Element image;

	static void install(JEditorPane editorPane, SHTMLPanel panel, EmbeddedImageResizer resizer) {
		if (!(editorPane instanceof SHTMLEditorPane))
			return;
		final JPopupMenu popup = ((SHTMLEditorPane) editorPane).getPopup();
		if (popup != null)
			new ImageContextMenu(editorPane, panel, resizer, popup);
	}

	private ImageContextMenu(JEditorPane editorPane, SHTMLPanel panel, EmbeddedImageResizer resizer, JPopupMenu popup) {
		this.editorPane = editorPane;
		this.resizer = resizer;
		sizeItem.addActionListener(e -> resizeImage());
		linkItem.addActionListener(e -> editLink(panel));
		popup.addSeparator();
		popup.add(separator);
		popup.add(sizeItem);
		popup.add(linkItem);
		final MouseListener recorder = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				remember(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				remember(e);
			}
		};
		final MouseListener[] existing = editorPane.getMouseListeners();
		for (final MouseListener listener : existing)
			editorPane.removeMouseListener(listener);
		editorPane.addMouseListener(recorder);
		for (final MouseListener listener : existing)
			editorPane.addMouseListener(listener);
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				showImageItemsIfOnImage();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	private void remember(MouseEvent e) {
		if (e.isPopupTrigger())
			popupPoint = e.getPoint();
	}

	private void showImageItemsIfOnImage() {
		image = popupPoint == null || !editorPane.isEditable() ? null : resizer.imageAt(popupPoint);
		final boolean onImage = image != null;
		separator.setVisible(onImage);
		sizeItem.setVisible(onImage);
		linkItem.setVisible(onImage);
		if (onImage)
			editorPane.select(image.getStartOffset(), image.getEndOffset());
	}

	private void resizeImage() {
		try {
			if (image != null)
				resizer.resize(image);
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	private void editLink(SHTMLPanel panel) {
		final Action editLink = ((SHTMLPanelImpl) panel).getAction("editLink");
		if (editLink != null)
			editLink.actionPerformed(null);
	}
}
