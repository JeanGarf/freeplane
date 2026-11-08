package org.freeplane.features.mindmanager2002;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
	private static final String ROOT_STYLE = "AutomaticLayout.level.root";
	private static final String FLOATING_STYLE = "styles.floating";
	static final double MAP_UNIT_IN_PIXELS = 0.27;

	private final ImageStore imageStore;
	private final ConversionTable table;
	private final Function<String, StyleAppearance> styleAppearance;
	private final StringBuilder xml = new StringBuilder();
	private final Set<Long> topicIds = new HashSet<>();
	private final Map<Long, Integer> imageCounters = new HashMap<>();
	private final Deque<String> branch = new ArrayDeque<>();
	private List<ImportProblem> problems;

	FreeplaneMapWriter(ImageStore imageStore, ConversionTable table,
			Function<String, StyleAppearance> styleAppearance) {
		this.imageStore = imageStore;
		this.table = table;
		this.styleAppearance = styleAppearance;
	}

	String write(Mm2002Map map) throws IOException {
		problems = map.problems;
		collectIds(map.mainTopics);
		xml.append("<map version=\"").append(FreeplaneVersion.XML_VERSION).append("\">\n");
		xml.append("<node TEXT=\"").append(attribute(map.title)).append("\" ID=\"").append(ROOT_ID).append("\" FOLDED=\"false\" LOCALIZED_STYLE_REF=\"")
				.append(ROOT_STYLE).append("\">\n");
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
		if (topic.isFolded())
			xml.append(" FOLDED=\"true\"");
		String style = topic.hasFill() ? table.style(topic.fillColor) : null;
		StyleAppearance appearance = style == null ? StyleAppearance.NONE : styleAppearance.apply(style);
		if (style != null)
			xml.append(style.startsWith(ConversionTable.LOCALIZED_STYLE_PREFIX) ? " LOCALIZED_STYLE_REF=\"" : " STYLE_REF=\"")
					.append(attribute(style)).append('"');
		else if (topic.hasFill())
			xml.append(" STYLE=\"bubble\" BACKGROUND_COLOR=\"").append(color(topic.fillColor)).append('"');
		if (!topic.hyperlink.isEmpty())
			xml.append(" LINK=\"").append(attribute(link(topic.hyperlink))).append('"');
		String alias = alias(topic.alias);
		if (!alias.isEmpty())
			xml.append(" ALIAS=\"").append(alias).append('"');
		xml.append(">\n");
		if (topic.lineColor != Topic.NO_COLOR && !appearance.edgeColor)
			xml.append("<edge COLOR=\"").append(color(topic.lineColor)).append("\"/>\n");
		if (topic.cloudColor != Topic.NO_COLOR)
			xml.append("<cloud COLOR=\"").append(color(topic.cloudColor)).append("\"/>\n");
		writeFont(topic, appearance);
		writeIcons(topic);
		writeSymbols(topic);
		writeRelationships(topic);
		writeNote(topic);
		branch.addLast(topic.text.replaceAll("\\s+", " ").trim());
		for (Topic child : topic.children)
			writeTopic(child, false);
		branch.removeLast();
		for (FreeText freeText : topic.freeTexts)
			writeFreeText(freeText);
		xml.append("</node>\n");
	}

	private void writeFont(Topic topic, StyleAppearance appearance) {
		writeFont(topic.fontName, topic.fontSize, topic.bold && !appearance.bold, topic.italic && !appearance.italic);
	}

	private void writeFont(String fontName, int fontSize, boolean bold, boolean italic) {
		if (fontName == null && !bold && !italic)
			return;
		xml.append("<font");
		if (fontName != null && !fontName.isEmpty())
			xml.append(" NAME=\"").append(attribute(fontName)).append('"');
		if (fontSize > 0)
			xml.append(" SIZE=\"").append(fontSize).append('"');
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

	private String branchOf(Topic topic) {
		StringBuilder path = new StringBuilder();
		for (String ancestor : branch)
			path.append(ancestor).append(" > ");
		return path.append(topic.text.replaceAll("\\s+", " ").trim()).toString();
	}

	private void writeSymbols(Topic topic) throws IOException {
		for (Picture picture : topic.pictures) {
			BufferedImage image;
			try {
				image = ImageDecoder.decode(picture.data);
			}
			catch (IOException e) {
				LogUtils.warn("MindManager 2002 image skipped: " + e.getMessage());
				problems.add(new ImportProblem(ImportProblem.Kind.IMAGE_IGNORED, branchOf(topic), e.getMessage()));
				continue;
			}
			String uri = storeImage(topic, image);
			xml.append("<hook URI=\"").append(attribute(uri)).append("\" SIZE=\"").append(zoom(picture, image))
					.append("\" NAME=\"ExternalObject\"/>\n");
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

	private void writeFreeText(FreeText freeText) {
		xml.append("<node TEXT=\"").append(attribute(freeText.text)).append("\" LOCALIZED_STYLE_REF=\"")
				.append(FLOATING_STYLE).append('"');
		if (freeText.color != Topic.NO_COLOR)
			xml.append(" COLOR=\"").append(color(freeText.color)).append('"');
		xml.append(">\n");
		writeFont(freeText.fontName, freeText.fontSize, freeText.bold, freeText.italic);
		xml.append("</node>\n");
	}

	static String alias(String name) {
		String alias = name.trim().replaceAll("[^\\p{L}\\p{N}_]+", "_");
		return !alias.isEmpty() && Character.isDigit(alias.charAt(0)) ? "_" + alias : alias;
	}

	static String zoom(Picture picture, BufferedImage image) {
		if (picture.displayWidth <= 0 || image.getWidth() <= 0)
			return "1.0";
		double zoom = picture.displayWidth * MAP_UNIT_IN_PIXELS / image.getWidth();
		return String.format(Locale.ROOT, "%.3f", zoom);
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
