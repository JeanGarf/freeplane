package org.freeplane.features.styles.mindmapmode.stylespane;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.styles.IStyle;

class StyleSections {
	static List<StyleListItem> items(List<IStyle> styles, int userStyleCount, int levelStyleCount,
	                                 String predefinedTitle, String userTitle, String levelTitle) {
		final int levelStart = Math.max(0, styles.size() - levelStyleCount);
		final int userStart = Math.max(0, levelStart - userStyleCount);
		final List<StyleListItem> items = new ArrayList<>();
		addSection(items, predefinedTitle, styles.subList(0, userStart));
		addSection(items, userTitle, styles.subList(userStart, levelStart));
		addSection(items, levelTitle, styles.subList(levelStart, styles.size()));
		return items;
	}

	private static void addSection(List<StyleListItem> items, String title, List<IStyle> styles) {
		if (styles.isEmpty())
			return;
		if (!items.isEmpty())
			items.add(StyleListItem.blank());
		items.add(StyleListItem.header(title));
		for (final IStyle style : styles)
			items.add(StyleListItem.style(style));
	}
}
