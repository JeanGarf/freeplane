package org.freeplane.features.texttree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.freeplane.features.map.NodeModel;

public class TextTreeExtraction {
	private final boolean includesFolded;
	private final Predicate<NodeModel> isExported;
	private final Function<NodeModel, String> textOf;
	private final Function<NodeModel, String> linkOf;

	public TextTreeExtraction(final boolean includesFolded, final Predicate<NodeModel> isExported,
	                          final Function<NodeModel, String> textOf, final Function<NodeModel, String> linkOf) {
		this.includesFolded = includesFolded;
		this.isExported = isExported;
		this.textOf = textOf;
		this.linkOf = linkOf;
	}

	public List<TextTreeNode> extract(final NodeModel start, final boolean includesStart) {
		if (!includesStart)
			return childrenOf(start);
		final TextTreeNode startNode = contentOf(start);
		startNode.children().addAll(childrenOf(start));
		return Collections.singletonList(startNode);
	}

	private List<TextTreeNode> childrenOf(final NodeModel parent) {
		final List<TextTreeNode> children = new ArrayList<TextTreeNode>();
		for (final NodeModel child : parent.getChildren()) {
			if (isExported.test(child))
				children.add(treeOf(child));
			else
				children.addAll(childrenOf(child));
		}
		return children;
	}

	private TextTreeNode treeOf(final NodeModel node) {
		final TextTreeNode treeNode = contentOf(node);
		if (includesFolded || !node.isFolded())
			treeNode.children().addAll(childrenOf(node));
		return treeNode;
	}

	private TextTreeNode contentOf(final NodeModel node) {
		return new TextTreeNode(TextTreeLine.singleLine(textOf.apply(node)), linkOf.apply(node));
	}
}
