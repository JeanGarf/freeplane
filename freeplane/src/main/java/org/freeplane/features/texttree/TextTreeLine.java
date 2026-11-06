package org.freeplane.features.texttree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTreeLine {
	private static final Pattern TEXT_WITH_LINK = Pattern
		.compile("^(.*?)\\s*\\(([A-Za-z][A-Za-z0-9+.\\-]*:[^()\\s]*)\\)$");

	public static String singleLine(final String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim();
	}

	public static String format(final String text, final String link) {
		final String singleLineText = singleLine(text);
		if (link == null || link.isEmpty())
			return singleLineText;
		return singleLineText.isEmpty() ? "(" + link + ")" : singleLineText + " (" + link + ")";
	}

	public static TextTreeNode parse(final String element) {
		final String trimmed = singleLine(element);
		final Matcher matcher = TEXT_WITH_LINK.matcher(trimmed);
		if (matcher.matches())
			return new TextTreeNode(matcher.group(1), matcher.group(2));
		return new TextTreeNode(trimmed, null);
	}

	private TextTreeLine() {
	}
}
