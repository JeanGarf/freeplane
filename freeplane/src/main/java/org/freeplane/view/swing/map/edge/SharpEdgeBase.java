package org.freeplane.view.swing.map.edge;

class SharpEdgeBase {
	static final float END_OVERLAP = 1f;
	private static final float OUTLINE_STROKE = 1f;

	static float endHalfWidth(float borderWidth) {
		return Math.max(0f, (borderWidth - OUTLINE_STROKE) / 2);
	}

	static float clampedHalfWidth(float requested, float normalX, float normalY, float nodeHalfWidth,
	                              float nodeHalfHeight) {
		return Math.min(requested, Math.min(limit(nodeHalfWidth, normalX), limit(nodeHalfHeight, normalY)));
	}

	private static float limit(float room, float normalComponent) {
		return Math.abs(normalComponent) < 1e-6f ? Float.MAX_VALUE : room / Math.abs(normalComponent);
	}
}
