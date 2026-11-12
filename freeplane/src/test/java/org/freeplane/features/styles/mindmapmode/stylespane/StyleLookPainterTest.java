package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.freeplane.features.nodestyle.NodeStyleShape;
import org.junit.Test;

public class StyleLookPainterTest {
	private static StyleLook look(NodeStyleShape shape, Color background, Color border) {
		return new StyleLook(background, Color.BLACK, border, "Serif", true, false, shape);
	}

	private static BufferedImage paint(StyleLook look) {
		final BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();
		StyleLookPainter.paintBackground(g, look, 0, 0, 100, 30);
		g.dispose();
		return image;
	}

	@Test
	public void fillsShapeWithBackgroundColor() {
		final BufferedImage image = paint(look(NodeStyleShape.bubble, Color.YELLOW, null));

		assertThat(image.getRGB(50, 15)).isEqualTo(Color.YELLOW.getRGB());
	}

	@Test
	public void leavesRoundedCornersTransparent() {
		final BufferedImage image = paint(look(NodeStyleShape.bubble, Color.YELLOW, null));

		assertThat(image.getRGB(0, 0) >>> 24).isZero();
		assertThat(image.getRGB(99, 29) >>> 24).isZero();
	}

	@Test
	public void rectangleFillsCorners() {
		final BufferedImage image = paint(look(NodeStyleShape.rectangle, Color.YELLOW, null));

		assertThat(image.getRGB(2, 2)).isEqualTo(Color.YELLOW.getRGB());
	}

	@Test
	public void drawsBorderAroundShape() {
		final BufferedImage image = paint(look(NodeStyleShape.rectangle, Color.YELLOW, Color.RED));

		assertThat(image.getRGB(50, 0) >>> 24).isPositive();
		assertThat(image.getRGB(50, 15)).isEqualTo(Color.YELLOW.getRGB());
	}

	@Test
	public void forkOnlyUnderlinesWithBorderColor() {
		final BufferedImage image = paint(look(NodeStyleShape.fork, null, Color.BLUE));

		assertThat(image.getRGB(50, 15) >>> 24).isZero();
		assertThat(image.getRGB(50, 28)).isEqualTo(Color.BLUE.getRGB());
	}

	@Test
	public void unfilledShapeWithoutBorderPaintsNothing() {
		final BufferedImage image = paint(look(NodeStyleShape.bubble, null, null));

		assertThat(image.getRGB(50, 15) >>> 24).isZero();
	}

	@Test
	public void derivesFontFromStyleKeepingBaseSize() {
		final Font font = StyleLookPainter.fontFor(look(NodeStyleShape.bubble, null, null), new Font("SansSerif", Font.PLAIN, 13));

		assertThat(font.isBold()).isTrue();
		assertThat(font.isItalic()).isFalse();
		assertThat(font.getSize()).isEqualTo(13);
	}

	@Test
	public void insetsTextMoreForShapesThatCutCorners() {
		assertThat(StyleLookPainter.textInset(NodeStyleShape.oval, 24)).isGreaterThan(StyleLookPainter.textInset(NodeStyleShape.bubble, 24));
		assertThat(StyleLookPainter.textInset(NodeStyleShape.rectangle, 24)).isEqualTo(StyleLookPainter.textInset(NodeStyleShape.fork, 24));
	}
}
