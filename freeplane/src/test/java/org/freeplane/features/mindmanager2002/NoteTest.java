package org.freeplane.features.mindmanager2002;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Test;

public class NoteTest {
	private static final Charset ANSI = Charset.forName("windows-1252");

	@Test
	public void splitsParagraphsAndDropsTrailingEmptyOnes() {
		Note note = Note.decode("first\u0015\r\n\u0015\r\nthird\u0015\r\n\u0015\r\n".getBytes(ANSI));

		assertThat(note.paragraphs).containsExactly("first", "", "third");
	}

	@Test
	public void ignoresFormattingAfterMarker() {
		Note note = Note.decode("text\u0015\r\n~`!@#$%^&*()-+|=-TeCfMt\r\n\u0004\u0000garbage".getBytes(ANSI));

		assertThat(note.paragraphs).containsExactly("text");
		assertThat(note.images).isEmpty();
	}

	@Test
	public void lineFeedOnlyIsEmpty() {
		assertThat(Note.decode(new byte[] { '\n' }).isEmpty()).isTrue();
	}

	@Test
	public void extractsEmbeddedBitmap() throws Exception {
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		content.write("\u0018\u0015\r\n~`!@#$%^&*()-+|=-TeCfMt\r\n".getBytes(ANSI));
		content.write(new byte[] { 1, 2, 3 });
		content.write(dib(2, 1, 0xff0000, 0x0000ff));

		Note note = Note.decode(content.toByteArray());

		assertThat(note.paragraphs).containsExactly("\u0018");
		assertThat(note.images).hasSize(1);
		BufferedImage image = note.images.get(0);
		assertThat(image.getWidth()).isEqualTo(2);
		assertThat(image.getRGB(0, 0) & 0xffffff).isEqualTo(0xff0000);
		assertThat(image.getRGB(1, 0) & 0xffffff).isEqualTo(0x0000ff);
	}

	static byte[] dib(int width, int height, int... rgbs) {
		int rowSize = (width * 3 + 3) / 4 * 4;
		int pixelSize = rowSize * height;
		byte[] data = new byte[8 + pixelSize + 40];
		putU32(data, 0, pixelSize);
		putU32(data, 4, 40);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = rgbs[(height - 1 - y) * width + x];
				int offset = 8 + y * rowSize + x * 3;
				data[offset] = (byte) rgb;
				data[offset + 1] = (byte) (rgb >> 8);
				data[offset + 2] = (byte) (rgb >> 16);
			}
		}
		int info = 8 + pixelSize;
		putU32(data, info, 40);
		putU32(data, info + 4, width);
		putU32(data, info + 8, height);
		data[info + 12] = 1;
		data[info + 14] = 24;
		putU32(data, info + 20, pixelSize);
		return data;
	}

	private static void putU32(byte[] data, int position, int value) {
		data[position] = (byte) value;
		data[position + 1] = (byte) (value >> 8);
		data[position + 2] = (byte) (value >> 16);
		data[position + 3] = (byte) (value >> 24);
	}
}
