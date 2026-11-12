package org.freeplane.features.edge;

import org.freeplane.features.nodestyle.NodeStyleShape;

public enum EdgeAttachment {
	top, middle, bottom;

	public static EdgeAttachment defaultFor(NodeStyleShape shape) {
		return shape == NodeStyleShape.fork ? bottom : middle;
	}
}
