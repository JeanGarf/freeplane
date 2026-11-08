package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class EmfRendererTest {
	private final ByteArrayOutputStream emf = new ByteArrayOutputStream();

	@Test
	public void rendersFilledPolygonWithSelectedBrush() throws Exception {
		header(0, 0, 19, 9);
		record(39, 1, 0, 0x000000ff, 0);
		record(37, 1);
		record(37, 0x80000008);
		polygon16(new short[] { 0, 0, 9, 0, 9, 9, 0, 9 });
		record(14, 0, 0, 0);

		BufferedImage image = EmfRenderer.render(emf.toByteArray());

		assertThat(image.getWidth()).isEqualTo(20);
		assertThat(image.getHeight()).isEqualTo(10);
		assertThat(image.getRGB(4, 4)).isEqualTo(0xffff0000);
		assertThat(image.getRGB(15, 4) >>> 24).isZero();
	}

	@Test
	public void appliesWindowToViewportScaling() throws Exception {
		header(0, 0, 9, 9);
		record(17, 8);
		record(9, 100, 100);
		record(11, 10, 10);
		record(39, 1, 0, 0x0000ff00, 0);
		record(37, 1);
		record(37, 0x80000008);
		polygon16(new short[] { 0, 0, 50, 0, 50, 50, 0, 50 });
		record(14, 0, 0, 0);

		BufferedImage image = EmfRenderer.render(emf.toByteArray());

		assertThat(image.getRGB(2, 2)).isEqualTo(0xff00ff00);
		assertThat(image.getRGB(8, 8) >>> 24).isZero();
	}

	@Test
	public void rejectsOtherData() {
		assertThatThrownBy(() -> EmfRenderer.render(new byte[] { 2, 0, 0, 0, 8, 0, 0, 0 }))
				.isInstanceOf(IOException.class);
	}

	private void header(int left, int top, int right, int bottom) {
		int[] values = new int[22];
		values[0] = left;
		values[1] = top;
		values[2] = right;
		values[3] = bottom;
		record(1, values);
	}

	private void polygon16(short[] points) {
		u32(86);
		u32(8 + 16 + 4 + points.length * 2);
		for (int i = 0; i < 4; i++)
			u32(0);
		u32(points.length / 2);
		for (short point : points) {
			emf.write(point);
			emf.write(point >> 8);
		}
	}

	private void record(int type, int... values) {
		u32(type);
		u32(8 + values.length * 4);
		for (int value : values)
			u32(value);
	}

	private void u32(int value) {
		emf.write(value);
		emf.write(value >> 8);
		emf.write(value >> 16);
		emf.write(value >> 24);
	}
}
