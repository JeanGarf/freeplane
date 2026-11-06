package org.freeplane.features.texttree;

import java.util.ArrayList;
import java.util.List;

public class TextTreeNode {
	private final String text;
	private final String link;
	private final List<TextTreeNode> children = new ArrayList<TextTreeNode>();

	public TextTreeNode(final String text, final String link) {
		this.text = text;
		this.link = link;
	}

	public String text() {
		return text;
	}

	public String link() {
		return link;
	}

	public List<TextTreeNode> children() {
		return children;
	}

	public TextTreeNode add(final TextTreeNode child) {
		children.add(child);
		return child;
	}

	boolean hasSameContentAs(final TextTreeNode other) {
		return text.equals(other.text) && (link == null ? other.link == null : link.equals(other.link));
	}

	@Override
	public String toString() {
		return TextTreeLine.format(text, link) + (children.isEmpty() ? "" : children.toString());
	}
}
