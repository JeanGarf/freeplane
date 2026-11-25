package org.freeplane.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class RecentFilesHeadTest {
	@Test
	public void isFirstWhenItHeadsTheList() {
		assertThat(RecentFilesHead.isAlreadyFirst(Arrays.asList("MindMap:/a.mm", "MindMap:/b.mm"), "MindMap:/a.mm"))
		    .isTrue();
	}

	@Test
	public void isNotFirstWhenListedLaterOrAbsent() {
		assertThat(RecentFilesHead.isAlreadyFirst(Arrays.asList("MindMap:/a.mm", "MindMap:/b.mm"), "MindMap:/b.mm"))
		    .isFalse();
		assertThat(RecentFilesHead.isAlreadyFirst(Arrays.asList("MindMap:/a.mm"), "MindMap:/c.mm")).isFalse();
	}

	@Test
	public void isNotFirstInAnEmptyList() {
		assertThat(RecentFilesHead.isAlreadyFirst(Collections.<String>emptyList(), "MindMap:/a.mm")).isFalse();
	}
}
