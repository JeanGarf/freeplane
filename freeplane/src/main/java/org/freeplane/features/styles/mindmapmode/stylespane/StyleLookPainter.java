package org.freeplane.features.styles.mindmapmode.stylespane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.freeplane.features.nodestyle.NodeStyleShape;

public class StyleLookPainter {
	static final float DEFAULT_BORDER_WIDTH = 1.5f;
	private static final float MAX_BORDER_WIDTH = 8f;

	static float paintedBorderWidth(StyleLook look) {
		return Math.max(1f, Math.min(look.borderWidth, MAX_BORDER_WIDTH));
	}

	public static Shape shapeIn(NodeStyleShape shape, double x, double y, double width, double height) {
		switch (shape) {
		case oval:
			return new Ellipse2D.Double(x, y, width, height);
		case rectangle:
		case fork:
			return new Rectangle2D.Double(x, y, width, height);
		case wide_hexagon:
			return hexagon(x, y, width, height, height / 2);
		case narrow_hexagon:
			return hexagon(x, y, width, height, height / 4);
		default:
			return new RoundRectangle2D.Double(x, y, width, height, height * 0.6, height * 0.6);
		}
	}

	public static int textInset(NodeStyleShape shape, int height) {
		switch (shape) {
		case oval:
			return height / 2 + 2;
		case wide_hexagon:
			return height / 2;
		case narrow_hexagon:
			return height / 4 + 4;
		default:
			return 8;
		}
	}

	public static Font fontFor(StyleLook look, Font base) {
		final int style = (look.bold ? Font.BOLD : 0) | (look.italic ? Font.ITALIC : 0);
		return new Font(look.fontFamily != null ? look.fontFamily : base.getFamily(), style, base.getSize());
	}

	public static void paintBackground(Graphics2D g, StyleLook look, int x, int y, int width, int height) {
		final Graphics2D graphics = (Graphics2D) g.create();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final float borderWidth = paintedBorderWidth(look);
			final double inset = borderWidth / 2;
			final Shape shape = shapeIn(look.shape, x + inset, y + inset, width - 2 * inset - 1, height - 2 * inset - 1);
			if (look.background != null) {
				graphics.setColor(look.background);
				graphics.fill(shape);
			}
			graphics.setStroke(new BasicStroke(borderWidth));
			if (look.shape == NodeStyleShape.fork) {
				graphics.setColor(underlineColor(look));
				final double lineY = StyleEdgePainter.attachmentY(NodeStyleShape.fork, y, height, borderWidth);
				graphics.draw(new Line2D.Double(x, lineY, x + width - 1, lineY));
			}
			else if (look.border != null) {
				graphics.setColor(look.border);
				graphics.draw(shape);
			}
		}
		finally {
			graphics.dispose();
		}
	}

	private static Color underlineColor(StyleLook look) {
		return look.border != null ? look.border : look.foreground != null ? look.foreground : Color.GRAY;
	}

	private static Shape hexagon(double x, double y, double width, double height, double point) {
		final double p = Math.min(point, width / 2);
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(x + p, y);
		path.lineTo(x + width - p, y);
		path.lineTo(x + width, y + height / 2);
		path.lineTo(x + width - p, y + height);
		path.lineTo(x + p, y + height);
		path.lineTo(x, y + height / 2);
		path.closePath();
		return path;
	}
}
