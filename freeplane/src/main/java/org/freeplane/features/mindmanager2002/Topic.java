package org.freeplane.features.mindmanager2002;

import java.util.ArrayList;
import java.util.List;

class Topic {
	static final int NO_COLOR = -1;

	long id;
	String text = "";
	boolean left;
	boolean hidden;
	int imageWidth;
	int imageHeight;
	int x;
	int y;
	boolean bold;
	boolean italic;
	boolean strikedThrough;
	boolean filled;
	int lineColor = NO_COLOR;
	int fillColor = NO_COLOR;
	int cloudColor = NO_COLOR;
	String fontName;
	int fontSize;
	int priority;
	final List<Integer> iconIndexes = new ArrayList<>();
	String hyperlink = "";
	String hyperlinkArguments = "";
	String resource = "";
	String category = "";
	boolean hasTask;
	int percentComplete;
	double taskStart = Double.NaN;
	double taskDue = Double.NaN;
	long taskSeconds;
	Note note = Note.EMPTY;
	final List<Picture> pictures = new ArrayList<>();
	final List<FreeText> freeTexts = new ArrayList<>();
	String alias = "";
	final List<Relationship> relationships = new ArrayList<>();
	final List<Topic> children = new ArrayList<>();

	boolean isFolded() {
		return !hidden && !children.isEmpty() && children.get(0).hidden;
	}

	boolean hasFill() {
		return filled && fillColor != NO_COLOR;
	}
}
