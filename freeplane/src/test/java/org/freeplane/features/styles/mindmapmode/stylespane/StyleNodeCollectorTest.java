package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.StyleString;
import org.junit.Test;

public class StyleNodeCollectorTest {
	private static final IStyle IMPORTANT = new StyleString("important");
	private static final IStyle OTHER = new StyleString("other");

	private static void use(NodeModel node, IStyle style) {
		LogicalStyleModel.createExtension(node).setStyle(style);
	}

	@Test
	public void collectsNodesUsingStyleIncludingNestedOnes() {
		final MapFake map = new MapFake();
		final NodeModel first = map.addNode("first");
		final NodeModel second = map.addNode("second");
		final NodeModel nested = map.createNode("nested");
		second.insert(nested);
		use(first, IMPORTANT);
		use(second, OTHER);
		use(nested, IMPORTANT);

		assertThat(StyleNodeCollector.nodesUsing(map.getRoot(), IMPORTANT)).containsExactly(first, nested);
	}

	@Test
	public void returnsNothingWhenStyleIsUnused() {
		final MapFake map = new MapFake();
		use(map.addNode("first"), OTHER);

		assertThat(StyleNodeCollector.nodesUsing(map.getRoot(), IMPORTANT)).isEmpty();
	}
}
