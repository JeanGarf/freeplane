package org.freeplane.features.mindmanager2002;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

class ConversionTable {
	static final String FILE_NAME = "mindmanager2002-import.properties";
	static final String LOCALIZED_STYLE_PREFIX = "styles.";
	private static final String STYLE_PREFIX = "style.";
	private static final String ICON_PREFIX = "icon.";
	private static final String PRIORITY_PREFIX = "priority.";

	private final Map<Integer, String> styles = new TreeMap<>();
	private final Map<String, String> icons = new TreeMap<>();

	static ConversionTable load(File file) throws IOException {
		if (!file.isFile())
			return new ConversionTable();
		return parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
	}

	static ConversionTable parse(String content) throws IOException {
		Properties properties = new Properties();
		try (Reader reader = new StringReader(content)) {
			properties.load(reader);
		}
		ConversionTable table = new ConversionTable();
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key).trim();
			if (key.startsWith(STYLE_PREFIX)) {
				try {
					table.styles.put(Integer.parseInt(key.substring(STYLE_PREFIX.length()), 16), value);
				}
				catch (NumberFormatException e) {
					continue;
				}
			}
			else if (key.startsWith(ICON_PREFIX) || key.startsWith(PRIORITY_PREFIX))
				table.icons.put(key, value);
		}
		return table;
	}

	void save(File file) throws IOException {
		File directory = file.getParentFile();
		if (directory != null && !directory.isDirectory() && !directory.mkdirs())
			throw new IOException("Cannot create " + directory);
		Files.write(file.toPath(), format().getBytes(StandardCharsets.UTF_8));
	}

	String format() {
		StringBuilder content = new StringBuilder();
		content.append("# MindManager 2002 import\n");
		content.append("# style.<fill color>=<Freeplane style> (empty: keep the fill color)\n");
		for (Map.Entry<Integer, String> style : styles.entrySet())
			appendEntry(content, STYLE_PREFIX + String.format("%06x", style.getKey()), style.getValue());
		content.append("# icon.<MindManager icon>=<Freeplane icon> (empty: no icon)\n");
		for (String[] icon : IconMapping.DEFAULT_ICONS)
			appendEntry(content, ICON_PREFIX + icon[0], iconFor(ICON_PREFIX + icon[0], icon[1]));
		content.append("# priority.<MindManager priority>=<Freeplane icon> (empty: no icon)\n");
		for (int priority = 1; priority <= IconMapping.MAX_PRIORITY; priority++)
			appendEntry(content, PRIORITY_PREFIX + priority,
					iconFor(PRIORITY_PREFIX + priority, IconMapping.defaultPriorityIcon(priority)));
		return content.toString();
	}

	boolean knowsColor(int rgb) {
		return styles.containsKey(rgb);
	}

	String style(int rgb) {
		String style = styles.get(rgb);
		return style == null || style.isEmpty() ? null : style;
	}

	void setStyle(int rgb, String style) {
		styles.put(rgb, style == null ? "" : style);
	}

	String icon(int index) {
		String key = IconMapping.iconKey(index);
		if (key == null)
			return null;
		return emptyToNull(iconFor(ICON_PREFIX + key, IconMapping.DEFAULT_ICONS[index][1]));
	}

	String priorityIcon(int priority) {
		if (priority < 1 || priority > IconMapping.MAX_PRIORITY)
			return null;
		return emptyToNull(iconFor(PRIORITY_PREFIX + priority, IconMapping.defaultPriorityIcon(priority)));
	}

	private String iconFor(String key, String defaultIcon) {
		String icon = icons.get(key);
		return icon == null ? defaultIcon : icon;
	}

	private static String emptyToNull(String value) {
		return value.isEmpty() ? null : value;
	}

	private static void appendEntry(StringBuilder content, String key, String value) {
		content.append(key).append('=').append(escape(value)).append('\n');
	}

	private static String escape(String value) {
		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			if (character == '\\' || character == '=' || character == ':' || i == 0 && (character == ' '
					|| character == '#' || character == '!'))
				escaped.append('\\');
			escaped.append(character);
		}
		return escaped.toString();
	}
}
