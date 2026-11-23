package org.freeplane.view.swing.map.edge;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

class SharpBezierGeometry {
	private final Point2D.Float start;
	private final Point2D.Float shapeStart;
	private final Point2D.Float end;
	private final float startX;
	private final float startY;
	private final float startNormalX;
	private final float startNormalY;
	private final float endNormalX;
	private final float endNormalY;
	private final float startHalfWidth;
	private final float endHalfWidth;
	private final Point2D.Float startControl;
	private final Point2D.Float endControl;

	SharpBezierGeometry(Point2D.Float start, Point2D.Float shapeStart, Point2D.Float end, int startDirectionX,
	                    int startDirectionY, int endDirectionX, int endDirectionY, float startHalfWidth,
	                    float endHalfWidth, float minimumControlDistance) {
		this.start = start;
		this.shapeStart = shapeStart;
		this.end = end;
		this.startHalfWidth = startHalfWidth;
		this.endHalfWidth = endHalfWidth;
		this.startX = shapeStart.x;
		this.startY = shapeStart.y;
		this.startNormalX = -startDirectionY;
		this.startNormalY = startDirectionX;
		this.endNormalX = endDirectionY;
		this.endNormalY = -endDirectionX;
		final float startDistance = ControlDistance.of(minimumControlDistance,
		    startDirectionX != 0 ? end.x - shapeStart.x : end.y - shapeStart.y);
		final float endDistance = ControlDistance.of(minimumControlDistance,
		    endDirectionX != 0 ? end.x - shapeStart.x : end.y - shapeStart.y);
		startControl = new Point2D.Float(shapeStart.x + startDirectionX * startDistance,
		    shapeStart.y + startDirectionY * startDistance);
		endControl = new Point2D.Float(end.x + endDirectionX * endDistance, end.y + endDirectionY * endDistance);
	}

	GeneralPath shape() {
		final GeneralPath path = new GeneralPath();
		final float sx = startNormalX * startHalfWidth;
		final float sy = startNormalY * startHalfWidth;
		final float ex = endNormalX * endHalfWidth;
		final float ey = endNormalY * endHalfWidth;
		if (start != shapeStart) {
			path.moveTo(start.x + sx, start.y + sy);
			path.lineTo(start.x - sx, start.y - sy);
			path.lineTo(startX - sx, startY - sy);
		}
		else
			path.moveTo(startX - sx, startY - sy);
		path.curveTo(startControl.x - sx, startControl.y - sy, endControl.x - ex, endControl.y - ey, end.x - ex,
		    end.y - ey);
		path.lineTo(end.x + ex, end.y + ey);
		path.curveTo(endControl.x + ex, endControl.y + ey, startControl.x + sx, startControl.y + sy, startX + sx,
		    startY + sy);
		path.closePath();
		return path;
	}

	Path2D centerLine() {
		final Path2D line = new Path2D.Float();
		line.moveTo(start.x, start.y);
		if (start != shapeStart)
			line.lineTo(startX, startY);
		line.curveTo(startControl.x, startControl.y, endControl.x, endControl.y, end.x, end.y);
		return line;
	}
}
