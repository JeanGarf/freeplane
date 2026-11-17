package org.freeplane.features.text.mindmapmode;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.mmx.EmbeddedImages;

class EmbeddedImageResizer extends MouseAdapter {
	private final JEditorPane editorPane;

	static void install(JEditorPane editorPane) {
		final EmbeddedImageResizer resizer = new EmbeddedImageResizer(editorPane);
		editorPane.addMouseListener(resizer);
		ImageResizeHandles.install(editorPane, resizer);
	}

	private EmbeddedImageResizer(JEditorPane editorPane) {
		this.editorPane = editorPane;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() != 2 || e.getButton() != MouseEvent.BUTTON1 || !editorPane.isEditable())
			return;
		final Element image = imageAt(e.getPoint());
		if (image == null)
			return;
		try {
			resize(image);
		}
		catch (Exception ex) {
			LogUtils.warn(ex);
		}
	}

	Element imageAt(Point point) {
		if (!(editorPane.getDocument() instanceof HTMLDocument))
			return null;
		final HTMLDocument document = (HTMLDocument) editorPane.getDocument();
		final int position = editorPane.viewToModel(point);
		for (int candidate = position; candidate >= Math.max(0, position - 1); candidate--) {
			final Element element = document.getCharacterElement(candidate);
			if (HTML.Tag.IMG.toString().equalsIgnoreCase(element.getName()) && contains(element, point))
				return element;
		}
		return null;
	}

	private boolean contains(Element element, Point point) {
		final Rectangle bounds = boundsOf(element);
		return bounds != null && bounds.contains(point);
	}

	Rectangle boundsOf(Element element) {
		try {
			final Rectangle start = editorPane.modelToView(element.getStartOffset());
			final Rectangle end = editorPane.modelToView(element.getEndOffset());
			return start == null || end == null ? null : start.union(end);
		}
		catch (javax.swing.text.BadLocationException e) {
			return null;
		}
	}

	Element imageStartingAt(int offset) {
		if (offset < 0 || !(editorPane.getDocument() instanceof HTMLDocument))
			return null;
		final Element element = ((HTMLDocument) editorPane.getDocument()).getCharacterElement(offset);
		return HTML.Tag.IMG.toString().equalsIgnoreCase(element.getName()) ? element : null;
	}

	void applySize(Element image, Dimension size) {
		DeferredImages.setSize((HTMLDocument) editorPane.getDocument(), image, size.width, size.height);
	}

	private void resize(Element image) throws Exception {
		final AttributeSet attributes = image.getAttributes();
		final String source = String.valueOf(attributes.getAttribute(HTML.Attribute.SRC));
		final Dimension original = originalSize(source);
		final Dimension current = currentSize(attributes, original);
		if (current == null)
			return;
		final Dimension chosen = ImageSizeDialog.ask(editorPane, current, original);
		if (chosen == null)
			return;
		applySize(image, chosen);
	}

	Rectangle displayedBoundsOf(Element image) {
		final Rectangle bounds = boundsOf(image);
		if (bounds == null)
			return null;
		final Dimension size = currentSize(image.getAttributes(), null);
		return size == null ? bounds : new Rectangle(bounds.x, bounds.y, size.width, size.height);
	}

	private Dimension currentSize(AttributeSet attributes, Dimension original) {
		final Integer width = number(attributes.getAttribute(HTML.Attribute.WIDTH));
		final Integer height = number(attributes.getAttribute(HTML.Attribute.HEIGHT));
		if (width != null && height != null)
			return new Dimension(width, height);
		return original;
	}

	private Integer number(Object value) {
		try {
			return value == null ? null : Integer.valueOf(value.toString().trim());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private Dimension originalSize(String source) {
		final EmbeddedImages.Extraction extraction = EmbeddedImages.extract(source);
		if (extraction.images.isEmpty())
			return null;
		try {
			final byte[] content = extraction.images.values().iterator().next();
			final BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
			return image == null ? null : new Dimension(image.getWidth(), image.getHeight());
		}
		catch (java.io.IOException e) {
			return null;
		}
	}
}
