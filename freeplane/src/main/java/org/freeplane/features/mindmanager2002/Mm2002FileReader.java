package org.freeplane.features.mindmanager2002;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class Mm2002FileReader {
	static final String MAIN_TOPIC_CLASS = "TopicC";
	static final String TOPIC_CLASS = "ItemC";
	static final String SYMBOL_CLASS = "FreeSymbolC";
	private static final int NEW_CLASS_TAG = 0xffff;
	private static final int CLASS_TAG = 0x8000;
	private static final int VERSION_MARKER = 0x32;
	private static final int TOPIC_MARKER = 0x33;
	private static final int FLAG_PRIORITY = 0x02;
	private static final int STYLE_BOLD = 0x01;
	private static final int STYLE_ITALIC = 0x02;
	private static final int STYLE_FILLED = 0x08;
	private static final int ICONS_PER_MASK = 32;
	private static final byte[] TOPIC_END = { 0x00, 0x33, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00,
			0x00, 0x00, 0x03, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00 };
	private static final int TOPIC_TRAILER_SIZE = 6;
	private static final int MAIN_TOPIC_TRAILER_SIZE = 10;
	private static final int MAIN_TOPIC_SIDE_OFFSET = 5;
	private static final int SINGLE_ARROW = 1;
	private static final int MAIN_TOPIC_COUNT_OFFSET = 7;
	private static final long MAX_NOTE_SIZE = 256L * 1024 * 1024;

	private final ArchiveReader reader;
	private final Map<Integer, String> classes = new HashMap<>();
	private int nextIndex = 1;

	private Mm2002FileReader(ArchiveReader reader) {
		this.reader = reader;
	}

	static Mm2002Map read(byte[] data) throws IOException {
		ArchiveReader header = new ArchiveReader(data, 0);
		if (data.length == 0 || header.u8() != VERSION_MARKER)
			throw new IOException("Not a MindManager 2002 file");
		Mm2002Map map = new Mm2002Map(header.string());
		int mainTopicTag = header.indexOf(newClassTag(MAIN_TOPIC_CLASS), 0);
		if (mainTopicTag < 0)
			return map;
		ArchiveReader reader = new ArchiveReader(data, mainTopicTag - MAIN_TOPIC_COUNT_OFFSET);
		reader.expect(0xff);
		int count = reader.u16();
		Mm2002FileReader fileReader = new Mm2002FileReader(reader);
		for (int i = 0; i < count; i++)
			map.mainTopics.add(fileReader.topic());
		return map;
	}

	private static byte[] newClassTag(String className) {
		byte[] name = className.getBytes(StandardCharsets.US_ASCII);
		byte[] tag = new byte[name.length + 2];
		tag[0] = (byte) name.length;
		System.arraycopy(name, 0, tag, 2, name.length);
		return tag;
	}

	private Topic topic() throws IOException {
		String className = objectClass();
		if (!MAIN_TOPIC_CLASS.equals(className) && !TOPIC_CLASS.equals(className))
			throw new IOException("Unsupported MindManager 2002 object " + className + " at offset " + reader.position());
		Topic topic = new Topic();
		reader.expect(TOPIC_MARKER);
		topic.text = reader.string();
		reader.skip(8 + 4);
		int flags = reader.i32();
		reader.skip(13);
		reader.string();
		int priority = reader.i32();
		if ((flags & FLAG_PRIORITY) != 0)
			topic.priority = priority;
		int objectCount = reader.i32();
		for (int i = 0; i < objectCount; i++)
			attachedObject(topic);
		readStyle(topic);
		reader.expect(VERSION_MARKER, 0xff);
		reader.skip(3);
		topic.id = reader.u32();
		topic.note = Note.decode(blob());
		reader.expect(0x00, VERSION_MARKER, 0xff);
		int relationshipCount = reader.i32();
		for (int i = 0; i < relationshipCount; i++)
			topic.relationships.add(relationship());
		reader.skip(4);
		addIcons(topic, reader.i32(), 0);
		reader.skip(31);
		link();
		reader.skip(22);
		addIcons(topic, reader.i32(), ICONS_PER_MASK);
		topic.hyperlink = link();
		reader.skip(4);
		reader.expect(toInts(TOPIC_END));
		reader.expect(0xff);
		int childCount = reader.u16();
		for (int i = 0; i < childCount; i++)
			topic.children.add(topic());
		reader.skip(TOPIC_TRAILER_SIZE);
		if (MAIN_TOPIC_CLASS.equals(className)) {
			byte[] trailer = reader.bytes(MAIN_TOPIC_TRAILER_SIZE);
			topic.left = trailer[MAIN_TOPIC_SIDE_OFFSET] == 1;
		}
		return topic;
	}

	private void readStyle(Topic topic) throws IOException {
		reader.expect(VERSION_MARKER);
		int style = reader.u16();
		topic.bold = (style & STYLE_BOLD) != 0;
		topic.italic = (style & STYLE_ITALIC) != 0;
		topic.filled = (style & STYLE_FILLED) != 0;
		topic.lineColor = reader.color();
		topic.fillColor = reader.color();
		int fontReference = reader.u16();
		if (fontReference == 0) {
			topic.fontSize = reader.i32();
			topic.fontName = reader.string();
			reader.skip(2);
		}
		reader.skip(1);
	}

	private void attachedObject(Topic topic) throws IOException {
		String className = objectClass();
		if (!SYMBOL_CLASS.equals(className))
			throw new IOException("Unsupported MindManager 2002 object " + className + " at offset " + reader.position());
		reader.skip(3 + 4 * 4 + 2 + 2 * 4 + 2 + 4 + 16);
		link();
		reader.expect(0x00, VERSION_MARKER, 0xff, VERSION_MARKER);
		reader.expect(VERSION_MARKER);
		int size = reader.length();
		reader.skip(4 + 1);
		topic.symbols.add(reader.bytes(size));
		reader.skip(16);
	}

	private Relationship relationship() throws IOException {
		reader.expect(VERSION_MARKER);
		long id = reader.u32();
		reader.skip(4 * 4 + 4);
		int color = reader.color();
		reader.skip(4);
		boolean arrowAtBothEnds = reader.i32() != SINGLE_ARROW;
		reader.skip(4);
		link();
		String label = reader.string();
		reader.skip(1);
		reader.expect(VERSION_MARKER, 0x01);
		int size = reader.length();
		int end = reader.position() + size;
		registerEmbeddedObjects(size);
		reader.seek(end);
		reader.expect(0xff);
		long targetId = reader.u32();
		long sourceId = reader.u32();
		return new Relationship(id, sourceId, targetId, label, color, arrowAtBothEnds);
	}

	private void registerEmbeddedObjects(int size) throws IOException {
		if (size < 4)
			return;
		int count = reader.i32();
		if (count > 0)
			objectClass();
	}

	private String link() throws IOException {
		reader.expect(VERSION_MARKER);
		String link = reader.string();
		reader.skip(4);
		reader.expect(VERSION_MARKER, 0xff);
		return link;
	}

	private byte[] blob() throws IOException {
		reader.expect(VERSION_MARKER);
		long uncompressedSize = reader.u32();
		int compressedSize = reader.length();
		if (uncompressedSize > MAX_NOTE_SIZE)
			throw new IOException("Invalid MindManager 2002 note size at offset " + reader.position());
		if (compressedSize == 0)
			return reader.bytes((int) uncompressedSize);
		int chunkCount = reader.length();
		int[] chunkSizes = new int[chunkCount];
		for (int i = 0; i < chunkCount; i++) {
			reader.skip(4);
			chunkSizes[i] = reader.length();
		}
		reader.skip(8);
		ByteArrayOutputStream content = new ByteArrayOutputStream((int) uncompressedSize);
		for (int chunkSize : chunkSizes)
			content.write(inflate(reader.bytes(chunkSize)));
		return content.toByteArray();
	}

	private static byte[] inflate(byte[] compressed) throws IOException {
		Inflater inflater = new Inflater();
		inflater.setInput(compressed);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		try {
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				if (count == 0 && (inflater.needsInput() || inflater.needsDictionary()))
					break;
				output.write(buffer, 0, count);
			}
		}
		catch (DataFormatException e) {
			throw new IOException("Corrupted MindManager 2002 note", e);
		}
		finally {
			inflater.end();
		}
		return output.toByteArray();
	}

	private String objectClass() throws IOException {
		int tag = reader.u16();
		String className;
		if (tag == NEW_CLASS_TAG) {
			reader.skip(2);
			int nameLength = reader.u16();
			className = new String(reader.bytes(nameLength), StandardCharsets.US_ASCII);
			classes.put(nextIndex++, className);
		}
		else if ((tag & CLASS_TAG) != 0) {
			className = classes.get(tag & ~CLASS_TAG);
			if (className == null)
				throw new IOException("Unknown MindManager 2002 class reference at offset " + reader.position());
		}
		else
			throw new IOException("Unsupported MindManager 2002 object reference at offset " + reader.position());
		nextIndex++;
		return className;
	}

	private static void addIcons(Topic topic, int mask, int firstIndex) {
		for (int bit = 0; bit < ICONS_PER_MASK; bit++) {
			if ((mask & 1 << bit) != 0)
				topic.iconIndexes.add(firstIndex + bit);
		}
	}

	private static int[] toInts(byte[] bytes) {
		int[] ints = new int[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			ints[i] = bytes[i] & 0xff;
		return ints;
	}
}
