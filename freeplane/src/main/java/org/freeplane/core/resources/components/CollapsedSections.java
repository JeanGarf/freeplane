package org.freeplane.core.resources.components;

import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.resources.ResourceController;

class CollapsedSections {
	interface Storage {
		String read();
		void write(String value);
	}

	static final String PROPERTY_NAME = "collapsedSections";
	private static final String SEPARATOR = ",";

	private static Storage storage = new ResourceControllerStorage();
	private static Set<String> collapsed;

	static String key(String context, String sectionKey) {
		return context + ":" + sectionKey;
	}

	static synchronized boolean isCollapsed(String key) {
		return loaded().contains(key);
	}

	static synchronized void setCollapsed(String key, boolean collapsed) {
		final Set<String> keys = loaded();
		final boolean changed = collapsed ? keys.add(key) : keys.remove(key);
		if (changed)
			storage.write(serialize(keys));
	}

	static synchronized void clear() {
		collapsed = new LinkedHashSet<>();
		storage.write("");
	}

	static synchronized void useStorage(Storage newStorage) {
		storage = newStorage;
		collapsed = null;
	}

	static String serialize(Set<String> keys) {
		return String.join(SEPARATOR, keys);
	}

	static Set<String> parse(String value) {
		final Set<String> keys = new LinkedHashSet<>();
		if (value != null)
			for (String key : value.split(SEPARATOR))
				if (!key.trim().isEmpty())
					keys.add(key.trim());
		return keys;
	}

	private static Set<String> loaded() {
		if (collapsed == null)
			collapsed = parse(storage.read());
		return collapsed;
	}

	private static class ResourceControllerStorage implements Storage {
		@Override
		public String read() {
			try {
				return ResourceController.getResourceController().getProperty(PROPERTY_NAME, "");
			} catch (RuntimeException e) {
				return "";
			}
		}

		@Override
		public void write(String value) {
			try {
				ResourceController.getResourceController().setProperty(PROPERTY_NAME, value);
			} catch (RuntimeException e) {
			}
		}
	}
}
