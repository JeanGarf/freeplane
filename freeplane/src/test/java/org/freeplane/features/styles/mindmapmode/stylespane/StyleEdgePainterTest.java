package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.freeplane.api.Dash;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.styles.mindmapmode.stylespane.StyleEdgePainter.Attachment;
import org.junit.Test;

public class StyleEdgePainterTest {
	private static StyleEdge edge(EdgeStyle style) {
		return new StyleEdge(Color.RED, 2, Dash.SOLID, style);
	}

	private static BufferedImage paint(StyleEdge edge, NodeStyleShape shape) {
		final BufferedImage image = new BufferedImage(40, 30, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();
		StyleEdgePainter.paint(g, edge, shape, 0, 20, 0, 30, 3, 24);
		g.dispose();
		return image;
	}

	private static boolean paintedAround(BufferedImage image, int x, int y) {
		for (int dy = -1; dy <= 1; dy++)
			if ((image.getRGB(x, y + dy) >>> 24) > 0)
				return true;
		return false;
	}

	@Test
	public void forkEdgeArrivesAtBottom() {
		assertThat(StyleEdgePainter.attachmentFor(NodeStyleShape.fork)).isEqualTo(Attachment.BOTTOM);
	}

	@Test
	public void otherShapesArriveInMiddle() {
		assertThat(StyleEdgePainter.attachmentFor(NodeStyleShape.bubble)).isEqualTo(Attachment.MIDDLE);
		assertThat(StyleEdgePainter.attachmentFor(NodeStyleShape.oval)).isEqualTo(Attachment.MIDDLE);
	}

	@Test
	public void computesArrivalHeightFromAttachment() {
		assertThat(StyleEdgePainter.attachmentY(NodeStyleShape.bubble, 3, 24)).isEqualTo(15);
		assertThat(StyleEdgePainter.attachmentY(NodeStyleShape.fork, 3, 24)).isEqualTo(25);
	}

	@Test
	public void linearEdgeEndsAtMiddleOfBubble() {
		assertThat(paintedAround(paint(edge(EdgeStyle.EDGESTYLE_LINEAR), NodeStyleShape.bubble), 19, 15)).isTrue();
	}

	@Test
	public void linearEdgeEndsAtBottomOfFork() {
		assertThat(paintedAround(paint(edge(EdgeStyle.EDGESTYLE_LINEAR), NodeStyleShape.fork), 19, 25)).isTrue();
	}

	@Test
	public void bezierEdgeEndsAtBottomOfFork() {
		assertThat(paintedAround(paint(edge(EdgeStyle.EDGESTYLE_BEZIER), NodeStyleShape.fork), 19, 25)).isTrue();
	}

	@Test
	public void hiddenEdgePaintsNothing() {
		final BufferedImage image = paint(edge(EdgeStyle.EDGESTYLE_HIDDEN), NodeStyleShape.bubble);

		assertThat(paintedAround(image, 10, 15)).isFalse();
		assertThat(StyleEdgePainter.isVisible(edge(EdgeStyle.EDGESTYLE_HIDDEN))).isFalse();
	}

	@Test
	public void paintsNothingWithoutEdge() {
		assertThat(paintedAround(paint(null, NodeStyleShape.bubble), 10, 15)).isFalse();
	}

	@Test
	public void usesEdgeColor() {
		final BufferedImage image = paint(edge(EdgeStyle.EDGESTYLE_LINEAR), NodeStyleShape.bubble);

		assertThat(image.getRGB(10, 15)).isEqualTo(Color.RED.getRGB());
	}
}
