package org.freeplane.features.mindmanager2002;

class TopicEnd {
	private static final int START = 0x00;
	private static final int MARKER = 0x33;
	private static final int FIELD_COUNT = 4;
	private static final int MAX_FIELD_LENGTH = 4096;
	private static final int HEADER_SIZE = 2;
	private static final int FIELD_HEADER_SIZE = 5;

	static int find(ArchiveReader reader, int from) {
		for (int position = from; reader.byteAt(position) >= 0; position++) {
			if (lengthAt(reader, position) > 0)
				return position;
		}
		return -1;
	}

	static String field(ArchiveReader reader, int position, int wanted) {
		int end = position + HEADER_SIZE;
		for (int field = 1; field <= FIELD_COUNT; field++) {
			int length = reader.byteAt(end + 1) | reader.byteAt(end + 2) << 8 | reader.byteAt(end + 3) << 16
					| reader.byteAt(end + 4) << 24;
			if (field == wanted) {
				byte[] value = new byte[length];
				for (int i = 0; i < length; i++)
					value[i] = (byte) reader.byteAt(end + FIELD_HEADER_SIZE + i);
				return new String(value, ArchiveReader.ANSI).replace("\0", "");
			}
			end += FIELD_HEADER_SIZE + length;
		}
		return "";
	}

	static int lengthAt(ArchiveReader reader, int position) {
		if (reader.byteAt(position) != START || reader.byteAt(position + 1) != MARKER)
			return -1;
		int end = position + HEADER_SIZE;
		for (int field = 1; field <= FIELD_COUNT; field++) {
			if (reader.byteAt(end) != field)
				return -1;
			int length = reader.byteAt(end + 1) | reader.byteAt(end + 2) << 8 | reader.byteAt(end + 3) << 16
					| reader.byteAt(end + 4) << 24;
			if (length < 0 || length > MAX_FIELD_LENGTH || reader.byteAt(end + FIELD_HEADER_SIZE + length - 1) < 0)
				return -1;
			end += FIELD_HEADER_SIZE + length;
		}
		return end - position;
	}

	private TopicEnd() {
	}
}
