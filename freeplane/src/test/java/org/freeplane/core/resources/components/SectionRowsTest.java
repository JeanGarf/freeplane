package org.freeplane.core.resources.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.freeplane.core.resources.components.SectionRows.Range;
import org.junit.Test;

public class SectionRowsTest {
	@Test
	public void collapsesFromRowAfterHeaderUpToGapBeforeNextHeader() {
		final List<Range> ranges = SectionRows.collapsibleRanges(new int[] { 2, 10, 20 }, 30);

		assertThat(ranges).hasSize(3);
		assertThat(ranges.get(0).first).isEqualTo(3);
		assertThat(ranges.get(0).last).isEqualTo(8);
		assertThat(ranges.get(1).first).isEqualTo(11);
		assertThat(ranges.get(1).last).isEqualTo(18);
	}

	@Test
	public void lastSectionExtendsToTheLastRow() {
		final List<Range> ranges = SectionRows.collapsibleRanges(new int[] { 2, 10 }, 15);

		assertThat(ranges.get(1).first).isEqualTo(11);
		assertThat(ranges.get(1).last).isEqualTo(15);
	}

	@Test
	public void sectionWithoutContentIsEmpty() {
		final List<Range> ranges = SectionRows.collapsibleRanges(new int[] { 2, 4 }, 10);

		assertThat(ranges.get(0).isEmpty()).isTrue();
	}

	@Test
	public void headerRowsOnlyBelongToNoRange() {
		final List<Range> ranges = SectionRows.collapsibleRanges(new int[] { 2, 10 }, 15);

		assertThat(ranges.get(0).contains(2)).isFalse();
		assertThat(ranges.get(0).contains(3)).isTrue();
		assertThat(ranges.get(0).contains(9)).isFalse();
	}

	@Test
	public void noHeadersGiveNoRanges() {
		assertThat(SectionRows.collapsibleRanges(new int[0], 10)).isEmpty();
	}
}
