package org.freeplane.features.text.mindmapmode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.mmx.DeferredImageSources;

import com.lightdev.app.shtm.SHTMLDocument;
import com.lightdev.app.shtm.SHTMLPanel;

public class DeferredImages {
	public static void setContent(SHTMLPanel panel, String html) {
		final DeferredImageSources deferred = DeferredImageSources.defer(html);
		panel.setCurrentDocumentContent(deferred.html);
		if (!deferred.isEmpty()) {
			restore(panel.getDocument(), deferred.sources);
			purgeUndos(panel);
		}
	}

	public static void setText(JEditorPane viewer, String html) {
		final DeferredImageSources deferred = DeferredImageSources.defer(html);
		viewer.setText(deferred.html);
		if (!deferred.isEmpty() && viewer.getDocument() instanceof HTMLDocument)
			restore((HTMLDocument) viewer.getDocument(), deferred.sources);
	}

	static void restore(HTMLDocument document, Map<String, String> sources) {
		final List<Element> pending = new ArrayList<>();
		collectPendingImages(document.getDefaultRootElement(), sources, pending);
		for (final Element image : pending) {
			final SimpleAttributeSet source = new SimpleAttributeSet();
			source.addAttribute(HTML.Attribute.SRC, sources.get(image.getAttributes().getAttribute(HTML.Attribute.SRC)));
			document.setCharacterAttributes(image.getStartOffset(), image.getEndOffset() - image.getStartOffset(), source,
			    false);
		}
	}

	static void setSize(HTMLDocument document, Element image, int width, int height) {
		final SimpleAttributeSet size = new SimpleAttributeSet();
		size.addAttribute(HTML.Attribute.WIDTH, Integer.toString(width));
		size.addAttribute(HTML.Attribute.HEIGHT, Integer.toString(height));
		document.setCharacterAttributes(image.getStartOffset(), image.getEndOffset() - image.getStartOffset(), size,
		    false);
	}

	static void compoundEdit(HTMLDocument document, Runnable edit) {
		final SHTMLDocument shtml = document instanceof SHTMLDocument ? (SHTMLDocument) document : null;
		if (shtml != null)
			shtml.startCompoundEdit();
		try {
			edit.run();
		}
		finally {
			if (shtml != null)
				shtml.endCompoundEdit();
		}
	}

	private static void collectPendingImages(Element element, Map<String, String> sources, List<Element> pending) {
		final AttributeSet attributes = element.getAttributes();
		if (HTML.Tag.IMG.toString().equalsIgnoreCase(element.getName())
		        && sources.containsKey(String.valueOf(attributes.getAttribute(HTML.Attribute.SRC))))
			pending.add(element);
		for (int i = 0; i < element.getElementCount(); i++)
			collectPendingImages(element.getElement(i), sources, pending);
	}

	private static void purgeUndos(SHTMLPanel panel) {
		try {
			for (Class<?> type = panel.getClass(); type != null; type = type.getSuperclass()) {
				try {
					final Method purge = type.getDeclaredMethod("purgeUndos");
					purge.setAccessible(true);
					purge.invoke(panel);
					return;
				}
				catch (NoSuchMethodException e) {
				}
			}
		}
		catch (Exception | LinkageError e) {
			LogUtils.warn(e);
		}
	}
}
