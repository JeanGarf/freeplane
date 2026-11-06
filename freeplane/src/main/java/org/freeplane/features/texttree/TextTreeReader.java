package org.freeplane.features.texttree;

import java.util.ArrayList;
import java.util.List;

public class TextTreeReader {
	private static final int TAB_WIDTH = 4;

	private static class Level {
		final int indentation;
		final TextTreeNode node;

		Level(final int indentation, final TextTreeNode node) {
			this.indentation = indentation;
			this.node = node;
		}
	}

	public static List<TextTreeNode> readTabs(final List<String> lines) {
		final List<TextTreeNode> roots = new ArrayList<TextTreeNode>();
		final List<Level> levels = new ArrayList<Level>();
		for (final String line : lines) {
			if (line.trim().isEmpty())
				continue;
			final int indentation = indentationOf(line);
			while (!levels.isEmpty() && levels.get(levels.size() - 1).indentation >= indentation)
				levels.remove(levels.size() - 1);
			final TextTreeNode node = TextTreeLine.parse(line);
			if (levels.isEmpty())
				roots.add(node);
			else
				levels.get(levels.size() - 1).node.add(node);
			levels.add(new Level(indentation, node));
		}
		return roots;
	}

	private static int indentationOf(final String line) {
		int columns = 0;
		for (int index = 0; index < line.length(); index++) {
			final char character = line.charAt(index);
			if (character == '\t')
				columns = (columns / TAB_WIDTH + 1) * TAB_WIDTH;
			else if (character == ' ' || character == ' ')
				columns++;
			else
				break;
		}
		return columns;
	}

	public static List<TextTreeNode> readPaths(final List<String> lines, final String separator) {
		final List<TextTreeNode> roots = new ArrayList<TextTreeNode>();
		for (final String line : lines) {
			if (line.trim().isEmpty())
				continue;
			List<TextTreeNode> siblings = roots;
			for (final String element : split(line, separator)) {
				final TextTreeNode node = TextTreeLine.parse(element);
				if (node.text().isEmpty() && node.link() == null)
					continue;
				siblings = childOrNew(siblings, node).children();
			}
		}
		return roots;
	}

	private static TextTreeNode childOrNew(final List<TextTreeNode> siblings, final TextTreeNode node) {
		for (final TextTreeNode sibling : siblings) {
			if (sibling.hasSameContentAs(node))
				return sibling;
		}
		siblings.add(node);
		return node;
	}

	static List<String> split(final String line, final String separator) {
		final List<String> elements = new ArrayList<String>();
		int depth = 0;
		int start = 0;
		int index = 0;
		while (index < line.length()) {
			final char character = line.charAt(index);
			if (character == '(')
				depth++;
			else if (character == ')' && depth > 0)
				depth--;
			if (depth == 0 && !separator.isEmpty() && line.startsWith(separator, index)) {
				elements.add(line.substring(start, index));
				index += separator.length();
				start = index;
			}
			else
				index++;
		}
		elements.add(line.substring(start));
		return elements;
	}

	private TextTreeReader() {
	}
}
