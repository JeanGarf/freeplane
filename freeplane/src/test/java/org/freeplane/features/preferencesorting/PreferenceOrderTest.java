package org.freeplane.features.preferencesorting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class PreferenceOrderTest {

	@Test
	public void reordersSortedElementsInTheirSlotsOnly() {
		assertThat(PreferenceOrder.reorder(Arrays.asList("a", "hidden", "b", "c"), Arrays.asList("c", "a", "b")))
			.containsExactly("c", "hidden", "a", "b");
	}

	@Test
	public void listsItemsInRankingOrder() {
		assertThat(PreferenceOrder.ranked(Arrays.asList("Toyota", "Renault", "BYD"), Arrays.asList(2, 0, 1)))
			.containsExactly("BYD", "Toyota", "Renault");
	}

	@Test
	public void reportsPercentagesInRankingOrderOneLinePerItem() {
		assertThat(PreferenceOrder.report(Arrays.asList("Toyota", "Volkswagen", "Peugeot"), Arrays.asList(1, 0, 2),
			new int[] { 30, 60, 10 }))
			.isEqualTo("60% Volkswagen\n30% Toyota\n10% Peugeot");
	}
}
