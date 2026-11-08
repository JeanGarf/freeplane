package org.freeplane.features.mindmanager2002;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class NewStyles {
	static Map<String, Integer> missing(Map<Integer, List<String>> fillColors, ConversionTable table,
			Predicate<String> existingStyle) {
		Map<String, Integer> missing = new LinkedHashMap<>();
		for (Integer color : fillColors.keySet()) {
			String style = table.style(color);
			if (style != null && !style.startsWith(ConversionTable.LOCALIZED_STYLE_PREFIX)
					&& !existingStyle.test(style))
				missing.putIfAbsent(style, color);
		}
		return missing;
	}
}
