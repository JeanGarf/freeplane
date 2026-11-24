package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.mindmapmode.stylespane.StyleModificationMarker;

class StyleBoxModificationLayer extends LayerUI<JComponent> {
	private static final long serialVersionUID = 1L;
	private static final int ARROW_ROOM = 22;

	static JLayer<JComponent> wrap(JComponent box, Supplier<NodeModel> nodeSupplier) {
		final StyleBoxModificationLayer ui = new StyleBoxModificationLayer(nodeSupplier);
		final JLayer<JComponent> layer = new JLayer<>(box, ui);
		layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
		LocalModificationTracker.addListener(() -> SwingUtilities.invokeLater(layer::repaint));
		return layer;
	}

	private final Supplier<NodeModel> nodeSupplier;

	private StyleBoxModificationLayer(Supplier<NodeModel> nodeSupplier) {
		this.nodeSupplier = nodeSupplier;
	}

	static Rectangle markerBounds(int width, int height) {
		final int size = StyleModificationMarker.SIZE;
		return new Rectangle(width - ARROW_ROOM - size, (height - size) / 2, size, size);
	}

	private int count() {
		return LocalModificationTracker.countFor(nodeSupplier.get());
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);
		final int count = count();
		if (count > 0)
			StyleModificationMarker.paint((Graphics2D) g, markerBounds(c.getWidth(), c.getHeight()), count, c.getFont());
	}

	@Override
	protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> layer) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED && isOnMarker(e, layer)) {
			final Rectangle marker = markerBounds(layer.getWidth(), layer.getHeight());
			LocalModificationMenu.show(layer, marker.x, marker.y + marker.height, nodeSupplier.get());
			e.consume();
		}
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> layer) {
		layer.setCursor(isOnMarker(e, layer) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
	}

	private boolean isOnMarker(MouseEvent e, JLayer<? extends JComponent> layer) {
		if (count() == 0)
			return false;
		final java.awt.Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), layer);
		return markerBounds(layer.getWidth(), layer.getHeight()).contains(point);
	}
}
