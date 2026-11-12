package org.freeplane.core.resources.components;

import static org.assertj.core.api.Assertions.assertThat;

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
}
