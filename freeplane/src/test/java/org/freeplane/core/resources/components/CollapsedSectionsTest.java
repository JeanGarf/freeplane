package org.freeplane.core.resources.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.Test;

public class CollapsedSectionsTest {
	@After
	public void cleanUp() {
		CollapsedSections.clear();
	}

	@Test
	public void remembersCollapsedState() {
		final String key = CollapsedSections.key("format", "OptionPanel.separator.NodeFont");

		CollapsedSections.setCollapsed(key, true);

		assertThat(CollapsedSections.isCollapsed(key)).isTrue();
	}

	@Test
	public void forgetsStateWhenExpanded() {
		final String key = CollapsedSections.key("format", "a");
		CollapsedSections.setCollapsed(key, true);

		CollapsedSections.setCollapsed(key, false);

		assertThat(CollapsedSections.isCollapsed(key)).isFalse();
	}

	@Test
	public void keepsContextsApart() {
		CollapsedSections.setCollapsed(CollapsedSections.key("format", "same"), true);

		assertThat(CollapsedSections.isCollapsed(CollapsedSections.key("preferences", "same"))).isFalse();
	}

	private static class MemoryStorage implements CollapsedSections.Storage {
		String value = "";
		public String read() { return value; }
		public void write(String newValue) { value = newValue; }
	}

	@Test
	public void writesCollapsedKeysToStorage() {
		final MemoryStorage storage = new MemoryStorage();
		CollapsedSections.useStorage(storage);

		CollapsedSections.setCollapsed("format:a", true);
		CollapsedSections.setCollapsed("format:b", true);
		CollapsedSections.setCollapsed("format:a", false);

		assertThat(storage.value).isEqualTo("format:b");
	}

	@Test
	public void readsCollapsedKeysFromStorage() {
		final MemoryStorage storage = new MemoryStorage();
		storage.value = "format:a, preferences:b,";
		CollapsedSections.useStorage(storage);

		assertThat(CollapsedSections.isCollapsed("format:a")).isTrue();
		assertThat(CollapsedSections.isCollapsed("preferences:b")).isTrue();
		assertThat(CollapsedSections.isCollapsed("format:c")).isFalse();
	}

	@Test
	public void serializesAndParsesRoundTrip() {
		final LinkedHashSet<String> keys = new LinkedHashSet<>(Arrays.asList("x:1", "y:2"));

		assertThat(CollapsedSections.parse(CollapsedSections.serialize(keys))).containsExactly("x:1", "y:2");
	}
}
