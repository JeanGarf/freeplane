package org.freeplane.view.swing.map.edge;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

class SharpLinearGeometry {
	private final Point2D.Float start;
	private final Point2D.Float end;
	private final float normalX;
	private final float normalY;
	private final float halfWidth;

	SharpLinearGeometry(Point2D.Float start, Point2D.Float end, float startHalfWidth, float nodeHalfWidth,
	                    float nodeHalfHeight) {
		this.start = start;
		this.end = end;
		final float chordX = end.x - start.x;
		final float chordY = end.y - start.y;
		final float chord = (float) Math.hypot(chordX, chordY);
		normalX = chord == 0 ? 0 : -chordY / chord;
		normalY = chord == 0 ? 1 : chordX / chord;
		halfWidth = SharpEdgeBase.clampedHalfWidth(startHalfWidth, normalX, normalY, nodeHalfWidth, nodeHalfHeight);
	}

	Path2D shape() {
		final Path2D.Float path = new Path2D.Float();
		path.moveTo(start.x - normalX * halfWidth, start.y - normalY * halfWidth);
		path.lineTo(end.x, end.y);
		path.lineTo(start.x + normalX * halfWidth, start.y + normalY * halfWidth);
		path.closePath();
		return path;
	}
}
