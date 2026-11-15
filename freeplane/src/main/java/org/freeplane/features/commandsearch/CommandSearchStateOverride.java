package org.freeplane.features.commandsearch;

import java.util.LinkedHashMap;
import java.util.Map;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.commandsearch.CommandSearchDialog.Scope;

class CommandSearchStateOverride {
	static final String WHOLE_WORDS_PROPERTY = "cmdsearch_whole_words";
	static final String CLOSE_AFTER_EXECUTE_PROPERTY = "cmdsearch_close_after_execute";

	interface Store {
		boolean get(String name);

		void set(String name, boolean value);
	}

	static class ResourceStore implements Store {
		@Override
		public boolean get(final String name) {
			return ResourceController.getResourceController().getBooleanProperty(name, true);
		}

		@Override
		public void set(final String name, final boolean value) {
			ResourceController.getResourceController().setProperty(name, value);
		}
	}

	private final Store store;
	private final Map<String, Boolean> savedStates = new LinkedHashMap<String, Boolean>();

	static CommandSearchStateOverride forPreferencesOnly() {
		return forPreferencesOnly(new ResourceStore());
	}

	static CommandSearchStateOverride forPreferencesOnly(final Store store) {
		final CommandSearchStateOverride override = new CommandSearchStateOverride(store);
		override.apply(Scope.PREFERENCES.propertyName(), true);
		override.apply(Scope.MENUS.propertyName(), false);
		override.apply(Scope.ICONS.propertyName(), false);
		override.apply(WHOLE_WORDS_PROPERTY, false);
		override.apply(CLOSE_AFTER_EXECUTE_PROPERTY, true);
		return override;
	}

	private CommandSearchStateOverride(final Store store) {
		this.store = store;
	}

	private void apply(final String name, final boolean value) {
		savedStates.put(name, store.get(name));
		store.set(name, value);
	}

	void restore() {
		for (final Map.Entry<String, Boolean> saved : savedStates.entrySet())
			store.set(saved.getKey(), saved.getValue());
		savedStates.clear();
	}
}
