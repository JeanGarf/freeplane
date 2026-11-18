package org.freeplane.features.text.mindmapmode;

import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

class ImageLinks {
	static Element selectedImage(JEditorPane editorPane) {
		if (!(editorPane.getDocument() instanceof HTMLDocument))
			return null;
		final int start = editorPane.getSelectionStart();
		final int end = editorPane.getSelectionEnd();
		if (end - start != 1)
			return null;
		final Element element = ((HTMLDocument) editorPane.getDocument()).getCharacterElement(start);
		return HTML.Tag.IMG.toString().equalsIgnoreCase(element.getName()) ? element : null;
	}

	static String linkOf(Element image) {
		final Object anchor = image.getAttributes().getAttribute(HTML.Tag.A);
		if (!(anchor instanceof AttributeSet))
			return null;
		final Object href = ((AttributeSet) anchor).getAttribute(HTML.Attribute.HREF);
		return href == null ? null : href.toString();
	}

	static void setLink(JEditorPane editorPane, Element image, String link) {
		final HTMLDocument document = (HTMLDocument) editorPane.getDocument();
		final int start = image.getStartOffset();
		final int length = image.getEndOffset() - start;
		if (link == null) {
			final SimpleAttributeSet withoutLink = new SimpleAttributeSet(image.getAttributes().copyAttributes());
			withoutLink.removeAttribute(HTML.Tag.A);
			document.setCharacterAttributes(start, length, withoutLink, true);
			return;
		}
		final SimpleAttributeSet anchor = new SimpleAttributeSet();
		anchor.addAttribute(HTML.Attribute.HREF, link);
		final SimpleAttributeSet withLink = new SimpleAttributeSet();
		withLink.addAttribute(HTML.Tag.A, anchor);
		document.setCharacterAttributes(start, length, withLink, false);
	}
}
