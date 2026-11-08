package org.freeplane.features.mindmanager2002;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Mm2002Map {
	final String title;
	final List<Topic> mainTopics = new ArrayList<>();
	final Map<Integer, String> legend = new LinkedHashMap<>();

	Mm2002Map(String title) {
		this.title = title;
	}

	Map<Integer, List<String>> fillColors() {
		Map<Integer, List<String>> colors = new LinkedHashMap<>();
		collectFillColors(mainTopics, colors);
		return colors;
	}

	private static void collectFillColors(List<Topic> topics, Map<Integer, List<String>> colors) {
		for (Topic topic : topics) {
			if (topic.hasFill())
				colors.computeIfAbsent(topic.fillColor, color -> new ArrayList<>()).add(topic.text);
			collectFillColors(topic.children, colors);
		}
	}
}
