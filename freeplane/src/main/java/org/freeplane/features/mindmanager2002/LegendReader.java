package org.freeplane.features.mindmanager2002;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class LegendReader {
	private static final int MIN_ICON_COUNT = 30;
	private static final int MAX_ICON_COUNT = 64;
	private static final int MAX_NAME_LENGTH = 80;
	private static final int PALETTE_COLOR_FLAG = 0x02;

	static Map<Integer, String> read(byte[] data, int headerEnd) {
		Map<Integer, String> legend = new LinkedHashMap<>();
		int legendStart = legendStart(data, headerEnd);
		if (legendStart < 0)
			return legend;
		ArchiveReader reader = new ArchiveReader(data, legendStart);
		try {
			while (reader.position() < headerEnd) {
				String name = name(reader);
				if (name == null)
					break;
				int red = reader.u8();
				int green = reader.u8();
				int blue = reader.u8();
				int flag = reader.u8();
				int separator = reader.u8();
				if (flag != PALETTE_COLOR_FLAG || separator > 1)
					break;
				int rgb = red << 16 | green << 8 | blue;
				if (!name.trim().isEmpty())
					legend.putIfAbsent(rgb, name.trim());
			}
		}
		catch (IOException e) {
			return legend;
		}
		return legend;
	}

	private static int legendStart(byte[] data, int headerEnd) {
		for (int position = 0; position + 4 < headerEnd; position++) {
			ArchiveReader reader = new ArchiveReader(data, position);
			try {
				long count = reader.u32();
				if (count < MIN_ICON_COUNT || count > MAX_ICON_COUNT)
					continue;
				boolean valid = true;
				for (int i = 0; i < count && valid; i++) {
					String iconName = name(reader);
					valid = iconName != null && !iconName.isEmpty();
				}
				if (valid)
					return reader.position();
			}
			catch (IOException e) {
				continue;
			}
		}
		return -1;
	}

	private static String name(ArchiveReader reader) throws IOException {
		int length = reader.u8();
		if (length > MAX_NAME_LENGTH)
			return null;
		byte[] bytes = reader.bytes(length);
		for (byte character : bytes) {
			if ((character & 0xff) < 0x20)
				return null;
		}
		return new String(bytes, ArchiveReader.ANSI);
	}
}
