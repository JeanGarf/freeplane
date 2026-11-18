package org.freeplane.features.text.mindmapmode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;

class ImageResizeHandles {
	private static final int HANDLE_SIZE = 8;
	private static final int MARGIN = HANDLE_SIZE / 2 + 1;
	private static final Color COLOR = new Color(0, 0, 128);

	private final JEditorPane editorPane;
	private final EmbeddedImageResizer resizer;
	private final Overlay overlay = new Overlay();
	private int selectedStart = -1;

	static void install(JEditorPane editorPane, EmbeddedImageResizer resizer) {
		new ImageResizeHandles(editorPane, resizer);
	}

	private ImageResizeHandles(JEditorPane editorPane, EmbeddedImageResizer resizer) {
		this.editorPane = editorPane;
		this.resizer = resizer;
		editorPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1 || !editorPane.isEditable())
					return;
				final Element image = resizer.imageAt(e.getPoint());
				if (image == null)
					deselect();
				else if (image.getStartOffset() != selectedStart)
					select(image);
			}
		});
		editorPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				deselect();
			}
		});
		final DocumentListener documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				deselect();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				deselect();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				deselect();
			}
		};
		editorPane.getDocument().addDocumentListener(documentListener);
		editorPane.addPropertyChangeListener("document", e -> {
			deselect();
			final Document newDocument = editorPane.getDocument();
			if (newDocument != null)
				newDocument.addDocumentListener(documentListener);
		});
		editorPane.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				deselect();
			}
		});
	}

	private void select(Element image) {
		final Rectangle bounds = resizer.displayedBoundsOf(image);
		if (bounds == null)
			return;
		selectedStart = image.getStartOffset();
		overlay.showAround(bounds);
		editorPane.select(image.getStartOffset(), image.getEndOffset());
	}

	private void deselect() {
		selectedStart = -1;
		if (overlay.getParent() != null) {
			editorPane.remove(overlay);
			editorPane.repaint();
		}
	}

	private void apply(Dimension size) {
		final int start = selectedStart;
		final Element image = resizer.imageStartingAt(start);
		if (image == null)
			return;
		resizer.applySize(image, size);
		SwingUtilities.invokeLater(() -> {
			final Element resized = resizer.imageStartingAt(start);
			if (resized != null)
				select(resized);
		});
	}

	private class Overlay extends JComponent {
		private static final long serialVersionUID = 1L;
		private Rectangle imageBounds;
		private ImageHandle dragged;
		private Point pressPoint;
		private Rectangle preview;

		Overlay() {
			setOpaque(false);
			final MouseAdapter mouse = new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					final ImageHandle handle = handleAt(e.getPoint());
					setCursor(handle == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(handle.cursorType));
				}

				@Override
				public void mousePressed(MouseEvent e) {
					dragged = handleAt(e.getPoint());
					if (dragged == null)
						return;
					pressPoint = SwingUtilities.convertPoint(Overlay.this, e.getPoint(), editorPane);
					preview = new Rectangle(imageBounds);
					setBounds(0, 0, editorPane.getWidth(), editorPane.getHeight());
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (dragged == null)
						return;
					final Point point = SwingUtilities.convertPoint(Overlay.this, e.getPoint(), editorPane);
					final Dimension size = dragged.resized(imageBounds.getSize(), point.x - pressPoint.x,
					    point.y - pressPoint.y);
					preview = dragged.anchored(imageBounds, size);
					repaint();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (dragged == null)
						return;
					final Dimension size = preview.getSize();
					dragged = null;
					if (!size.equals(imageBounds.getSize()))
						apply(size);
					else
						showAround(imageBounds);
				}
			};
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
		}

		void showAround(Rectangle bounds) {
			imageBounds = new Rectangle(bounds);
			dragged = null;
			preview = null;
			setBounds(bounds.x - MARGIN, bounds.y - MARGIN, bounds.width + 2 * MARGIN, bounds.height + 2 * MARGIN);
			if (getParent() == null)
				editorPane.add(this, 0);
			editorPane.repaint();
		}

		private Rectangle imageInOverlay() {
			return new Rectangle(imageBounds.x - getX(), imageBounds.y - getY(), imageBounds.width, imageBounds.height);
		}

		private ImageHandle handleAt(Point point) {
			final Rectangle image = imageInOverlay();
			for (final ImageHandle handle : ImageHandle.values()) {
				final Point center = handle.centerOn(image);
				if (new Rectangle(center.x - HANDLE_SIZE / 2, center.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE)
				    .contains(point))
					return handle;
			}
			return null;
		}

		@Override
		public boolean contains(int x, int y) {
			return dragged != null || (imageBounds != null && handleAt(new Point(x, y)) != null);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			if (imageBounds == null)
				return;
			final Graphics2D g = (Graphics2D) graphics.create();
			try {
				g.setColor(COLOR);
				g.setStroke(new BasicStroke(1f));
				final Rectangle image = imageInOverlay();
				g.drawRect(image.x, image.y, image.width, image.height);
				for (final ImageHandle handle : ImageHandle.values()) {
					final Point center = handle.centerOn(image);
					g.setColor(Color.WHITE);
					g.fillRect(center.x - HANDLE_SIZE / 2, center.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
					g.setColor(COLOR);
					g.drawRect(center.x - HANDLE_SIZE / 2, center.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
				}
				if (dragged != null && preview != null) {
					g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					    new float[] { 5f, 4f }, 0f));
					g.drawRect(preview.x - getX(), preview.y - getY(), preview.width, preview.height);
				}
			}
			finally {
				g.dispose();
			}
		}
	}
}
