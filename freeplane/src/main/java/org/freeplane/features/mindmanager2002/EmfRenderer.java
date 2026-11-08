package org.freeplane.features.mindmanager2002;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class EmfRenderer {
	private static final int EMR_HEADER = 1;
	private static final int EMR_POLYBEZIER = 2;
	private static final int EMR_POLYGON = 3;
	private static final int EMR_POLYLINE = 4;
	private static final int EMR_POLYBEZIERTO = 5;
	private static final int EMR_POLYLINETO = 6;
	private static final int EMR_POLYPOLYLINE = 7;
	private static final int EMR_POLYPOLYGON = 8;
	private static final int EMR_SETWINDOWEXTEX = 9;
	private static final int EMR_SETWINDOWORGEX = 10;
	private static final int EMR_SETVIEWPORTEXTEX = 11;
	private static final int EMR_SETVIEWPORTORGEX = 12;
	private static final int EMR_EOF = 14;
	private static final int EMR_SETMAPMODE = 17;
	private static final int EMR_SETPOLYFILLMODE = 19;
	private static final int EMR_MOVETOEX = 27;
	private static final int EMR_SELECTOBJECT = 37;
	private static final int EMR_CREATEPEN = 38;
	private static final int EMR_CREATEBRUSHINDIRECT = 39;
	private static final int EMR_DELETEOBJECT = 40;
	private static final int EMR_ELLIPSE = 42;
	private static final int EMR_RECTANGLE = 43;
	private static final int EMR_LINETO = 54;
	private static final int EMR_BEGINPATH = 59;
	private static final int EMR_ENDPATH = 60;
	private static final int EMR_CLOSEFIGURE = 61;
	private static final int EMR_FILLPATH = 62;
	private static final int EMR_STROKEANDFILLPATH = 63;
	private static final int EMR_STROKEPATH = 64;
	private static final int EMR_POLYBEZIER16 = 85;
	private static final int EMR_POLYGON16 = 86;
	private static final int EMR_POLYLINE16 = 87;
	private static final int EMR_POLYBEZIERTO16 = 88;
	private static final int EMR_POLYLINETO16 = 89;
	private static final int EMR_POLYPOLYLINE16 = 90;
	private static final int EMR_POLYPOLYGON16 = 91;
	private static final int EMR_EXTCREATEPEN = 95;
	private static final long STOCK_OBJECT = 0x80000000L;
	private static final int MM_TEXT = 1;
	private static final int WINDING = 2;
	private static final int PS_NULL = 5;
	private static final int BS_NULL = 1;
	private static final int RECORD_HEADER_SIZE = 8;
	private static final int MAX_IMAGE_SIZE = 2000;

	private final ArchiveReader reader;
	private final Graphics2D graphics;
	private final Map<Long, Object> objects = new HashMap<>();
	private Color penColor = Color.BLACK;
	private float penWidth = 1;
	private Color brushColor = Color.WHITE;
	private int mapMode = MM_TEXT;
	private double windowOriginX, windowOriginY, windowExtentX = 1, windowExtentY = 1;
	private double viewportOriginX, viewportOriginY, viewportExtentX = 1, viewportExtentY = 1;
	private int fillRule = Path2D.WIND_EVEN_ODD;
	private Path2D.Double path;
	private Point2D.Double current = new Point2D.Double();

	private static class Pen {
		final Color color;
		final float width;

		Pen(Color color, float width) {
			this.color = color;
			this.width = width;
		}
	}

	private static class Brush {
		final Color color;

		Brush(Color color) {
			this.color = color;
		}
	}

	private EmfRenderer(ArchiveReader reader, Graphics2D graphics) {
		this.reader = reader;
		this.graphics = graphics;
	}

	static BufferedImage render(byte[] emf) throws IOException {
		ArchiveReader header = new ArchiveReader(emf, 0);
		if (header.i32() != EMR_HEADER)
			throw new IOException("Not an EMF image");
		header.skip(4);
		int left = header.i32();
		int top = header.i32();
		int right = header.i32();
		int bottom = header.i32();
		int width = right - left + 1;
		int height = bottom - top + 1;
		if (width <= 0 || height <= 0 || width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE)
			throw new IOException("Unsupported EMF image size");
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.translate(-left, -top);
			new EmfRenderer(new ArchiveReader(emf, 0), graphics).play();
		}
		finally {
			graphics.dispose();
		}
		return image;
	}

	private void play() throws IOException {
		while (reader.hasMore()) {
			int start = reader.position();
			int type = reader.i32();
			int size = reader.i32();
			if (size < RECORD_HEADER_SIZE)
				throw new IOException("Corrupted EMF record");
			if (type == EMR_EOF)
				return;
			playRecord(type);
			reader.seek(start + size);
		}
	}

	private void playRecord(int type) throws IOException {
		switch (type) {
		case EMR_SETMAPMODE:
			mapMode = reader.i32();
			break;
		case EMR_SETWINDOWEXTEX:
			windowExtentX = nonZero(reader.i32());
			windowExtentY = nonZero(reader.i32());
			break;
		case EMR_SETWINDOWORGEX:
			windowOriginX = reader.i32();
			windowOriginY = reader.i32();
			break;
		case EMR_SETVIEWPORTEXTEX:
			viewportExtentX = nonZero(reader.i32());
			viewportExtentY = nonZero(reader.i32());
			break;
		case EMR_SETVIEWPORTORGEX:
			viewportOriginX = reader.i32();
			viewportOriginY = reader.i32();
			break;
		case EMR_SETPOLYFILLMODE:
			fillRule = reader.i32() == WINDING ? Path2D.WIND_NON_ZERO : Path2D.WIND_EVEN_ODD;
			break;
		case EMR_CREATEPEN: {
			long handle = reader.u32();
			int style = reader.i32();
			int width = reader.i32();
			reader.skip(4);
			Color color = color();
			objects.put(handle, new Pen(style == PS_NULL ? null : color, Math.max(1, width)));
			break;
		}
		case EMR_EXTCREATEPEN: {
			long handle = reader.u32();
			reader.skip(16);
			int style = reader.i32();
			int width = reader.i32();
			reader.skip(4);
			Color color = color();
			objects.put(handle, new Pen((style & 0xf) == PS_NULL ? null : color, Math.max(1, width)));
			break;
		}
		case EMR_CREATEBRUSHINDIRECT: {
			long handle = reader.u32();
			int style = reader.i32();
			Color color = color();
			objects.put(handle, new Brush(style == BS_NULL ? null : color));
			break;
		}
		case EMR_SELECTOBJECT:
			select(reader.u32());
			break;
		case EMR_DELETEOBJECT:
			objects.remove(reader.u32());
			break;
		case EMR_MOVETOEX:
			current = new Point2D.Double(reader.i32(), reader.i32());
			if (path != null)
				path.moveTo(current.x, current.y);
			break;
		case EMR_LINETO: {
			Point2D.Double target = new Point2D.Double(reader.i32(), reader.i32());
			if (path != null)
				path.lineTo(target.x, target.y);
			else
				stroke(new java.awt.geom.Line2D.Double(current, target));
			current = target;
			break;
		}
		case EMR_RECTANGLE:
		case EMR_ELLIPSE: {
			double left = reader.i32();
			double top = reader.i32();
			double right = reader.i32();
			double bottom = reader.i32();
			Shape shape = type == EMR_RECTANGLE ? new Rectangle2D.Double(left, top, right - left, bottom - top)
					: new Ellipse2D.Double(left, top, right - left, bottom - top);
			fillAndStroke(shape);
			break;
		}
		case EMR_BEGINPATH:
			path = new Path2D.Double(fillRule);
			path.moveTo(current.x, current.y);
			break;
		case EMR_ENDPATH:
			break;
		case EMR_CLOSEFIGURE:
			if (path != null)
				path.closePath();
			break;
		case EMR_FILLPATH:
			if (path != null)
				fill(path);
			path = null;
			break;
		case EMR_STROKEPATH:
			if (path != null)
				stroke(path);
			path = null;
			break;
		case EMR_STROKEANDFILLPATH:
			if (path != null)
				fillAndStroke(path);
			path = null;
			break;
		case EMR_POLYGON:
		case EMR_POLYGON16:
		case EMR_POLYLINE:
		case EMR_POLYLINE16:
		case EMR_POLYBEZIER:
		case EMR_POLYBEZIER16:
		case EMR_POLYLINETO:
		case EMR_POLYLINETO16:
		case EMR_POLYBEZIERTO:
		case EMR_POLYBEZIERTO16:
			polyline(type);
			break;
		case EMR_POLYPOLYGON:
		case EMR_POLYPOLYGON16:
		case EMR_POLYPOLYLINE:
		case EMR_POLYPOLYLINE16:
			polyPolyline(type);
			break;
		default:
			break;
		}
	}

	private void polyline(int type) throws IOException {
		reader.skip(16);
		int count = reader.i32();
		boolean shortPoints = type >= EMR_POLYBEZIER16;
		boolean continues = type == EMR_POLYLINETO || type == EMR_POLYLINETO16 || type == EMR_POLYBEZIERTO
				|| type == EMR_POLYBEZIERTO16;
		boolean bezier = type == EMR_POLYBEZIER || type == EMR_POLYBEZIER16 || type == EMR_POLYBEZIERTO
				|| type == EMR_POLYBEZIERTO16;
		Point2D.Double[] points = points(count, shortPoints);
		Path2D.Double shape = path != null ? path : new Path2D.Double(fillRule);
		int first = 0;
		if (continues) {
			if (path == null)
				shape.moveTo(current.x, current.y);
		}
		else if (count > 0) {
			shape.moveTo(points[0].x, points[0].y);
			first = 1;
		}
		if (bezier) {
			for (int i = first; i + 2 < count; i += 3)
				shape.curveTo(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, points[i + 2].x,
						points[i + 2].y);
		}
		else {
			for (int i = first; i < count; i++)
				shape.lineTo(points[i].x, points[i].y);
		}
		if (count > 0)
			current = points[count - 1];
		if (path != null)
			return;
		if (type == EMR_POLYGON || type == EMR_POLYGON16) {
			shape.closePath();
			fillAndStroke(shape);
		}
		else
			stroke(shape);
	}

	private void polyPolyline(int type) throws IOException {
		reader.skip(16);
		int polygonCount = reader.i32();
		int totalCount = reader.i32();
		int[] counts = new int[polygonCount];
		for (int i = 0; i < polygonCount; i++)
			counts[i] = reader.i32();
		Point2D.Double[] points = points(totalCount, type >= EMR_POLYBEZIER16);
		Path2D.Double shape = path != null ? path : new Path2D.Double(fillRule);
		boolean polygon = type == EMR_POLYPOLYGON || type == EMR_POLYPOLYGON16;
		int index = 0;
		for (int count : counts) {
			for (int i = 0; i < count && index < points.length; i++, index++) {
				if (i == 0)
					shape.moveTo(points[index].x, points[index].y);
				else
					shape.lineTo(points[index].x, points[index].y);
			}
			if (polygon)
				shape.closePath();
		}
		if (path != null)
			return;
		if (polygon)
			fillAndStroke(shape);
		else
			stroke(shape);
	}

	private Point2D.Double[] points(int count, boolean shortPoints) throws IOException {
		Point2D.Double[] points = new Point2D.Double[count];
		for (int i = 0; i < count; i++) {
			double x = shortPoints ? (short) reader.u16() : reader.i32();
			double y = shortPoints ? (short) reader.u16() : reader.i32();
			points[i] = new Point2D.Double(x, y);
		}
		return points;
	}

	private void select(long handle) {
		if ((handle & STOCK_OBJECT) != 0) {
			selectStockObject((int) (handle & ~STOCK_OBJECT));
			return;
		}
		Object object = objects.get(handle);
		if (object instanceof Pen) {
			penColor = ((Pen) object).color;
			penWidth = ((Pen) object).width;
		}
		else if (object instanceof Brush)
			brushColor = ((Brush) object).color;
	}

	private void selectStockObject(int index) {
		switch (index) {
		case 0:
			brushColor = Color.WHITE;
			break;
		case 1:
			brushColor = Color.LIGHT_GRAY;
			break;
		case 2:
			brushColor = Color.GRAY;
			break;
		case 3:
			brushColor = Color.DARK_GRAY;
			break;
		case 4:
			brushColor = Color.BLACK;
			break;
		case 5:
			brushColor = null;
			break;
		case 6:
			penColor = Color.WHITE;
			penWidth = 1;
			break;
		case 7:
			penColor = Color.BLACK;
			penWidth = 1;
			break;
		case 8:
			penColor = null;
			break;
		default:
			break;
		}
	}

	private void fillAndStroke(Shape shape) {
		fill(shape);
		stroke(shape);
	}

	private void fill(Shape shape) {
		if (brushColor == null)
			return;
		graphics.setColor(brushColor);
		graphics.fill(toDevice(shape));
	}

	private void stroke(Shape shape) {
		if (penColor == null)
			return;
		graphics.setColor(penColor);
		graphics.setStroke(new BasicStroke((float) Math.max(1, penWidth * Math.abs(scaleX()))));
		graphics.draw(toDevice(shape));
	}

	private Shape toDevice(Shape shape) {
		AffineTransform transform = new AffineTransform();
		transform.translate(viewportOriginX, viewportOriginY);
		transform.scale(scaleX(), scaleY());
		transform.translate(-windowOriginX, -windowOriginY);
		return transform.createTransformedShape(shape);
	}

	private double scaleX() {
		return mapMode == MM_TEXT ? 1 : viewportExtentX / windowExtentX;
	}

	private double scaleY() {
		return mapMode == MM_TEXT ? 1 : viewportExtentY / windowExtentY;
	}

	private Color color() throws IOException {
		return new Color(reader.color());
	}

	private static double nonZero(int value) {
		return value == 0 ? 1 : value;
	}
}
