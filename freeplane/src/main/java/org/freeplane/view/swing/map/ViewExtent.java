package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JEditorPane;
import javax.swing.text.View;

class ViewExtent {
	static int rightEdge(JEditorPane editor, Dimension size) {
		final View root = editor.getUI().getRootView(editor);
		root.setSize(size.width, size.height);
		return rightEdge(root, new Rectangle(0, 0, size.width, size.height));
	}

	private static int rightEdge(View view, Shape allocation) {
		final Rectangle bounds = allocation instanceof Rectangle ? (Rectangle) allocation : allocation.getBounds();
		int right = bounds.x + bounds.width;
		for (int i = 0; i < view.getViewCount(); i++) {
			final Shape child = view.getChildAllocation(i, allocation);
			if (child != null)
				right = Math.max(right, rightEdge(view.getView(i), child));
		}
		return right;
	}
}
