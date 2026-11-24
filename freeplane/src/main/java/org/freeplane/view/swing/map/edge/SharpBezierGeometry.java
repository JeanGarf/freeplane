package org.freeplane.view.swing.map.edge;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

class SharpBezierGeometry {
	private static final float STRAIGHT_PART = 0.2f;
	private static final float END_BEND_RATIO = 0.3f;

	private final Point2D.Float start;
	private final Point2D.Float end;
	private final int endDirectionX;
	private final int endDirectionY;
	private final float startHalfWidth;
	private final float endHalfWidth;
	private final float minimumControlDistance;
	private final float startNormalX;
	private final float startNormalY;
	private final float endNormalX;
	private final float endNormalY;

	SharpBezierGeometry(Point2D.Float start, Point2D.Float end, int endDirectionX, int endDirectionY,
	                    float startHalfWidth, float endHalfWidth, float minimumControlDistance, float nodeHalfWidth,
	                    float nodeHalfHeight) {
		this.start = start;
		this.end = end;
		this.endDirectionX = endDirectionX;
		this.endDirectionY = endDirectionY;
		this.endHalfWidth = endHalfWidth;
		this.minimumControlDistance = minimumControlDistance;
		final float chordX = end.x - start.x;
		final float chordY = end.y - start.y;
		final float chord = (float) Math.hypot(chordX, chordY);
		final float directionX = chord == 0 ? 1 : chordX / chord;
		final float directionY = chord == 0 ? 0 : chordY / chord;
		this.startNormalX = -directionY;
		this.startNormalY = directionX;
		this.startHalfWidth = SharpEdgeBase.clampedHalfWidth(startHalfWidth, startNormalX, startNormalY, nodeHalfWidth,
		    nodeHalfHeight);
		this.endNormalX = endDirectionY;
		this.endNormalY = -endDirectionX;
	}

	GeneralPath shape() {
		final Border minus = border(-1);
		final Border plus = border(1);
		final GeneralPath path = new GeneralPath();
		path.moveTo(minus.from.x, minus.from.y);
		path.curveTo(minus.control1.x, minus.control1.y, minus.control2.x, minus.control2.y, minus.to.x, minus.to.y);
		path.lineTo(plus.to.x, plus.to.y);
		path.curveTo(plus.control2.x, plus.control2.y, plus.control1.x, plus.control1.y, plus.from.x, plus.from.y);
		path.closePath();
		return path;
	}

	Path2D centerLine() {
		final Border center = border(0);
		final Path2D line = new Path2D.Float();
		line.moveTo(start.x, start.y);
		line.curveTo(center.control1.x, center.control1.y, center.control2.x, center.control2.y, center.to.x,
		    center.to.y);
		return line;
	}

	private Border border(int side) {
		final Point2D.Float from = new Point2D.Float(start.x + side * startNormalX * startHalfWidth,
		    start.y + side * startNormalY * startHalfWidth);
		final Point2D.Float to = new Point2D.Float(end.x + side * endNormalX * endHalfWidth,
		    end.y + side * endNormalY * endHalfWidth);
		final float length = (float) Math.hypot(to.x - from.x, to.y - from.y);
		final float directionX = length == 0 ? 1 : (to.x - from.x) / length;
		final float directionY = length == 0 ? 0 : (to.y - from.y) / length;
		final float bend = Math.max(minimumControlDistance, length * END_BEND_RATIO);
		final Point2D.Float control1 = new Point2D.Float(from.x + directionX * length * STRAIGHT_PART,
		    from.y + directionY * length * STRAIGHT_PART);
		final Point2D.Float control2 = new Point2D.Float(to.x + endDirectionX * bend, to.y + endDirectionY * bend);
		return new Border(from, control1, control2, to);
	}

	private static class Border {
		final Point2D.Float from;
		final Point2D.Float control1;
		final Point2D.Float control2;
		final Point2D.Float to;

		Border(Point2D.Float from, Point2D.Float control1, Point2D.Float control2, Point2D.Float to) {
			this.from = from;
			this.control1 = control1;
			this.control2 = control2;
			this.to = to;
		}
	}
}
