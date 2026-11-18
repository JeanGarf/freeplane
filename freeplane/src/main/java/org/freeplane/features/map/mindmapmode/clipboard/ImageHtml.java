package org.freeplane.features.map.mindmapmode.clipboard;

import org.freeplane.core.util.HtmlUtils;

public class ImageHtml {
	private static final String BODY_END = "</body>";

	public static String appendTo(String existingContent, String imageElement) {
		final String paragraph = "<p>" + imageElement + "</p>";
		if (existingContent == null || existingContent.trim().isEmpty())
			return "<html><body>" + paragraph + BODY_END + "</html>";
		final String html = HtmlUtils.isHtml(existingContent) ? existingContent : HtmlUtils.plainToHTML(existingContent);
		final int bodyEnd = html.toLowerCase().lastIndexOf(BODY_END);
		return bodyEnd < 0 ? html + paragraph : html.substring(0, bodyEnd) + paragraph + html.substring(bodyEnd);
	}
}
