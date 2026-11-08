package org.freeplane.features.mindmanager2002;

class IconMapping {
	private static final String[] FREEPLANE_ICONS = {
			"idea",
			"up",
			"down",
			"forward",
			"flag",
			"help",
			"ksmiletris",
			"smily_bad",
			"Mail",
			"emoji-1F4DE",
			"messagebox_warning",
			"emoji-1F4A3",
			"calendar",
			"hourglass",
			"prepare",
			"emoji-2702",
			"button_cancel",
			"stop-sign",
			"info",
			"emoji-2757",
			"clock",
			"emoji-1F60E",
			"smiley-angry",
			"smiley-oh",
			"emoji-26A0",
			"emoji-1F527",
			"emoji-1F69A",
			"emoji-1F6AB",
			"emoji-1F44D",
			"emoji-1F44E",
			"emoji-1F91D",
			"pencil",
			"emoji-1F4B2",
			"emoji-1F453",
			"internet",
			"group",
			"emoji-1F4D6",
			"gohome",
			"back",
			"emoji-2195",
			"button_ok",
			"unchecked",
	};
	private static final int MAX_PRIORITY = 9;

	static String icon(int index) {
		return index >= 0 && index < FREEPLANE_ICONS.length ? FREEPLANE_ICONS[index] : null;
	}

	static String priorityIcon(int priority) {
		return priority >= 1 && priority <= MAX_PRIORITY ? "full-" + priority : null;
	}
}
