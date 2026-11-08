package org.freeplane.features.mindmanager2002;

class StyleAppearance {
	static final StyleAppearance NONE = new StyleAppearance(false, false, false);

	final boolean bold;
	final boolean italic;
	final boolean edgeColor;

	StyleAppearance(boolean bold, boolean italic, boolean edgeColor) {
		this.bold = bold;
		this.italic = italic;
		this.edgeColor = edgeColor;
	}
}
