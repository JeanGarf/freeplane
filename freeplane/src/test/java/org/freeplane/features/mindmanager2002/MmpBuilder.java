package org.freeplane.features.mindmanager2002;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

class MmpBuilder {
	private static final Charset ANSI = Charset.forName("windows-1252");

	static class TopicSpec {
		final String text;
		long id;
		boolean left;
		int style;
		int lineColor = 0xffcc00;
		int fillColor = 0xffff00;
		String fontName;
		int fontSize;
		int priority;
		int iconMask;
		int secondIconMask;
		String hyperlink = "";
		byte[] note;
		boolean compressNote;
		byte[] symbol;
		int cloudColor = -1;
		int priorityHighBits;
		boolean unknownObject;
		final List<byte[]> relationships = new ArrayList<>();
		final List<TopicSpec> children = new ArrayList<>();

		TopicSpec(String text, long id) {
			this.text = text;
			this.id = id;
		}

		TopicSpec child(TopicSpec child) {
			children.add(child);
			return this;
		}
	}

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private boolean mainTopicClassWritten;
	private boolean topicClassWritten;
	private boolean symbolClassWritten;
	private int nextIndex = 1;
	private int mainTopicClassIndex;
	private int topicClassIndex;
	private int symbolClassIndex;

	static byte[] build(String title, String[] iconNames, String[][] legend, List<TopicSpec> mainTopics) {
		MmpBuilder builder = new MmpBuilder();
		builder.u8(0x32);
		builder.string(title);
		builder.zeros(12);
		builder.u32(8);
		builder.u32(iconNames.length);
		for (String iconName : iconNames)
			builder.string(iconName);
		for (String[] entry : legend) {
			builder.string(entry[0]);
			builder.color(Integer.parseInt(entry[1], 16));
			builder.u8(1);
		}
		builder.u8(0x32);
		builder.u8(0xff);
		builder.zeros(8);
		builder.u8(0xff);
		builder.u16(mainTopics.size());
		for (TopicSpec topic : mainTopics)
			builder.topic(topic, true);
		builder.zeros(16);
		return builder.out.toByteArray();
	}

	static byte[] relationship(long id, long sourceId, long targetId, String label, int color, boolean singleArrow) {
		MmpBuilder builder = new MmpBuilder();
		builder.u8(0x32);
		builder.u32(id);
		builder.zeros(16);
		builder.u32(3);
		builder.color(color);
		builder.u32(0);
		builder.u32(singleArrow ? 1 : 0);
		builder.u32(0);
		builder.link("");
		builder.string(label);
		builder.u8(0);
		builder.u8(0x32);
		builder.u8(0x01);
		builder.u32(4);
		builder.u32(0);
		builder.u8(0xff);
		builder.u32(targetId);
		builder.u32(sourceId);
		return builder.out.toByteArray();
	}

