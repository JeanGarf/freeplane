package org.freeplane.view.swing.map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TooltipTextWidth {
	private static final Pattern POINT_WIDTH = Pattern.compile("(?<![\\w-])width\\s*:\\s*[0-9.]+\\s*pt\\s*;");

	static String narrowedTo(String html, int widthInPixels) {
		final Matcher matcher = POINT_WIDTH.matcher(html);
		return matcher.find() ? matcher.replaceFirst("width: " + widthInPixels + "px;") : html;
	}
}
