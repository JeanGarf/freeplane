package org.freeplane.core.resources.components;

import java.util.HashSet;
import java.util.Set;

class CollapsedSections {
	private static final Set<String> COLLAPSED = new HashSet<>();

	static String key(String context, String sectionKey) {
		return context + ":" + sectionKey;
	}

	static synchronized boolean isCollapsed(String key) {
		return COLLAPSED.contains(key);
	}

	static synchronized void setCollapsed(String key, boolean collapsed) {
		if (collapsed)
			COLLAPSED.add(key);
		else
			COLLAPSED.remove(key);
	}

	static synchronized void clear() {
		COLLAPSED.clear();
	}
}
