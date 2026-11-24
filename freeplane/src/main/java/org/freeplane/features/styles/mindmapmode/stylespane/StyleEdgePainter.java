package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.nodestyle.NodeStyleShape;

public class StyleEdgePainter {
	public enum Attachment {
		MIDDLE, BOTTOM
	}

	private static final int MAX_WIDTH = 4;

	public static Attachment attachmentFor(NodeStyleShape shape) {
		return shape == NodeStyleShape.fork ? Attachment.BOTTOM : Attachment.MIDDLE;
	}

	public static double attachmentY(NodeStyleShape shape, int y, int height, float borderWidth) {
		return attachmentFor(shape) == Attachment.BOTTOM ? y + height - 1 - borderWidth / 2 : y + height / 2.0;
	}

	public static boolean isVisible(StyleEdge edge) {
		return edge != null && edge.style != EdgeStyle.EDGESTYLE_HIDDEN && edge.style != EdgeStyle.EDGESTYLE_SUMMARY;
	}

	public static Shape pathFor(EdgeStyle style, double x0, double y0, double x1, double y1, double width) {
		final double middle = (x0 + x1) / 2;
		switch (style) {
		case EDGESTYLE_BEZIER:
			return new CubicCurve2D.Double(x0, y0, middle, y0, middle, y1, x1, y1);
		case EDGESTYLE_HORIZONTAL: {
			final Path2D.Double path = new Path2D.Double();
			path.moveTo(x0, y0);
			path.lineTo(x0, y1);
			path.lineTo(x1, y1);
			return path;
		}
		case EDGESTYLE_SHARP_LINEAR: {
			final Path2D.Double path = new Path2D.Double();
			path.moveTo(x0, y0 - width);
			path.lineTo(x1, y1);
			path.lineTo(x0, y0 + width);
			path.closePath();
			return path;
		}
		case EDGESTYLE_SHARP_BEZIER: {
			final Path2D.Double path = new Path2D.Double();
			path.moveTo(x0, y0 - width);
			path.curveTo(middle, y0 - width, middle, y1, x1, y1);
			path.curveTo(middle, y1, middle, y0 + width, x0, y0 + width);
			path.closePath();
			return path;
		}
		default:
			return new Line2D.Double(x0, y0, x1, y1);
		}
	}

	public static void paint(Graphics2D g, StyleEdge edge, NodeStyleShape shape, int zoneX, int zoneWidth, int rowY,
	                         int rowHeight, int styleY, int styleHeight, float borderWidth) {
		if (!isVisible(edge))
			return;
		final Graphics2D graphics = (Graphics2D) g.create();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(edge.color);
			final float width = Math.max(1, Math.min(edge.width, MAX_WIDTH));
			final double x0 = zoneX;
			final double x1 = zoneX + zoneWidth;
			final double y0 = rowY + rowHeight / 2.0;
			final double y1 = attachmentY(shape, styleY, styleHeight, borderWidth);
			final Shape path = pathFor(edge.style, x0, y0, x1, y1, width);
			if (edge.style == EdgeStyle.EDGESTYLE_SHARP_LINEAR || edge.style == EdgeStyle.EDGESTYLE_SHARP_BEZIER) {
				graphics.fill(path);
				return;
			}
			graphics.setStroke(stroke(edge, width));
			graphics.draw(path);
		}
		finally {
			graphics.dispose();
		}
	}

	private static BasicStroke stroke(StyleEdge edge, float width) {
		final int[] pattern = edge.dash == null ? new int[0] : edge.dash.pattern;
		if (pattern.length == 0)
			return new BasicStroke(width);
		final float[] dash = new float[pattern.length];
		for (int i = 0; i < dash.length; i++)
			dash[i] = pattern[i];
		return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f);
	}
}
