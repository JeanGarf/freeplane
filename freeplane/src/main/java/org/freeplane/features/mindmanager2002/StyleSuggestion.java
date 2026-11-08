package org.freeplane.features.mindmanager2002;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

class StyleSuggestion {
	static String suggest(String label, List<String> styleNames) {
		if (label == null)
			return null;
		String normalizedLabel = normalize(label);
		if (normalizedLabel.isEmpty())
			return null;
		for (String styleName : styleNames) {
			if (normalize(styleName).equals(normalizedLabel))
				return styleName;
		}
		for (String styleName : styleNames) {
			String normalizedStyle = normalize(styleName);
			if (!normalizedStyle.isEmpty()
					&& (normalizedStyle.startsWith(normalizedLabel) || normalizedLabel.startsWith(normalizedStyle)))
				return styleName;
		}
		return null;
	}

	static String normalize(String text) {
		String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		String words = withoutAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
		StringBuilder singular = new StringBuilder();
		for (String word : words.split(" ")) {
			if (singular.length() > 0)
				singular.append(' ');
			singular.append(word.length() > 3 && word.endsWith("s") ? word.substring(0, word.length() - 1) : word);
		}
		return singular.toString();
	}
}
