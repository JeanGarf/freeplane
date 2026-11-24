package org.freeplane.features.mindmanager2002;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class Mm2002Map {
	private static final String MMP_EXTENSION = ".mmp";
	private static final String CONVERTED_SUFFIX = ".mm";

	final String title;
	final List<Topic> mainTopics = new ArrayList<>();
	final Map<Integer, String> legend = new LinkedHashMap<>();
	final List<ImportProblem> problems = new ArrayList<>();

	Mm2002Map(String title) {
		this.title = title;
	}

	void sortMainTopicsFromTopToBottom() {
		mainTopics.sort(Comparator.comparingInt((Topic topic) -> topic.y).reversed());
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

	boolean hasMindManagerFileLinks() {
		return countMindManagerFileLinks(mainTopics) > 0;
	}

	void retargetMindManagerFileLinks() {
		retarget(mainTopics);
	}

	static boolean isMindManagerFileLink(Topic topic) {
		return topic.hyperlinkArguments.trim().isEmpty() && topic.hyperlink.toLowerCase(Locale.ROOT).endsWith(MMP_EXTENSION);
	}

	private static int countMindManagerFileLinks(List<Topic> topics) {
		int count = 0;
		for (Topic topic : topics) {
			if (isMindManagerFileLink(topic))
				count++;
			count += countMindManagerFileLinks(topic.children);
		}
		return count;
	}

	private static void retarget(List<Topic> topics) {
		for (Topic topic : topics) {
			if (isMindManagerFileLink(topic))
				topic.hyperlink = topic.hyperlink + CONVERTED_SUFFIX;
			retarget(topic.children);
		}
	}
}
