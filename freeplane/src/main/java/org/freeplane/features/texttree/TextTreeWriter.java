package org.freeplane.features.texttree;

import java.util.ArrayList;
import java.util.List;

public class TextTreeWriter {
	private static final String INDENTATION = "\t";

	public static List<String> tabLines(final List<TextTreeNode> roots) {
		final List<String> lines = new ArrayList<String>();
		for (final TextTreeNode root : roots)
			addTabLines(root, "", lines);
		return lines;
	}

	private static void addTabLines(final TextTreeNode node, final String indentation, final List<String> lines) {
		lines.add(indentation + TextTreeLine.format(node.text(), node.link()));
		for (final TextTreeNode child : node.children())
			addTabLines(child, indentation + INDENTATION, lines);
	}

	public static List<String> pathLines(final List<TextTreeNode> roots, final String separator) {
		final List<String> lines = new ArrayList<String>();
		for (final TextTreeNode root : roots)
			addPathLines(root, null, separator, lines);
		return lines;
	}

	private static void addPathLines(final TextTreeNode node, final String parentPath, final String separator,
	                                 final List<String> lines) {
		final String element = TextTreeLine.format(node.text(), node.link());
		final String path = parentPath == null ? element : parentPath + separator + element;
		if (node.children().isEmpty())
			lines.add(path);
		for (final TextTreeNode child : node.children())
			addPathLines(child, path, separator, lines);
	}

	public static List<String> textsContaining(final List<TextTreeNode> roots, final String separator) {
		final List<String> texts = new ArrayList<String>();
		for (final TextTreeNode root : roots)
			addTextsContaining(root, separator, texts);
		return texts;
	}

	private static void addTextsContaining(final TextTreeNode node, final String separator, final List<String> texts) {
		if (TextTreeReader.split(node.text(), separator).size() > 1)
			texts.add(node.text());
		for (final TextTreeNode child : node.children())
			addTextsContaining(child, separator, texts);
	}

	private TextTreeWriter() {
	}
}