	private void topic(TopicSpec topic, boolean mainTopic) {
		if (mainTopic)
			mainTopicClassIndex = classTag("TopicC", mainTopicClassWritten, mainTopicClassIndex);
		else
			topicClassIndex = classTag("ItemC", topicClassWritten, topicClassIndex);
		if (mainTopic)
			mainTopicClassWritten = true;
		else
			topicClassWritten = true;
		u8(0x33);
		string(topic.text);
		zeros(8);
		color(0xffffff);
		int flags = 1 | (topic.priority != 0 ? 2 : 0) | (topic.note != null ? 4 : 0);
		u32(flags);
		zeros(13);
		string(topic.note != null ? "&" : "");
		u32((topic.priority != 0 ? topic.priority : 1) | topic.priorityHighBits);
		u32(topic.symbol != null || topic.unknownObject ? 1 : 0);
		if (topic.symbol != null)
			symbol(topic.symbol);
		else if (topic.unknownObject) {
			classTag("WeirdC", false, 0);
			bytes(0x32, 0x07, 0x00, 0x01, 0x02, 0x03);
		}
		u8(0x32);
		u16(topic.style);
		color(topic.lineColor);
		color(topic.fillColor);
		if (topic.fontName != null) {
			u16(0);
			u32(topic.fontSize);
			string(topic.fontName);
			zeros(2);
		}
		else
			u16(3);
		u8(0);
		u8(0x32);
		u8(0xff);
		zeros(3);
		u32(topic.id);
		note(topic);
		u8(0);
		u8(0x32);
		u8(0xff);
		u32(topic.relationships.size());
		for (byte[] relationship : topic.relationships)
			out.write(relationship, 0, relationship.length);
		u32(topic.iconMask != 0 ? 1 : 0);
		u32(topic.iconMask);
		zeros(29);
		if (topic.cloudColor >= 0) {
			classTag("BoundaryC", false, 0);
			bytes(0x32, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x32, 0x01);
			u32(4);
			color(topic.cloudColor);
			u8(0xff);
		}
		else
			u16(0);
		link("");
		zeros(22);
		u32(topic.secondIconMask);
		link(topic.hyperlink);
		u32(-1);
		bytes(0x00, 0x33, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x03, 0x02, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00);
		u8(0xff);
		u16(topic.children.size());
		for (TopicSpec child : topic.children)
			topic(child, false);
		bytes(0x32, 0x01, 0x00, 0x00, 0x32, 0xff);
		if (mainTopic)
			bytes(0x32, 0x00, 0x00, 0x00, 0x00, topic.left ? 1 : 0, 0x00, 0x00, 0x32, 0xff);
	}

	private void symbol(byte[] emf) {
		symbolClassIndex = classTag("FreeSymbolC", symbolClassWritten, symbolClassIndex);
		symbolClassWritten = true;
		zeros(3 + 4 * 4 + 2 + 2 * 4 + 2 + 4 + 16);
		link("");
		bytes(0x00, 0x32, 0xff, 0x32);
		u8(0x32);
		u32(emf.length);
		u32(2);
		u8(0);
		out.write(emf, 0, emf.length);
		zeros(16);
	}

	private void note(TopicSpec topic) {
		byte[] content = topic.note != null ? topic.note : new byte[] { '\n' };
		u8(0x32);
		u32(content.length);
		if (!topic.compressNote) {
			u32(0);
			out.write(content, 0, content.length);
			return;
		}
		Deflater deflater = new Deflater();
		deflater.setInput(content);
		deflater.finish();
		byte[] buffer = new byte[content.length + 1024];
		int compressedLength = deflater.deflate(buffer);
		deflater.end();
		u32(4 + 8 + 8 + compressedLength);
		u32(1);
		u32(content.length);
		u32(compressedLength);
		zeros(8);
		out.write(buffer, 0, compressedLength);
	}

	private int classTag(String name, boolean written, int index) {
		if (written) {
			u16(0x8000 | index);
			nextIndex++;
			return index;
		}
		u16(0xffff);
		u16(1);
		u16(name.length());
		bytes(name.getBytes(ANSI));
		int classIndex = nextIndex;
		nextIndex += 2;
		return classIndex;
	}

	private void link(String link) {
		u8(0x32);
		string(link);
		zeros(4);
		u8(0x32);
		u8(0xff);
	}

	private void string(String value) {
		byte[] bytes = value.getBytes(ANSI);
		if (bytes.length < 0xff)
			u8(bytes.length);
		else {
			u8(0xff);
			u16(bytes.length);
		}
		bytes(bytes);
	}

	private void color(int rgb) {
		bytes(rgb >> 16 & 0xff, rgb >> 8 & 0xff, rgb & 0xff, 0x02);
	}

	private void u8(int value) {
		out.write(value);
	}

	private void u16(int value) {
		u8(value);
		u8(value >> 8);
	}

	private void u32(long value) {
		u16((int) value);
		u16((int) (value >> 16));
	}

	private void zeros(int count) {
		for (int i = 0; i < count; i++)
			u8(0);
	}

	private void bytes(int... values) {
		for (int value : values)
			u8(value);
	}

	private void bytes(byte[] values) {
		out.write(values, 0, values.length);
	}
}
