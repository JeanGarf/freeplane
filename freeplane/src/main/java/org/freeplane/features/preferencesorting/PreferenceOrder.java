package org.freeplane.features.preferencesorting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreferenceOrder {
	public static <T> List<T> reorder(final List<T> all, final List<T> sortedSubset) {
		final List<T> reordered = new ArrayList<T>(all.size());
		final Iterator<T> sorted = sortedSubset.iterator();
		for (final T element : all)
			reordered.add(sortedSubset.contains(element) ? sorted.next() : element);
		return reordered;
	}

	public static <T> List<T> ranked(final List<T> items, final List<Integer> ranking) {
		final List<T> ranked = new ArrayList<T>(ranking.size());
		for (final int item : ranking)
			ranked.add(items.get(item));
		return ranked;
	}

	public static String report(final List<String> names, final List<Integer> ranking, final int[] percentages) {
		final StringBuilder report = new StringBuilder();
		for (final int item : ranking) {
			if (report.length() > 0)
				report.append('\n');
			report.append(percentages[item]).append("% ").append(names.get(item));
		}
		return report.toString();
	}

	private PreferenceOrder() {
	}
}
