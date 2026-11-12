package org.freeplane.view.swing.map;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.freeplane.features.edge.EdgeAttachment;

class EdgeEntryGeometry {
	static int entryY(EdgeAttachment attachment, int height, int borderWidth, int margin) {
		switch (attachment) {
		case top:
			return margin + borderWidth / 2;
		case bottom:
			return height - 1 - margin - borderWidth / 2;
		default:
			return height / 2;
		}
	}

	static int entryX(Shape outline, boolean leftSide, int y, int width, int fallback) {
		if (outline == null)
			return fallback;
		final Rectangle2D bounds = outline.getBounds2D();
		final double sampleY = Math.max(bounds.getMinY() + 0.01, Math.min(bounds.getMaxY() - 0.01, y + 0.5));
		if (leftSide) {
			for (int x = 0; x < width; x++)
				if (outline.contains(x + 0.5, sampleY))
					return x;
		}
		else {
			for (int x = width - 1; x >= 0; x--)
				if (outline.contains(x + 0.5, sampleY))
					return x;
		}
		return fallback;
	}
}
