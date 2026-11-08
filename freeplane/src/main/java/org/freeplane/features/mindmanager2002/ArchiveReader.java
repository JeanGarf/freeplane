package org.freeplane.features.mindmanager2002;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class ArchiveReader {
	static final Charset ANSI = Charset.forName("windows-1252");
	private static final int UNICODE_MARKER = 0xfffe;

	private final byte[] data;
	private int position;

	ArchiveReader(byte[] data, int position) {
		this.data = data;
		this.position = position;
	}

	int position() {
		return position;
	}

	void seek(int position) {
		this.position = position;
	}

	boolean hasMore() {
		return position < data.length;
	}

	int u8() throws IOException {
		ensureAvailable(1);
		return data[position++] & 0xff;
	}

	int byteAt(int index) {
		return index >= 0 && index < data.length ? data[index] & 0xff : -1;
	}

	int peek() throws IOException {
		ensureAvailable(1);
		return data[position] & 0xff;
	}

	int u16() throws IOException {
		ensureAvailable(2);
		int value = (data[position] & 0xff) | (data[position + 1] & 0xff) << 8;
		position += 2;
		return value;
	}

	int i32() throws IOException {
		ensureAvailable(4);
		int value = (data[position] & 0xff) | (data[position + 1] & 0xff) << 8 | (data[position + 2] & 0xff) << 16
				| (data[position + 3] & 0xff) << 24;
		position += 4;
		return value;
	}

	long u32() throws IOException {
		return i32() & 0xffffffffL;
	}

	int length() throws IOException {
		long value = u32();
		if (value > data.length)
			throw new IOException("Invalid length " + value + " at " + (position - 4));
		return (int) value;
	}

	byte[] bytes(int count) throws IOException {
		ensureAvailable(count);
		byte[] value = Arrays.copyOfRange(data, position, position + count);
		position += count;
		return value;
	}

	void skip(int count) throws IOException {
		ensureAvailable(count);
		position += count;
	}

	String string() throws IOException {
		int length = u8();
		if (length < 0xff)
			return new String(bytes(length), ANSI);
		length = u16();
		if (length == UNICODE_MARKER) {
			length = u8();
			if (length == 0xff)
				length = u16();
			return new String(bytes(2 * length), StandardCharsets.UTF_16LE);
		}
		if (length == 0xffff)
			length = length();
		return new String(bytes(length), ANSI);
	}

	int color() throws IOException {
		int red = u8();
		int green = u8();
		int blue = u8();
		u8();
		return red << 16 | green << 8 | blue;
	}

	void expect(int... expected) throws IOException {
		int start = position;
		for (int value : expected) {
			if (u8() != value)
				throw new IOException("Unexpected MindManager 2002 content at offset " + start);
		}
	}

	int indexOf(byte[] pattern, int from) {
		outer: for (int i = from; i <= data.length - pattern.length; i++) {
			for (int j = 0; j < pattern.length; j++) {
				if (data[i + j] != pattern[j])
					continue outer;
			}
			return i;
		}
		return -1;
	}

	private void ensureAvailable(int count) throws IOException {
		if (count < 0 || position + count > data.length)
			throw new IOException("Unexpected end of MindManager 2002 file at offset " + position);
	}
}
