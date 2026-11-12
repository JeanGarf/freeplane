package org.freeplane.features.styles.mindmapmode.stylespane;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;

class StyleNodeCollector {
	static List<NodeModel> nodesUsing(NodeModel root, IStyle style) {
		final List<NodeModel> nodes = new ArrayList<>();
		collect(root, style, nodes);
		return nodes;
	}

	private static void collect(NodeModel node, IStyle style, List<NodeModel> nodes) {
		if (style.equals(LogicalStyleModel.getStyle(node)))
			nodes.add(node);
		for (final NodeModel child : node.getChildren())
			collect(child, style, nodes);
	}
}
