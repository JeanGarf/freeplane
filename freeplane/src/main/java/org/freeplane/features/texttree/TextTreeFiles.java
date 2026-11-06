package org.freeplane.features.texttree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextTreeFiles {
	private static final byte[] UTF8_BYTE_ORDER_MARK = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
	private static final Charset LEGACY_WINDOWS_CHARSET = Charset.forName("windows-1252");

	public static void write(final File file, final List<String> lines, final boolean forWindows) throws IOException {
		final String lineSeparator = forWindows ? "\r\n" : "\n";
		final StringBuilder content = new StringBuilder();
		for (final String line : lines)
			content.append(line).append(lineSeparator);
		final byte[] text = content.toString().getBytes(StandardCharsets.UTF_8);
		final byte[] bytes;
		if (forWindows) {
			bytes = Arrays.copyOf(UTF8_BYTE_ORDER_MARK, UTF8_BYTE_ORDER_MARK.length + text.length);
			System.arraycopy(text, 0, bytes, UTF8_BYTE_ORDER_MARK.length, text.length);
		}
		else
			bytes = text;
		Files.write(file.toPath(), bytes);
	}

	public static List<String> read(final File file) throws IOException {
		return lines(decode(Files.readAllBytes(file.toPath())));
	}

	static String decode(final byte[] bytes) {
		if (startsWithByteOrderMark(bytes))
			return new String(bytes, UTF8_BYTE_ORDER_MARK.length, bytes.length - UTF8_BYTE_ORDER_MARK.length,
				StandardCharsets.UTF_8);
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
		}
		catch (final CharacterCodingException e) {
			return new String(bytes, LEGACY_WINDOWS_CHARSET);
		}
	}

	private static boolean startsWithByteOrderMark(final byte[] bytes) {
		if (bytes.length < UTF8_BYTE_ORDER_MARK.length)
			return false;
		for (int index = 0; index < UTF8_BYTE_ORDER_MARK.length; index++) {
			if (bytes[index] != UTF8_BYTE_ORDER_MARK[index])
				return false;
		}
		return true;
	}

	private static List<String> lines(final String text) {
		return new ArrayList<String>(Arrays.asList(text.split("\r\n|\r|\n")));
	}

	public static File textFileOf(final File mapFile) {
		return new File(mapFile.getParentFile(), baseNameOf(mapFile) + ".txt");
	}

	public static String baseNameOf(final File file) {
		final String name = file.getName();
		final int extension = name.lastIndexOf('.');
		return extension > 0 ? name.substring(0, extension) : name;
	}

	private TextTreeFiles() {
	}
}
