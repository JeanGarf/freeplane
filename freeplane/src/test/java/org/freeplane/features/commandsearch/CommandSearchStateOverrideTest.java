package org.freeplane.features.commandsearch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.freeplane.features.commandsearch.CommandSearchDialog.Scope;
import org.junit.Test;

public class CommandSearchStateOverrideTest {
	private static class MapStore implements CommandSearchStateOverride.Store {
		final Map<String, Boolean> values = new HashMap<String, Boolean>();

		@Override
		public boolean get(final String name) {
			return values.getOrDefault(name, true);
		}

		@Override
		public void set(final String name, final boolean value) {
			values.put(name, value);
		}
	}

	private static MapStore allEnabledExceptCloseAfterExecute() {
		final MapStore store = new MapStore();
		store.set(Scope.MENUS.propertyName(), true);
		store.set(Scope.PREFERENCES.propertyName(), false);
		store.set(Scope.ICONS.propertyName(), true);
		store.set(CommandSearchStateOverride.WHOLE_WORDS_PROPERTY, true);
		store.set(CommandSearchStateOverride.CLOSE_AFTER_EXECUTE_PROPERTY, false);
		return store;
	}

	@Test
	public void keepsOnlyPreferencesAndClosingAfterExecute() {
		final MapStore store = allEnabledExceptCloseAfterExecute();

		CommandSearchStateOverride.forPreferencesOnly(store);

		assertThat(store.get(Scope.PREFERENCES.propertyName())).isTrue();
		assertThat(store.get(Scope.MENUS.propertyName())).isFalse();
		assertThat(store.get(Scope.ICONS.propertyName())).isFalse();
		assertThat(store.get(CommandSearchStateOverride.WHOLE_WORDS_PROPERTY)).isFalse();
		assertThat(store.get(CommandSearchStateOverride.CLOSE_AFTER_EXECUTE_PROPERTY)).isTrue();
	}

	@Test
	public void restoresInitialStatesOnRestore() {
		final MapStore store = allEnabledExceptCloseAfterExecute();
		final Map<String, Boolean> initial = new HashMap<String, Boolean>(store.values);

		final CommandSearchStateOverride override = CommandSearchStateOverride.forPreferencesOnly(store);
		store.set(Scope.MENUS.propertyName(), true);
		override.restore();

		assertThat(store.values).isEqualTo(initial);
	}

	@Test
	public void restoresOnlyOnce() {
		final MapStore store = allEnabledExceptCloseAfterExecute();
		final CommandSearchStateOverride override = CommandSearchStateOverride.forPreferencesOnly(store);
		override.restore();
		store.set(Scope.MENUS.propertyName(), false);

		override.restore();

		assertThat(store.get(Scope.MENUS.propertyName())).isFalse();
	}
}
