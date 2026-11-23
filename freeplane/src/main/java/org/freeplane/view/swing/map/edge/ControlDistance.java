package org.freeplane.view.swing.map.edge;

class ControlDistance {
	private static final float SPAN_RATIO = 0.5f;

	static float of(float minimum, float span) {
		return Math.max(minimum, Math.abs(span) * SPAN_RATIO);
	}
}
