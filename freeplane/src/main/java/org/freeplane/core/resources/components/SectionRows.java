package org.freeplane.core.resources.components;

import java.util.ArrayList;
import java.util.List;

class SectionRows {
	static final class Range {
		final int first;
		final int last;

		Range(int first, int last) {
			this.first = first;
			this.last = last;
		}

		boolean contains(int row) {
			return row >= first && row <= last;
		}

		boolean isEmpty() {
			return last < first;
		}
	}

	static List<Range> collapsibleRanges(int[] sortedHeaderRows, int rowCount) {
		final List<Range> ranges = new ArrayList<>();
		for (int i = 0; i < sortedHeaderRows.length; i++) {
			final int first = sortedHeaderRows[i] + 1;
			final int last = i + 1 < sortedHeaderRows.length ? sortedHeaderRows[i + 1] - 2 : rowCount;
			ranges.add(new Range(first, last));
		}
		return ranges;
	}
}
