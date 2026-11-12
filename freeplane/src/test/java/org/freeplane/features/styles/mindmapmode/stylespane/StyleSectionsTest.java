package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.StyleString;
import org.junit.Test;

public class StyleSectionsTest {
	private static List<IStyle> styles(String... names) {
		final List<IStyle> styles = new ArrayList<>();
		for (final String name : names)
			styles.add(new StyleString(name));
		return styles;
	}

	private static List<String> describe(List<StyleListItem> items) {
		final List<String> description = new ArrayList<>();
		for (final StyleListItem item : items)
			description.add(item.isHeader() ? "[" + item.header + "]" : item.style.toString());
		return description;
	}

	@Test
	public void groupsStylesUnderTheirSectionsInOrder() {
		final List<StyleListItem> items = StyleSections.items(styles("default", "floating", "topic", "flower", "level1", "level2"),
		    2, 2, "Predefined", "User", "Levels");

		assertThat(describe(items)).containsExactly("[Predefined]", "default", "floating", "[User]", "topic", "flower",
		    "[Levels]", "level1", "level2");
	}

	@Test
	public void omitsEmptySections() {
		final List<StyleListItem> items = StyleSections.items(styles("default", "floating", "level1"), 0, 1, "Predefined", "User", "Levels");

		assertThat(describe(items)).containsExactly("[Predefined]", "default", "floating", "[Levels]", "level1");
	}

	@Test
	public void keepsAllStylesWhenCountsExceedTheirTotal() {
		final List<StyleListItem> items = StyleSections.items(styles("a", "b"), 5, 5, "Predefined", "User", "Levels");

		assertThat(describe(items)).containsExactly("[Levels]", "a", "b");
	}

	@Test
	public void returnsNothingForNoStyles() {
		assertThat(StyleSections.items(Arrays.<IStyle>asList(), 0, 0, "Predefined", "User", "Levels")).isEmpty();
	}
}
