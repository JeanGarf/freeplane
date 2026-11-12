package org.freeplane.features.styles.mindmapmode.stylespane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ManageStylesMenuLayoutTest {
	private static final String SEP = ManageStylesMenuLayout.SEPARATOR;

	@Test
	public void keepsEverythingWhenAllActionsExist() {
		final List<String> result = ManageStylesMenuLayout.available(ManageStylesMenuLayout.ENTRIES, key -> true);

		assertThat(result).isEqualTo(ManageStylesMenuLayout.ENTRIES);
	}

	@Test
	public void skipsMissingActions() {
		final List<String> result = ManageStylesMenuLayout.available(Arrays.asList("a", "b", "c"), key -> !key.equals("b"));

		assertThat(result).containsExactly("a", "c");
	}

	@Test
	public void dropsSeparatorsThatWouldLeadTrailOrRepeat() {
		final List<String> result = ManageStylesMenuLayout.available(Arrays.asList(SEP, "a", SEP, "b", SEP, "c", SEP),
		    key -> !key.equals("b") && !key.equals("c"));

		assertThat(result).containsExactly("a");
	}

	@Test
	public void keepsSeparatorBetweenRemainingGroups() {
		final List<String> result = ManageStylesMenuLayout.available(Arrays.asList("a", "b", SEP, "c", "d"),
		    key -> !key.equals("b"));

		assertThat(result).containsExactly("a", SEP, "c", "d");
	}
}
