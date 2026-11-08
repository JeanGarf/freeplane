package org.freeplane.features.mindmanager2002;

class IconMapping {
	static final String[][] DEFAULT_ICONS = {
			{ "solution", "idea" },
			{ "more_important", "up" },
			{ "less_important", "down" },
			{ "action", "forward" },
			{ "milestone", "flag" },
			{ "question", "help" },
			{ "this_is_good", "ksmiletris" },
			{ "this_does_not_work", "smily_bad" },
			{ "send_information", "Mail" },
			{ "phone_number", "emoji-1F4DE" },
			{ "problem", "messagebox_warning" },
			{ "bomb", "emoji-1F4A3" },
			{ "date", "calendar" },
			{ "waiting_for_information", "hourglass" },
			{ "in_progress", "prepare" },
			{ "cut_this_out", "emoji-2702" },
			{ "drop_it", "button_cancel" },
			{ "emergency", "stop-sign" },
			{ "information", "info" },
			{ "interesting", "emoji-2757" },
			{ "pending", "clock" },
			{ "cool", "emoji-1F60E" },
			{ "angry", "smiley-angry" },
			{ "confusing", "smiley-oh" },
			{ "issue", "emoji-26A0" },
			{ "fixed", "emoji-1F527" },
			{ "deliver", "emoji-1F69A" },
			{ "not_allowed", "emoji-1F6AB" },
			{ "thumbs_up", "emoji-1F44D" },
			{ "thumbs_down", "emoji-1F44E" },
			{ "compromise", "emoji-1F91D" },
			{ "make_a_note", "pencil" },
			{ "budget_information", "emoji-1F4B2" },
			{ "read_this", "emoji-1F453" },
			{ "open_source", "internet" },
			{ "meeting_or_conference", "group" },
			{ "read_document", "emoji-1F4D6" },
			{ "place", "gohome" },
			{ "look_at_parent_branch", "back" },
			{ "both_important", "emoji-2195" },
			{ "task_completed", "button_ok" },
			{ "task_not_completed", "unchecked" },
	};
	static final int MAX_PRIORITY = 9;

	static String iconKey(int index) {
		return index >= 0 && index < DEFAULT_ICONS.length ? DEFAULT_ICONS[index][0] : null;
	}

	static String defaultPriorityIcon(int priority) {
		return "full-" + priority;
	}
}
