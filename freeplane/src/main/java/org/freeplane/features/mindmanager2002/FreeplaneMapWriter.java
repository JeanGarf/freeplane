package org.freeplane.features.mindmanager2002;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;

class FreeplaneMapWriter {
	interface ImageStore {
		String store(String name, BufferedImage image) throws IOException;
	}

	private static final String ROOT_ID = "ID_ROOT";

	private final ImageStore imageStore;
	private final ConversionTable table;
	private final Function<String, StyleAppearance> styleAppearance;
	private final StringBuilder xml = new StringBuilder();
	private final Set<Long> topicIds = new HashSet<>();
	private final Map<Long, Integer> imageCounters = new HashMap<>();

	FreeplaneMapWriter(ImageStore imageStore, ConversionTable table,
			Function<String, StyleAppearance> styleAppearance) {
		this.imageStore = imageStore;
		this.table = table;
		this.styleAppearance = styleAppearance;
	}

	String write(Mm2002Map map) throws IOException {
		collectIds(map.mainTopics);
		xml.append("<map version=\"").append(FreeplaneVersion.XML_VERSION).append("\">\n");
		xml.append("<node TEXT=\"").append(attribute(map.title)).append("\" ID=\"").append(ROOT_ID).append("\" FOLDED=\"false\">\n");
		for (Topic topic : map.mainTopics)
			writeTopic(topic, true);
		xml.append("</node>\n</map>\n");
		return xml.toString();
	}

	private void collectIds(Iterable<Topic> topics) {
		for (Topic topic : topics) {
			topicIds.add(topic.id);
			collectIds(topic.children);
		}
	}

	private void writeTopic(Topic topic, boolean mainTopic) throws IOException {
		xml.append("<node TEXT=\"").append(attribute(topic.text)).append("\" ID=\"").append(nodeId(topic.id))
				.append('"');
		if (mainTopic)
			xml.append(" POSITION=\"").append(topic.left ? "left" : "right").append('"');
		String style = topic.hasFill() ? table.style(topic.fillColor) : null;
		StyleAppearance appearance = style == null ? StyleAppearance.NONE : styleAppearance.apply(style);
		if (style != null)
			xml.append(style.startsWith(ConversionTable.LOCALIZED_STYLE_PREFIX) ? " LOCALIZED_STYLE_REF=\"" : " STYLE_REF=\"")
					.append(attribute(style)).append('"');
		else if (topic.hasFill())
			xml.append(" STYLE=\"bubble\" BACKGROUND_COLOR=\"").append(color(topic.fillColor)).append('"');
		if (!topic.hyperlink.isEmpty())
			xml.append(" LINK=\"").append(attribute(link(topic.hyperlink))).append('"');
		xml.append(">\n");
		if (topic.lineColor != Topic.NO_COLOR && !appearance.edgeColor)
			xml.append("<edge COLOR=\"").append(color(topic.lineColor)).append("\"/>\n");
		writeFont(topic, appearance);
		writeIcons(topic);
		writeSymbols(topic);
		writeRelationships(topic);
		writeNote(topic);
		for (Topic child : topic.children)
			writeTopic(child, false);
		xml.append("</node>\n");
	}

	private void writeFont(Topic topic, StyleAppearance appearance) {
		boolean bold = topic.bold && !appearance.bold;
		boolean italic = topic.italic && !appearance.italic;
		if (topic.fontName == null && !bold && !italic)
			return;
		xml.append("<font");
		if (topic.fontName != null && !topic.fontName.isEmpty())
			xml.append(" NAME=\"").append(attribute(topic.fontName)).append('"');
		if (topic.fontSize > 0)
			xml.append(" SIZE=\"").append(topic.fontSize).append('"');
		if (bold)
			xml.append(" BOLD=\"true\"");
		if (italic)
			xml.append(" ITALIC=\"true\"");
		xml.append("/>\n");
	}

	private void writeIcons(Topic topic) {
		writeIcon(table.priorityIcon(topic.priority));
		for (int index : topic.iconIndexes)
			writeIcon(table.icon(index));
	}

	private void writeIcon(String icon) {
		if (icon != null)
			xml.append("<icon BUILTIN=\"").append(icon).append("\"/>\n");
	}

