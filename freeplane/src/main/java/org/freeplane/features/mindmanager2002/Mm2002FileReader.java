package org.freeplane.features.mindmanager2002;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class Mm2002FileReader {
	static final String MAIN_TOPIC_CLASS = "TopicC";
	static final String TOPIC_CLASS = "ItemC";
	static final String SYMBOL_CLASS = "FreeSymbolC";
	static final String BOUNDARY_CLASS = "BoundaryC";
	static final String METAFILE_CLASS = "EnhMetafileC";
	private static final int RAW_IMAGE = 1;
	private static final int COMPRESSED_METAFILE = 2;
	private static final int RAW_IMAGE_TRAILER_SIZE = 8;
	private static final int MAX_PROPERTIES = 10000;
	private static final int MAX_RELATIONSHIPS = 1000;
	private static final int NOTE_HEADER_OFFSET = 9;
	private static final int MAX_RECOVERED_LINK_LENGTH = 4096;
	private static final int NULL_OBJECT = 0;
	private static final int PRIORITY_MASK = 0xff;
	private static final int NEW_CLASS_TAG = 0xffff;
	private static final int CLASS_TAG = 0x8000;
	private static final int VERSION_MARKER = 0x32;
	private static final int TOPIC_MARKER = 0x33;
	private static final int FLAG_PRIORITY = 0x02;
	private static final int FLAG_HIDDEN = 0x80;
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
	private final Deque<String> branch = new ArrayDeque<>();
	private ReadStage stage;
	private final List<ImportProblem> problems;
	private int nextIndex = 1;

	private Mm2002FileReader(ArchiveReader reader, List<ImportProblem> problems) {
		this.reader = reader;
		this.problems = problems;
	}

	static Mm2002Map read(byte[] data) throws IOException {
		ArchiveReader header = new ArchiveReader(data, 0);
		if (data.length == 0 || header.u8() != VERSION_MARKER)
			throw new IOException("Not a MindManager 2002 file");
		Mm2002Map map = new Mm2002Map(header.string());
		int mainTopicTag = header.indexOf(newClassTag(MAIN_TOPIC_CLASS), 0);
		map.legend.putAll(LegendReader.read(data, mainTopicTag < 0 ? data.length : mainTopicTag));
		if (mainTopicTag < 0)
			return map;
		ArchiveReader reader = new ArchiveReader(data, mainTopicTag - MAIN_TOPIC_COUNT_OFFSET);
		reader.expect(0xff);
		int count = reader.u16();
		Mm2002FileReader fileReader = new Mm2002FileReader(reader, map.problems);
		try {
			for (int i = 0; i < count; i++)
				map.mainTopics.add(fileReader.topic());
		}
		catch (IOException e) {
			fileReader.problem(ImportProblem.Kind.REST_IGNORED, null, e, null, EnumSet.noneOf(ReadStage.class));
		}
		map.sortMainTopicsFromTopToBottom();
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
		String className = topicClass();
		Topic topic = new Topic();
		int contentStart = reader.position();
		int contentEnd = reader.indexOf(TOPIC_END, contentStart);
		if (contentEnd < 0)
			readForward(topic, EnumSet.noneOf(ReadStage.class));
		else
			readContent(topic, contentStart, contentEnd);
		reader.expect(0xff);
		int childCount = reader.u16();
		branch.addLast(topic.text);
		try {
			for (int i = 0; i < childCount; i++)
				topic.children.add(topic());
		}
		finally {
			branch.removeLast();
		}
		reader.skip(TOPIC_TRAILER_SIZE);
		if (MAIN_TOPIC_CLASS.equals(className)) {
			byte[] trailer = reader.bytes(MAIN_TOPIC_TRAILER_SIZE);
			topic.left = trailer[MAIN_TOPIC_SIDE_OFFSET] == 1;
		}
		return topic;
	}

	private String topicClass() throws IOException {
		int tag = reader.u16();
		if (isUnknownClassReference(tag) && reader.peek() == TOPIC_MARKER) {
			nextIndex++;
			return TOPIC_CLASS;
		}
		String className = objectClass(tag);
		if (!MAIN_TOPIC_CLASS.equals(className) && !TOPIC_CLASS.equals(className))
			throw new IOException("Unsupported MindManager 2002 object " + className + " at offset " + reader.position());
		return className;
	}

	private void readContent(Topic topic, int contentStart, int contentEnd) throws IOException {
		EnumSet<ReadStage> read = EnumSet.noneOf(ReadStage.class);
		IOException failure = null;
		ReadStage failedStage = null;
		try {
			try {
				readUpToNote(topic, read);
			}
			catch (IOException e) {
				failure = e;
				failedStage = stage;
				if (!seekToNote(topic, contentStart, contentEnd))
					throw e;
			}
			readFromNote(topic, read);
		}
		catch (IOException e) {
			if (failure == null) {
				failure = e;
				failedStage = stage;
			}
			recoverFromEnd(topic, read, contentEnd);
		}
		if (failure != null) {
			EnumSet<ReadStage> lost = EnumSet.complementOf(read);
			lost.remove(ReadStage.PROPERTIES);
			problem(ImportProblem.Kind.PARTIAL_TOPIC, topic, failure, failedStage, lost);
		}
		reader.seek(contentEnd + TOPIC_END.length);
	}

	private void readForward(Topic topic, EnumSet<ReadStage> read) throws IOException {
		readUpToNote(topic, read);
		readFromNote(topic, read);
	}

	private void readUpToNote(Topic topic, EnumSet<ReadStage> read) throws IOException {
		stage = ReadStage.TEXT;
		reader.expect(TOPIC_MARKER);
		topic.text = reader.string();
		topic.x = reader.i32();
		topic.y = reader.i32();
		reader.skip(4);
		int flags = reader.i32();
		topic.hidden = (flags & FLAG_HIDDEN) != 0;
		topic.imageWidth = reader.i32();
		topic.imageHeight = reader.i32();
		reader.skip(5);
		reader.string();
		int priority = reader.i32() & PRIORITY_MASK;
		if ((flags & FLAG_PRIORITY) != 0)
			topic.priority = priority;
		read.add(ReadStage.TEXT);
		stage = ReadStage.ATTACHED_OBJECTS;
		int objectCount = reader.i32();
		for (int i = 0; i < objectCount; i++)
			attachedObject(topic);
		read.add(ReadStage.ATTACHED_OBJECTS);
		stage = ReadStage.STYLE;
		readStyle(topic);
		reader.expect(VERSION_MARKER, 0xff);
		reader.skip(3);
		topic.id = reader.u32();
		read.add(ReadStage.STYLE);
	}

	private void readFromNote(Topic topic, EnumSet<ReadStage> read) throws IOException {
		stage = ReadStage.NOTE;
		topic.note = Note.decode(blob());
		reader.expect(0x00, VERSION_MARKER, 0xff);
		read.add(ReadStage.NOTE);
		stage = ReadStage.RELATIONSHIPS;
		int relationshipCount = reader.i32();
		for (int i = 0; i < relationshipCount; i++)
			topic.relationships.add(relationship());
		read.add(ReadStage.RELATIONSHIPS);
		stage = ReadStage.ICONS;
		reader.skip(4);
		addIcons(topic, reader.i32(), 0);
		read.add(ReadStage.ICONS);
		reader.skip(29);
		stage = ReadStage.CLOUD;
		int boundaryTag = reader.u16();
		if (boundaryTag != NULL_OBJECT)
			boundary(topic, boundaryTag);
		read.add(ReadStage.CLOUD);
		stage = ReadStage.PROPERTIES;
		properties();
		read.add(ReadStage.PROPERTIES);
		stage = ReadStage.IMAGE;
		reader.skip(16);
		int imageTag = reader.u16();
		if (imageTag != NULL_OBJECT)
			topicImage(topic, imageTag);
		reader.skip(4);
		read.add(ReadStage.IMAGE);
		stage = ReadStage.HYPERLINK;
		addIcons(topic, reader.i32(), ICONS_PER_MASK);
		topic.hyperlink = link();
		reader.skip(4);
		reader.expect(toInts(TOPIC_END));
		read.add(ReadStage.HYPERLINK);
	}

	private boolean seekToNote(Topic topic, int from, int to) {
		for (int position = from; position + NOTE_HEADER_OFFSET < to; position++) {
			if (reader.byteAt(position) != VERSION_MARKER || reader.byteAt(position + 1) != 0xff
					|| reader.byteAt(position + NOTE_HEADER_OFFSET) != VERSION_MARKER)
				continue;
			try {
				reader.seek(position + 5);
				long id = reader.u32();
				int noteStart = reader.position();
				blob();
				reader.expect(0x00, VERSION_MARKER, 0xff);
				int relationshipCount = reader.i32();
				if (relationshipCount < 0 || relationshipCount > MAX_RELATIONSHIPS)
					continue;
				topic.id = id;
				reader.seek(noteStart);
				return true;
			}
			catch (IOException e) {
				continue;
			}
		}
		return false;
	}

	private void recoverFromEnd(Topic topic, EnumSet<ReadStage> read, int contentEnd) {
		if (read.contains(ReadStage.HYPERLINK))
			return;
		int linkEnd = contentEnd - 4;
		if (linkEnd < 2 || reader.byteAt(linkEnd - 2) != VERSION_MARKER || reader.byteAt(linkEnd - 1) != 0xff)
			return;
		int textEnd = linkEnd - 2 - 4;
		for (int start = textEnd - 2; start >= 4 && start >= textEnd - MAX_RECOVERED_LINK_LENGTH; start--) {
			if (reader.byteAt(start) != VERSION_MARKER)
				continue;
			try {
				reader.seek(start + 1);
				String hyperlink = reader.string();
				if (reader.position() != textEnd || hyperlink.chars().anyMatch(character -> character < ' '))
					continue;
				topic.hyperlink = hyperlink;
				reader.seek(start - 4);
				addIcons(topic, reader.i32(), ICONS_PER_MASK);
				read.add(ReadStage.HYPERLINK);
				return;
			}
			catch (IOException e) {
				continue;
			}
		}
	}

	private void properties() throws IOException {
		reader.expect(VERSION_MARKER);
		int count = reader.i32();
		if (count < 0 || count > MAX_PROPERTIES)
			throw new IOException("Invalid MindManager 2002 property count at offset " + reader.position());
		for (int i = 0; i < count; i++) {
			reader.skip(4);
			reader.skip(reader.length());
		}
		reader.skip(1);
		reader.expect(VERSION_MARKER, 0xff);
	}

	private void topicImage(Topic topic, int tag) throws IOException {
		String className = objectClass(tag);
		if (!METAFILE_CLASS.equals(className))
			throw new IOException("Unsupported MindManager 2002 object " + className + " at offset " + reader.position());
		reader.expect(VERSION_MARKER);
		long size = reader.u32();
		int type = reader.u8();
		if (type == RAW_IMAGE) {
			topic.pictures.add(new Picture(reader.bytes((int) Math.min(size, Integer.MAX_VALUE)), topic.imageWidth,
					topic.imageHeight));
			reader.skip(RAW_IMAGE_TRAILER_SIZE);
		}
		else if (type == COMPRESSED_METAFILE)
			topic.pictures.add(new Picture(chunks(size), topic.imageWidth, topic.imageHeight));
		else
			throw new IOException("Unsupported MindManager 2002 image type " + type + " at offset " + reader.position());
		reader.expect(0x00, VERSION_MARKER, 0xff);
	}

	private void boundary(Topic topic, int tag) throws IOException {
		String className = objectClass(tag);
		if (!BOUNDARY_CLASS.equals(className))
			throw new IOException("Unsupported MindManager 2002 object " + className + " at offset " + reader.position());
		reader.skip(3 + 4);
		reader.expect(VERSION_MARKER, 0x01);
		int size = reader.length();
		int end = reader.position() + size;
		if (size >= 4)
			topic.cloudColor = reader.color();
		reader.seek(end);
		reader.expect(0xff);
	}

	private void problem(ImportProblem.Kind kind, Topic topic, IOException cause, ReadStage failedStage,
			Set<ReadStage> lost) {
		StringBuilder path = new StringBuilder();
		for (String ancestor : branch)
			path.append(ancestor.replaceAll("\\s+", " ").trim()).append(" > ");
		path.append(topic == null || topic.text.isEmpty() ? "?" : topic.text.replaceAll("\\s+", " ").trim());
		problems.add(new ImportProblem(kind, path.toString(), cause.getMessage(), failedStage, lost));
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
		reader.skip(3 + 4 * 4 + 2);
		int width = Math.abs(reader.i32());
		int height = Math.abs(reader.i32());
		reader.skip(2 + 4 + 16);
		properties();
		reader.expect(0x00, VERSION_MARKER, 0xff, VERSION_MARKER);
		reader.expect(VERSION_MARKER);
		int size = reader.length();
		reader.skip(4 + 1);
		topic.pictures.add(new Picture(reader.bytes(size), width, height));
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
		return chunks(uncompressedSize, compressedSize);
	}

	private byte[] chunks(long uncompressedSize) throws IOException {
		return chunks(uncompressedSize, reader.length());
	}

	private byte[] chunks(long uncompressedSize, int compressedSize) throws IOException {
		if (uncompressedSize > MAX_NOTE_SIZE)
			throw new IOException("Invalid MindManager 2002 data size at offset " + reader.position());
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
		return objectClass(reader.u16());
	}

	private boolean isUnknownClassReference(int tag) {
		return tag != NEW_CLASS_TAG && (tag & CLASS_TAG) != 0 && !classes.containsKey(tag & ~CLASS_TAG);
	}

	private String objectClass(int tag) throws IOException {
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
			if ((mask & 1 << bit) != 0 && !topic.iconIndexes.contains(firstIndex + bit))
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
