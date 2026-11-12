package org.freeplane.core.ui.menubuilders.menu;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.junit.Test;

public class EntryPopupMenuFactoryTest {
	private static Entry entry(String name, Entry... children) {
		final Entry entry = new Entry();
		entry.setName(name);
		for (final Entry child : children)
			entry.addChild(child);
		return entry;
	}

	@Test
	public void findsEntryByNameAnywhereInTheTree() {
		final Entry target = entry("menu_manageStyles", entry("a"), entry("b"));
		final Entry root = entry("main_menu", entry("file"), entry("format", entry("x"), target));

		assertThat(EntryPopupMenuFactory.findByName(root, "menu_manageStyles")).isSameAs(target);
	}

	@Test
	public void findsTheRootItselfWhenNamesMatch() {
		final Entry root = entry("main_menu");

		assertThat(EntryPopupMenuFactory.findByName(root, "main_menu")).isSameAs(root);
	}

	@Test
	public void returnsNullWhenNoEntryHasTheName() {
		assertThat(EntryPopupMenuFactory.findByName(entry("main_menu", entry("file")), "missing")).isNull();
	}
}