	private void writeSymbols(Topic topic) throws IOException {
		for (byte[] symbol : topic.symbols) {
			BufferedImage image;
			try {
				image = EmfRenderer.render(symbol);
			}
			catch (IOException e) {
				LogUtils.warn("MindManager 2002 image skipped: " + e.getMessage());
				continue;
			}
			String uri = storeImage(topic, image);
			xml.append("<hook URI=\"").append(attribute(uri)).append("\" SIZE=\"1.0\" NAME=\"ExternalObject\"/>\n");
			return;
		}
	}

	private void writeRelationships(Topic topic) {
		for (Relationship relationship : topic.relationships) {
			if (relationship.sourceId != topic.id || !topicIds.contains(relationship.targetId))
				continue;
			xml.append("<arrowlink DESTINATION=\"").append(nodeId(relationship.targetId)).append("\" COLOR=\"")
					.append(color(relationship.color)).append("\" STARTARROW=\"")
					.append(relationship.arrowAtBothEnds ? "DEFAULT" : "NONE").append("\" ENDARROW=\"DEFAULT\"");
			if (!relationship.label.isEmpty())
				xml.append(" MIDDLE_LABEL=\"").append(attribute(relationship.label)).append('"');
			xml.append("/>\n");
		}
	}

	private void writeNote(Topic topic) throws IOException {
		if (topic.note.isEmpty())
			return;
		xml.append("<richcontent TYPE=\"NOTE\" CONTENT-TYPE=\"xml/\">\n<html>\n<head/>\n<body>\n");
		int imageIndex = 0;
		for (String paragraph : topic.note.paragraphs) {
			xml.append("<p>");
			for (char character : paragraph.toCharArray()) {
				if (character == Note.EMBEDDED_OBJECT) {
					if (imageIndex < topic.note.images.size())
						writeImage(topic, topic.note.images.get(imageIndex++));
				}
				else
					xml.append(text(String.valueOf(character)));
			}
			xml.append("</p>\n");
		}
		while (imageIndex < topic.note.images.size())
			writeImage(topic, topic.note.images.get(imageIndex++));
		xml.append("</body>\n</html>\n</richcontent>\n");
	}

	private void writeImage(Topic topic, BufferedImage image) throws IOException {
		xml.append("<img src=\"").append(attribute(storeImage(topic, image))).append("\"/>");
	}

	private String storeImage(Topic topic, BufferedImage image) throws IOException {
		int number = imageCounters.merge(topic.id, 1, Integer::sum);
		return imageStore.store("image_" + topic.id + "_" + number + ".png", image);
	}

	static String link(String hyperlink) {
		if (hyperlink.contains("://") || hyperlink.startsWith("mailto:"))
			return hyperlink;
		String path = hyperlink.replace('\\', '/');
		if (path.matches("^[A-Za-z]:/.*"))
			return uri("file", "/" + path);
		if (path.startsWith("//"))
			return uri("file", path);
		return uri(null, path);
	}

	static String uri(String scheme, String path) {
		try {
			return new URI(scheme, null, path, null).toString();
		}
		catch (URISyntaxException e) {
			return path;
		}
	}

	private static String nodeId(long id) {
		return "ID_" + id;
	}

	private static String color(int rgb) {
		return String.format("#%06x", rgb & 0xffffff);
	}

	private static String attribute(String value) {
		return escape(value.replace("\r\n", "\n"), true);
	}

	private static String text(String value) {
		return escape(value, false);
	}

	private static String escape(String value, boolean attribute) {
		StringBuilder escaped = new StringBuilder(value.length());
		for (char character : value.toCharArray()) {
			switch (character) {
			case '&':
				escaped.append("&amp;");
				break;
			case '<':
				escaped.append("&lt;");
				break;
			case '>':
				escaped.append("&gt;");
				break;
			case '"':
				escaped.append("&quot;");
				break;
			case '\n':
				escaped.append(attribute ? "&#xa;" : "\n");
				break;
			case '\t':
				escaped.append(attribute ? "&#x9;" : "\t");
				break;
			default:
				if (character >= ' ')
					escaped.append(character);
			}
		}
		return escaped.toString();
	}
}
