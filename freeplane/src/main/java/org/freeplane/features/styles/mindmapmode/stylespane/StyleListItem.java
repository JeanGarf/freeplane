package org.freeplane.features.styles.mindmapmode.stylespane;

import org.freeplane.features.styles.IStyle;

class StyleListItem {
	final String header;
	final IStyle style;

	private StyleListItem(String header, IStyle style) {
		this.header = header;
		this.style = style;
	}

	static StyleListItem header(String title) {
		return new StyleListItem(title, null);
	}

	static StyleListItem style(IStyle style) {
		return new StyleListItem(null, style);
	}

	boolean isHeader() {
		return header != null;
	}
}
